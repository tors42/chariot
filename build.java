///usr/bin/env java "$0" "$@" ; exit $?

void main(String[] args) throws Exception {
    String version   = args.length > 0 ? args[0] : "0.0.1-SNAPSHOT";
    String timestamp = args.length > 1 ? args[1] : ZonedDateTime.now()
        .withNano(0).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    String module = "chariot";
    String filenamePrefix = module + "-" + version;

    var javac   = ToolProvider.findFirst("javac").orElseThrow();
    var jar     = ToolProvider.findFirst("jar").orElseThrow();
    var javadoc = ToolProvider.findFirst("javadoc").orElseThrow();

    Path out = Path.of("out");
    del(out);

    Path src       = Path.of("src");
    Path classes   = out.resolve("classes");
    Path moduleOut = out.resolve("modules");
    Path metaInf   = out.resolve("META-INF");
    Path manifest  = out.resolve("MANIFEST.MF");

    for (Path dir : List.of(moduleOut, metaInf)) Files.createDirectories(dir);

    Files.copy(Path.of("LICENSE"), metaInf.resolve("LICENSE"));

    Files.writeString(manifest, """
            Implementation-Title: %s
            Implementation-Version: %s
            Created-By: %s
            """.formatted(module, version, Runtime.version()));

    boolean skipTests = System.getenv("SKIP_TESTS") != null;
    boolean itTests = !skipTests && System.getenv("LILA_API") != null;

    var executor = Executors.newCachedThreadPool();

    var buildResult = executor.submit(() -> {
        run(javac,
            "--release", "25",
            "--module-source-path", src,
            "--module", module,
            "-d", classes);

        run(jar,
            "--create",
            "--date", timestamp,
            "--manifest", manifest,
            "--module-version", version,
            "--file", moduleOut.resolve(filenamePrefix + ".jar"),
            "-C", out, "META-INF",
            "-C", classes.resolve(module), ".");

        run(javac,
            "--release", String.valueOf(Runtime.version().feature()),
            "--enable-preview",
            "--module-path", moduleOut,
            "--module-source-path", src,
            "--module", "testchariot",
            "--add-exports", "chariot/chariot.internal=testchariot",
            "--add-exports", "chariot/chariot.internal.yayson=testchariot",
            "-d", classes);

        run(jar,
            "--create",
            "--date", timestamp,
            "--manifest", manifest,
            "--module-version", version,
            "--main-class", "util.Main",
            "--file", moduleOut.resolve("test"+filenamePrefix + ".jar"),
            "-C", classes.resolve("testchariot"), ".");

        if (skipTests) return 0;

        int basicTests = new ProcessBuilder("java",
            "--enable-preview",
            "--add-exports", "chariot/chariot.internal=testchariot",
            "--add-exports", "chariot/chariot.internal.yayson=testchariot",
            "-p", moduleOut.toString(), "-m", "testchariot")
            .inheritIO().start().waitFor();

        if (basicTests != 0) return basicTests;

        if (itTests) {
            return new ProcessBuilder("java",
                "--enable-preview",
                "--add-exports", "chariot/chariot.internal=testchariot",
                "--add-exports", "chariot/chariot.internal.yayson=testchariot",
                "-p", moduleOut.toString(), "-m", "testchariot", "it")
                .inheritIO().start().waitFor();
        }
        return 0;
    });

    executor.submit(() -> {
        run(javadoc,
            "--release", "25",
            "-notimestamp",
            "--module-source-path", src,
            "--module", module,
            "--snippet-path", Path.of("build", "snippets").toString(),
            "-d", out.resolve("javadoc"));

        run(jar,
            "--create",
            "--date", timestamp,
            "--manifest", manifest,
            "--file", out.resolve(filenamePrefix + "-javadoc.jar"),
            "-C", out, "META-INF",
            "-C", out.resolve("javadoc"), ".");
    });

    executor.submit(() ->
        run(jar,
            "--create",
            "--date", timestamp,
            "--manifest", manifest,
            "--file", out.resolve(filenamePrefix + "-sources.jar"),
            "-C", out, "META-INF",
            "-C", src.resolve(module), "."));

    executor.shutdown();

    System.exit(buildResult.get());
}

void del(Path dir) {
    if (Files.exists(dir)) {
        try (var files = Files.walk(dir).map(Path::toFile)) {
            files.toList().reversed().forEach(File::delete);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}

void run(ToolProvider tool, Object... args) {
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
