package build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;

class Build {

    public static void main(String... args) throws Exception {
        var props = Arrays.stream(args).map(s -> s.split("=")).collect(Collectors.toMap( kv -> kv[0], kv -> kv[1]));
        var module = props.getOrDefault("module", "chariot");
        var version = props.getOrDefault("version", "0.0.1-SNAPSHOT");

        var javac = ToolProvider.findFirst("javac").orElseThrow(() -> new RuntimeException("Missing javac tool"));
        var jar = ToolProvider.findFirst("jar").orElseThrow(() -> new RuntimeException("Missing jar tool"));
        var javadoc = ToolProvider.findFirst("javadoc").orElseThrow(() -> new RuntimeException("Missing javadoc tool"));

        String prefix = module + "-" + version;

        Path out = Path.of("out");
        del(out);

        Path moduleSrc = Path.of("src", "main");
        Path classes = out.resolve("classes");
        Path moduleOut = out.resolve("modules");
        Path metaInf = out.resolve("META-INF");
        Path manifest = out.resolve("MANIFEST.MF");

        Files.createDirectories(moduleOut);
        Files.createDirectories(metaInf);
        Files.copy(Path.of("LICENSE"), metaInf.resolve("LICENSE"));

        Files.writeString(manifest, """
                Implementation-Title: %s
                Implementation-Version: %s
                Created-By: %s
                """.formatted(module, version, Runtime.version()));

        run(javac,
                "-encoding", "UTF-8",
                "--module-source-path", moduleSrc.toString(),
                "--module", module,
                "-d", classes.toString()
           );

        run(jar,
                "--create",
                "--manifest", manifest.toString(),
                "--module-version", version,
                "--file", moduleOut.resolve(prefix + ".jar").toString(),
                "-C", out.toString(), "META-INF",
                "-C", classes.resolve(module).toString(), "."
           );

        run(javadoc,
                "--module-source-path", moduleSrc.toString(),
                "--module", module,
                "-d", out.resolve("javadoc").toString()
           );

        run(jar,
                "--create",
                "--manifest", manifest.toString(),
                "--file", out.resolve(prefix + "-javadoc.jar").toString(),
                "-C", out.toString(), "META-INF",
                "-C", out.resolve("javadoc").toString(), "."
           );

        run(jar,
                "--create",
                "--manifest", manifest.toString(),
                "--file", out.resolve(prefix + "-sources.jar").toString(),
                "-C", out.toString(), "META-INF",
                "-C", moduleSrc.toString(), "."
           );
    }

    static void del(Path dir) {
        if (dir.toFile().exists()) {
            try (var files = Files.walk(dir)) {
                files.sorted(Collections.reverseOrder()).map(Path::toFile).forEach(f -> f.delete());
            } catch (Exception e) {}
        }
    }

    static void run(ToolProvider tool, String... args) {
        var out = new StringWriter();
        var err = new StringWriter();

        int exitCode = tool.run(new PrintWriter(out), new PrintWriter(err), args);

        if (exitCode != 0) {
            out.flush();
            err.flush();
            System.err.format("""
                    %s exited with code %d
                    args:   %s
                    stdout: %s
                    stderr: %s%n""",
                    tool, exitCode, Arrays.stream(args).collect(Collectors.joining(" ")),
                    out.toString(), err.toString());
            System.exit(exitCode);
        }
    }

}
