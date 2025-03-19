import java.lang.classfile.*;
import java.lang.classfile.instruction.InvokeInstruction;
import java.lang.instrument.*;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/// In `lila.relay.Env`,  
/// there's a task being scheduled for polling PGN broadcast URLs.
///
/// ```
/// scheduler.scheduleWithFixedDelay(1 minute, 1 minute): () =>
///   api.autoStart >> api.autoFinishNotSyncing
///
/// ```
///
/// During integration tests,
/// it would be nice if not need to wait the full minute for the server to start its polling.
///
/// One idea - rewrite the delays from 1 minute to 1 second.
///
/// Plan to try that idea out:
///  1. Find the ClassElement containing scheduleWithFixedDelay invocation.
///  2. During transform of said ClassElement,
///     find and replace DurationInt minute()-initializers with second()-initializers.
///  3. Profit!
public class FastAutoStartRelayAgent {

    /// Before the application starts, register a transformer of class files.
    public static void premain(String agentArgs, Instrumentation inst) {
        var transformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader      loader,
                                    String           className,
                                    Class<?>         classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[]           classBytes) {
                if ("lila/relay/Env".equals(className)) {
                    System.out.println("Agent found " + className);
                    return secondDelayInsteadOfMinuteDelay(classBytes);
                } else {
                    return null;
                }
            }
        };
        inst.addTransformer(transformer, true);
    }

    /// Rewrite scheduling of auto start relay to have a 1 second delay/repeat
    private static byte[] secondDelayInsteadOfMinuteDelay(byte[] classBytes) {

        var modified = new AtomicBoolean();

        ClassFile cf = ClassFile.of(ClassFile.DebugElementsOption.DROP_DEBUG);
        ClassModel classModel = cf.parse(classBytes);

        Predicate<MethodModel> invokesScheduleWithFixedDelay = methodModel ->
            methodModel.code()
                .map(codeModel -> codeModel
                        .elementStream()
                        .anyMatch(FastAutoStartRelayAgent::isInvocationOfScheduleWithFixedDelay))
                .orElse(false);

        CodeTransform rewriteDurationMethod =
            (codeBuilder, codeElement) -> {
                if (codeElement instanceof InvokeInstruction i
                    && i.opcode() == Opcode.INVOKEVIRTUAL
                    && "scala/concurrent/duration/package$DurationInt".equals(i.owner().asInternalName())
                    && "minute".equals(i.name().stringValue())) {

                    InvokeInstruction replaceWith = InvokeInstruction.of(
                            i.opcode(),
                            codeBuilder.constantPool()
                                .methodRefEntry(i.owner().asSymbol(), "second", i.typeSymbol()));
                    System.out.println("""
                            Replacing %s
                            with      %s""".formatted(i, replaceWith));
                    codeBuilder.with(replaceWith);
                    modified.set(true);
                } else {
                    codeBuilder.with(codeElement);
                }
            };

        ClassTransform ct = ClassTransform.transformingMethodBodies(invokesScheduleWithFixedDelay, rewriteDurationMethod);
        byte[] newClassBytes = cf.transformClass(classModel, ct);
        if (modified.get()) {
            return newClassBytes;
        } else {
            return null;
        }
    }

    private static boolean isInvocationOfScheduleWithFixedDelay(CodeElement codeElement) {
        return codeElement instanceof InvokeInstruction i
                && i.opcode() == Opcode.INVOKEINTERFACE
                && "akka/actor/Scheduler".equals(i.owner().asInternalName())
                && "scheduleWithFixedDelay".equals(i.name().stringValue())
                && "(Lscala/concurrent/duration/FiniteDuration;Lscala/concurrent/duration/FiniteDuration;Ljava/lang/Runnable;Lscala/concurrent/ExecutionContext;)Lakka/actor/Cancellable;"
                .equals(i.type().stringValue());
    }
}
