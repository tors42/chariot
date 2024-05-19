package chariot.internal.yayson;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import chariot.internal.Util;
import chariot.internal.yayson.Parser.*;
import chariot.model.Opt;

public class YayMapper {

    private final Config config;

    public static YayMapper mapper() {
        return mapper(new Config(false, new HashMap<>(), new HashMap<>()));
    }

    public static YayMapper mapper(Config config) {
        return new YayMapper(config);
    }

    private YayMapper(Config config) {
        this.config = config;
    }

    public record Config(boolean raw, Map<Class<?>, Map<String, String>> fieldMappings, Map<Class<?>, Function<YayNode, ?>> customMappings) {}

    public void setMappings(Class<?> cls, Map<String, String> mappings) {
        config.fieldMappings().put(cls, mappings);
    }

    public void setCustomMapper(Class<?> cls, Function<YayNode, ?> f) {
        config.customMappings().put(cls, f);
    }


    public <T> T fromString(String json, Class<T> cls) {

        try {
            var node = Parser.fromString(json);

            if (config.customMappings().containsKey(cls)) {
                var f = config.customMappings().get(cls);
                return cls.cast(f.apply(node));
            }

            // Here be dragons...
            T t = fromYayTree(node, cls);
            return t;
        } catch (Exception e) {
            System.err.println("""
                    Failed: T fromString(String json, Class<T> cls)
                    e.getMessage(): %s
                    cls.getName(): %s
                    json:
                    =======================
                    %s
                    =======================
            """.formatted(e.getMessage(), cls.getName(), json));
        }
        return null;
    }

    public <T> T fromYayTree(YayNode node, Class<T> cls) {
        try {

        if (config.customMappings().containsKey(cls)) {
            var f = config.customMappings().get(cls);
            return cls.cast(f.apply(node));
        }

        if (cls.isInterface() && cls.isSealed()) {

            return buildFromSealedInterface(node, cls, Optional.empty());

        } else if (cls.isRecord()) {

            var rcomp = Arrays.asList(cls.getRecordComponents());

            if (node instanceof YayObject yo) {
                List<?> instances = rcomp.stream()
                    .map(rc -> {

                        // See if we need to swap the json property name to a java friendly name
                        // json: { "int": 5 }
                        // java: int int = 5; <- not ok
                        // java: int intVariable = 5; <- "ok"
                        // So json "int" -> java "intVariable" mapping
                        var jsonName = config.fieldMappings().getOrDefault(cls, Map.of())
                            .getOrDefault(rc.getName(), rc.getName());

                        var parameterizedType = Optional.<ParameterizedType>empty();
                        if (rc.getGenericType() instanceof ParameterizedType pt) {
                            parameterizedType = Optional.of(pt);
                        }

                        Object obj = null;
                        try {
                            obj = rc.getType().isInterface() && rc.getType().isSealed() ?
                                buildFromSealedInterface(yo.value().get(jsonName), rc.getType(), parameterizedType) :
                                buildFromClass(yo.value().get(jsonName), rc.getType(), parameterizedType);
                        } catch (Exception e) {
                            System.err.println("Failure while looking for " + jsonName + " of type " +
                                    rc.getType() + " with (possibly) parameterized type " + parameterizedType);
                            System.err.println("""
                                    Failed: T fromYayTree(YayNode node, Class<T> cls)
                                    e.getMessage(): %s
                                    cls.getName(): %s
                                    node:
                                    =======================
                                    %s
                                    =======================
                                    """.formatted(e.getMessage(), cls.getName(), node));
                            e.printStackTrace(System.err);
                        }
                        return obj;
                    })
                .toList();

                T t;
                try {
                    var declaredCtors = cls.getDeclaredConstructors();
                    var ctor = declaredCtors[0];

                    if (declaredCtors.length > 1) {
                        var types = rcomp.stream()
                            .map(RecordComponent::getType)
                            .toList();
                        ctor = Arrays.stream(declaredCtors)
                            .filter(c -> Arrays.asList(c.getParameterTypes()).equals(types))
                            .findAny()
                            .orElseThrow();
                    }

                    // Create the instance!
                    t = cls.cast(ctor.newInstance(instances.toArray()));

                } catch (InstantiationException |
                        IllegalAccessException |
                        IllegalArgumentException |
                        InvocationTargetException e) {
                    e.printStackTrace();
                    System.out.println(" ########  Instances: " + instances);
                    System.out.println(" ========  node     : " + node);
                    t = null;
                        }
                return t;
            }
        }

        } catch (Exception e) {
            System.err.println("""
                    Failed: T fromYayTree(YayNode node, Class<T> cls)
                    e.getMessage(): %s
                    cls.getName(): %s
                    node:
                    =======================
                    %s
                    =======================
            """.formatted(e.getMessage(), cls.getName(), node));
            e.printStackTrace(System.err);
        }
        return null;
    }

