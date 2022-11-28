package build;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.spi.*;

class Build {

    public static void main(String... args) throws Exception {
        String module = "chariot";
        String version = args.length > 0 ? args[0] : "0.0.1-SNAPSHOT";
        String timestamp = args.length > 1 ? args[1] : ZonedDateTime.now()
            .withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String filenamePrefix = module + "-" + version;

        var javac = ToolProvider.findFirst("javac").orElseThrow();
        var jar = ToolProvider.findFirst("jar").orElseThrow();
        var javadoc = ToolProvider.findFirst("javadoc").orElseThrow();

        Path out = Path.of("out");
        del(out);

        Path moduleSrc = Path.of("src", "main");
        Path classes = out.resolve("classes");
        Path moduleOut = out.resolve("modules");
        Path metaInf = out.resolve("META-INF");
        Path manifest = out.resolve("MANIFEST.MF");

        for (var dir : List.of(moduleOut, metaInf))
            Files.createDirectories(dir);

        Files.copy(Path.of("LICENSE"), metaInf.resolve("LICENSE"));

        Files.writeString(manifest, """
                Implementation-Title: %s
                Implementation-Version: %s
                Created-By: %s
                """.formatted(module, version, Runtime.version()));

        run(javac,
                "--release", "17",
                "--module-source-path", moduleSrc,
                "--module", module,
                "-d", classes
           );

        run(jar,
                "--create",
                "--date", timestamp,
                "--manifest", manifest,
                "--module-version", version,
                "--file", moduleOut.resolve(filenamePrefix + ".jar"),
                "-C", out, "META-INF",
                "-C", classes.resolve(module), "."
           );

        run(javadoc,
                "--release", "17",
                "-notimestamp",
                "--module-source-path", moduleSrc,
                "--module", module,
                "-d", out.resolve("javadoc")
           );

        run(jar,
                "--create",
                "--date", timestamp,
                "--manifest", manifest,
                "--file", out.resolve(filenamePrefix + "-javadoc.jar"),
                "-C", out, "META-INF",
                "-C", out.resolve("javadoc"), "."
           );

        run(jar,
                "--create",
                "--date", timestamp,
                "--manifest", manifest,
                "--file", out.resolve(filenamePrefix + "-sources.jar"),
                "-C", out, "META-INF",
                "-C", moduleSrc, "."
           );
    }

    static void del(Path dir) {
        if (dir.toFile().exists()) {
            try (var files = Files.walk(dir)) {
                files.sorted(Collections.reverseOrder()).map(Path::toFile).forEach(f -> f.delete());
            } catch (Exception e) {}
        }
    }

    static void run(ToolProvider tool, Object... args) {
        String[] stringArgs = Arrays.stream(args).map(Object::toString).toArray(String[]::new);
        var out = new StringWriter();
        var err = new StringWriter();

        int exitCode = tool.run(new PrintWriter(out), new PrintWriter(err), stringArgs);

        if (exitCode != 0) {
            out.flush();
            err.flush();
            System.err.format("""
                    %s exited with code %d
                    args:   %s
                    stdout: %s
                    stderr: %s%n""",
                    tool, exitCode, String.join(" ", stringArgs),
                    out, err);
            System.exit(exitCode);
        }
    }

}
