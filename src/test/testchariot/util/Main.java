package util;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import static util.Assert.filterCast;

class Main {

    public static void main(String[] args) throws Exception {

        // 1 Find and run tests
        var testClasses = lookupTestClasses();
        for (var test : testClasses) {
            for (var method : test.methods()) {
                method.invoke(test.instance());
            }
        }

        // 2 Filter test results
        var success = filterCast(Assert.results.stream(), Assert.Success.class).distinct().toList();
        var failure = filterCast(Assert.results.stream(), Assert.Failure.class).toList();

        // 3 Print summary
        System.out.println("%d successful tests".formatted(success.size()));
        System.out.println("%d failed tests".formatted(failure.size()));


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


    record InstanceAndMethods(Object instance, List<Method> methods) {}

    static List<InstanceAndMethods> lookupTestClasses() throws Exception {
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
                        .filter(m -> m.isAnnotationPresent(Test.class))
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