    private <T> T buildFromSealedInterface(YayNode node, Class<T> cls, Optional<ParameterizedType> parameterizedType) {

        if (config.customMappings().containsKey(cls)) {
            var f = config.customMappings().get(cls);
            return cls.cast(f.apply(node));
        }

        if (cls.equals(Opt.class)) {
            if (node != null) {
                ParameterizedType pt = parameterizedType.get();
                Type typeArgument = pt.getActualTypeArguments()[0];
                if (typeArgument instanceof Class<?> typeClass) {
                    var value = buildFromClass(node, typeClass, Optional.empty());
                    @SuppressWarnings("unchecked")
                    T t = (T) Opt.of(value);
                    return t;
                }
            }
            @SuppressWarnings("unchecked")
            T t = (T) Opt.empty();
            return t;
        }

        if (node instanceof YayEmpty empty) {
            List<Class<?>> permittedRecordSubclasses = permittedRecordClassesOfSealedInterfaceHierarchy(cls);
            var opt = permittedRecordSubclasses.stream()
                .filter(p -> p.getRecordComponents().length == 0)
                .findFirst();
            if (opt.isPresent()) {
                var klass = opt.get();

                var o = buildFromClass(empty, klass, parameterizedType);
                if (o != null) {
                    return cls.cast(o);
                }
                return null;
            }
        }

        if (! (node instanceof YayObject yo)) {
            // Hmm, can be YayEmpty, I guess...
            return null;
        }
        var jsonFieldNames = yo.value().keySet();

        List<Class<?>> permittedRecordSubclasses = permittedRecordClassesOfSealedInterfaceHierarchy(cls);

        record ClassFieldMatches(Class<?> cls, int hit, int miss) {}
        var mostHit = Comparator.comparingInt(ClassFieldMatches::hit).reversed();
        var leastMiss = Comparator.comparingInt(ClassFieldMatches::miss);

        List<ClassFieldMatches> bestMatchList = permittedRecordSubclasses.stream()
            .map(rec -> {
                List<String> fieldsInRecord = Arrays.stream(rec.getRecordComponents())
                        .map(rc -> rc.getName())
                        .map(name -> config.fieldMappings().getOrDefault(rec, Map.of()).getOrDefault(name, name))
                        .toList();
                Map<Boolean, Long> hitMiss = fieldsInRecord.stream()
                    .collect(Collectors.groupingBy(jsonFieldNames::contains, Collectors.counting()));
                return new ClassFieldMatches(rec,
                        hitMiss.getOrDefault(true, 0l).intValue(),
                        hitMiss.getOrDefault(false, 0l).intValue());
            })
        .sorted(mostHit.thenComparing(leastMiss))
        .toList();

        if (bestMatchList.isEmpty()) {
            return null;
        }

        var first = bestMatchList.get(0);
        List<Class<?>> bestClasses = new ArrayList<>();
        bestClasses.addAll(bestMatchList.stream()
                .filter(cfm -> cfm.hit() == first.hit() && cfm.miss() == first.miss())
                .map(ClassFieldMatches::cls)
                .toList());

        record ClassValue(Class<?> cls, List<Method> getters, Object value) {}
        Comparator<ClassValue> withNullValuesLast = Comparator.comparing(cv -> cv.value() == null);
        Comparator<ClassValue> numberOfNonNullComponents = Comparator.comparing(cv -> cv.value() == null ? 0 : cv.getters().stream()
                        .filter(getter -> { try { return getter.invoke(cv.value()) != null; } catch (Exception e) {} return false; } )
                        .count());
        return bestClasses.stream()
            .map(bestClass -> new ClassValue(
                        bestClass,
                        Arrays.stream(bestClass.getRecordComponents()).map(RecordComponent::getAccessor).toList(),
                        config.customMappings().containsKey(bestClass)
                        ? config.customMappings().get(bestClass).apply(node)
                        : buildFromClass(node, bestClass, parameterizedType)))
            .sorted(withNullValuesLast.thenComparing(numberOfNonNullComponents.reversed()))
            .findFirst()
            .map(ClassValue::value)
            .map(cls::cast)
            .orElse(null);
    }

