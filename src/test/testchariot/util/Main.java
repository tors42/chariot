package util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;

import static util.Assert.filterCast;

public class Main {

    private final static URI api = URI.create(System.getenv("LILA_API") instanceof String env
            ? env
            : "http://lila:9663");
    public static final URI itApi() { return api; }

    public static void main(String[] args) throws Exception {

        var testType = Arrays.stream(args).anyMatch("it"::equals)
            ? IntegrationTest.class
            : Test.class;

        // 1 Find and run tests
        var testClasses = lookupTestClasses(testType);

        if (testType == IntegrationTest.class
            && ! integrationServerReady()) {
            System.err.println("Integration Server (" + api + ") wasn't ready.");
            System.exit(1);
        }

        for (var test : testClasses) {
            for (var method : test.methods()) {
                method.invoke(test.instance());
            }
        }

        // 2 Filter test results
        var success = filterCast(Assert.results.stream(), Assert.Success.class).distinct().toList();
        var failure = filterCast(Assert.results.stream(), Assert.Failure.class).toList();

        String testTypeName = testType == IntegrationTest.class
            ? "integration tests"
            : "basic tests";

        // 3 Print summary
        System.out.println("%d successful %s".formatted(success.size(), testTypeName));
        System.out.println("%d failed %s".formatted(failure.size(), testTypeName));

        // 4 Check if should fail with non-zero exit code
        if (! failure.isEmpty()) {
            System.err.println(String.join("\n",
                        failure.stream()
                        .map(f -> "\n%s\n%s".formatted(f.test(), f.message()))
                        .toList()));
            System.exit(1);
        }


        // 5 If random number between 0 to 1 is bigger than 1,
        // then show successul tests
        if (Math.random() > 1) {
            String successListing = String.join("\n",
                    success.stream()
                    .map(s -> "Success: %s".formatted(s.test()))
                    .toList());
            System.out.println(successListing);
        }
    }

    private static boolean integrationServerReady() {
        HttpClient http = HttpClient.newHttpClient();
        HttpRequest aliveRequest = HttpRequest.newBuilder(itApi()).HEAD().build();
        for (int attempt = 0; attempt < 10; attempt++) {
            try {
                var response = http.send(aliveRequest, BodyHandlers.discarding());
                System.out.println("Response: " + response);
                return true;
            } catch (Exception ex) {
                System.out.println("⌛ Waiting for lila to start...");
                try { Thread.sleep(Duration.ofSeconds(1)); } catch (InterruptedException ie) {}
            }
        }
        return false;
    }

    record InstanceAndMethods(Object instance, List<Method> methods) {}

    static List<InstanceAndMethods> lookupTestClasses(Class<? extends Annotation> testType) throws Exception {
        var result = new ArrayList<InstanceAndMethods>();

        var packages = Main.class.getModule().getPackages();

        for (String pkgName : packages) {
            String pkgPath = pkgName.replaceAll("[.]", "/");
            URI uri = ClassLoader.getSystemClassLoader().getResource(pkgPath).toURI();

            FileSystem fs = null;
            try {
                fs = FileSystems.getFileSystem(uri);
            } catch (FileSystemNotFoundException fsnfe) {
                fs = FileSystems.newFileSystem(uri, Map.of());
            }

            Path path = fs.getPath(pkgPath);
            var classNames = Files.walk(path, 1)
                .map(p -> p.getFileName().toString())
                .filter(name -> name.endsWith(".class"))
                .map(name -> name.substring(0, name.length()-".class".length()))
                .toList();

            try {
                for (String className : classNames) {
                    var cls = Class.forName(pkgName + "." + className);
                    var testMethods = Arrays.stream(cls.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(testType))
                        .toList();

                    if (! testMethods.isEmpty()) {
                        result.add(new InstanceAndMethods(cls.getDeclaredConstructor().newInstance(), testMethods));
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

}