    private List<Class<?>> permittedRecordClassesOfSealedInterfaceHierarchy(Class<?> cls) {

        boolean resolvedAllClasses = false;
        List<Class<?>> permittedRecordSubclasses = new ArrayList<>();
        List<Class<?>> sealedInterfaces = List.of(cls);

        while (! resolvedAllClasses) {
            var subClasses = sealedInterfaces.stream().flatMap(sealed -> Arrays.stream(sealed.getPermittedSubclasses())).toList();
            var recordClasses = subClasses.stream().filter(Class::isRecord).toList();
            var subSealedInterfaces = subClasses.stream().filter(Class::isSealed).filter(Class::isInterface).toList();
            permittedRecordSubclasses.addAll(recordClasses);
            if (subSealedInterfaces.isEmpty()) {
                resolvedAllClasses = true;
            } else {
                sealedInterfaces = subSealedInterfaces;
            }
        }
        return permittedRecordSubclasses;
    }

    private <T> T buildFromClass(YayNode node, Class<T> cls, Optional<ParameterizedType> parameterizedType) {

        if (config.customMappings().containsKey(cls)) {
            var f = config.customMappings().get(cls);
            return cls.cast(f.apply(node));
        }

        if (cls.isRecord()) {
            T r = fromYayTree(node, cls);
            return r;
        } else if (cls.equals(Optional.class)) {
            if (node != null) {
                ParameterizedType pt = parameterizedType.get();
                Type typeArgument = pt.getActualTypeArguments()[0];
                if (typeArgument instanceof Class<?> typeClass) {
                    var value = buildFromClass(node, typeClass, Optional.empty());
                    @SuppressWarnings("unchecked")
                    T t = (T) Optional.ofNullable(value);
                    return t;
                }
            }

            @SuppressWarnings("unchecked")
            T t = (T) Optional.empty();
            return t;
        } else if (cls.isArray()) {
            Class<?> componentClass = cls.componentType();
            if (parameterizedType.isPresent()) {
                var pt = parameterizedType.get();
                componentClass = (Class<?>) pt.getActualTypeArguments()[0];
            }
            var array = buildArray(node, componentClass, cls);
            return cls.cast(array);
        } else if (List.class.isAssignableFrom(cls)) {
            Class<?> componentClass = Object.class;
            if (parameterizedType.isPresent()) {
                var pt = parameterizedType.get();
                try {
                    componentClass = (Class<?>) pt.getActualTypeArguments()[0];
                } catch (ClassCastException cce) {}
            }
            var list = buildList(node, componentClass);
            return cls.cast(list);
        } else if (Map.class.isAssignableFrom(cls)) {
            if (node instanceof YayObject yo) {
                if (parameterizedType.isPresent()) {
                    var map = yo.value().entrySet().stream().collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> {
                                    var ptValueClass = (Class<?>) parameterizedType.get().getActualTypeArguments()[1];
                                    if (ptValueClass.isInterface() && ptValueClass.isSealed()) {
                                        return buildFromSealedInterface(node, ptValueClass, Optional.empty());
                                    } else {
                                        return buildFromClass(e.getValue(), ptValueClass, Optional.empty());
                                    }
                                }
                                ));
                    return cls.cast(map);
                }
            }
            return cls.cast(Map.of());
        } else if (cls.isEnum()) {
            if (node instanceof YayString string) {
                for (var constant : cls.getEnumConstants()) {
                    if (String.valueOf(constant).equals(string.value())) {
                        return constant;
                    }
                }
                return null;
            } else if (node instanceof YayNumber number) {
                try {
                    var mh = MethodHandles.lookup().findStatic(cls, "valueOf", MethodType.methodType(cls, int.class));
                    var o = mh.invoke(number.value().intValue());
                    return cls.cast(o);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            return null;
        } else {
            if (node instanceof YayNumber number) {
                if (ZonedDateTime.class.equals(cls)) {
                    ZonedDateTime zdt = Util.fromLong(number.value().longValue());
                    return cls.cast(zdt);
                }
            }

            if (node instanceof YayString string) {
                if (java.util.Objects.equals(URI.class, cls)) {
                    var uri = URI.create(string.value());
                    return cls.cast(uri);
                }
                if (ZonedDateTime.class.equals(cls)) {
                    ZonedDateTime zdt = ZonedDateTime.parse(string.value(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    return cls.cast(zdt);
                }
             }

            T t = buildFromPrimitive(node, cls);
            return t;
        }
    }

    private <T> T buildArray(YayNode node, Class<?> componentClass, Class<T> arrayClass) {
        var list = buildList(node, componentClass);
        int size = list.size();
        var array = Array.newInstance(componentClass, size);
        for (int i = 0; i < size; i++) {
            Array.set(array, i, list.get(i));
        }
        return arrayClass.cast(array);
    }

    private <T> List<T> buildList(YayNode node, Class<? extends T> cls) {
        List<T> list = new ArrayList<>();
        if (node instanceof YayArray ya) {
            ya.value().stream().forEach(yn -> {
                if (cls.isInterface() && cls.isSealed()) {
                    var o = buildFromSealedInterface(yn, cls, Optional.empty());
                    list.add(cls.cast(o));
                } else {
                    var o = buildFromClass(yn, cls, Optional.empty());
                    list.add(o);
                }
            });
        }
        return list;
    }

    // Hiding this hideous thing here at the bottom,
    // where nobody will see it... Hmm, but then who is reading this comment?
    private <T> T buildFromPrimitive(YayNode node, Class<T> cls) {
        Object o;
        if (node instanceof YayValue yv) {
            if (yv instanceof YayNumber yn) {
                Number number = yn.value();
                if (cls == Short.class) {
                    o = number.shortValue();
                } else if (cls == Integer.class) {
                    o = number.intValue();
                } else if (cls == Long.class) {
                    o = number.longValue();
                } else if (cls == Float.class) {
                    o = number.floatValue();
                } else if (cls == Double.class) {
                    o = number.doubleValue();
                } else {
                    o = number;
                }
            } else if (yv instanceof YayString string) {
                o = string.value();
            } else if (yv instanceof YayBool bool) {
                o = bool.value();
            } else if (yv instanceof YayNull ynull) {
                o = null;
            } else {
                o = null;
            }
        } else {
            if (cls.isPrimitive()) {
                if (cls == boolean.class) {
                    o = false;
                } else if (cls == char.class) {
                    o = '\u0000';
                } else if (cls == byte.class) {
                    o = (byte) 0;
                } else if (cls == short.class) {
                    o = (short) 0;
                } else if (cls == int.class) {
                    o = (int) 0;
                } else if (cls == long.class) {
                    o = (long) 0;
                } else if (cls == float.class) {
                    o = (float) 0;
                } else if (cls == double.class) {
                    o = (double) 0;
                } else {
                    o = null;
                }
            } else {
                o = null;
            }
        }

        if (o != null) {
            if (cls.isPrimitive()) {
                @SuppressWarnings("unchecked")
                T t = (T) o;
                return t;
            } else {
                T t = cls.cast(o);
                return t;
            }
        }
        return null;
    }
}

