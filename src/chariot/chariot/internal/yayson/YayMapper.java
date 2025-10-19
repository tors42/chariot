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
                return cls.cast(config.customMappings().get(cls).apply(node));
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
            return _fromYayTree(node, cls);
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
            return null;
        }
    }

    public <T> T _fromYayTree(YayNode node, Class<T> cls) throws Exception {
        if (config.customMappings().containsKey(cls)) {
            return cls.cast(config.customMappings().get(cls).apply(node));
        }

        if (node == null) return null;

        if (cls.isInterface() && cls.isSealed()) {
            return buildFromSealedInterface(node, cls, null);
        } else if (cls.isRecord() && node instanceof YayObject yo) {
            List<RecordComponent> recordComponents = Arrays.asList(cls.getRecordComponents());

            List<?> recordComponentValues = mapValuesForComponents(recordComponents, cls, yo);

            T recordInstance;
            try {
                // Create the instance!
                recordInstance = cls.cast(findConstructor(cls, recordComponents).newInstance(recordComponentValues.toArray()));
            } catch (InstantiationException |
                    IllegalAccessException |
                    IllegalArgumentException |
                    InvocationTargetException e) {
                e.printStackTrace(System.err);
                System.err.println(" ########  Instances: " + recordComponentValues);
                System.err.println(" ========  node     : " + node);
                recordInstance = null;
            }
            return recordInstance;
        }
        return null;
    }

    private Constructor<?> findConstructor(Class<?> cls, List<RecordComponent> recordComponents) {
        var declaredCtors = cls.getDeclaredConstructors();
        var ctor = declaredCtors[0];
        if (declaredCtors.length > 1) {
            var types = recordComponents.stream()
                .map(RecordComponent::getType)
                .toList();
            ctor = Arrays.stream(declaredCtors)
                .filter(c -> Arrays.asList(c.getParameterTypes()).equals(types))
                .findAny()
                .orElseThrow();
        }
        return ctor;
    }

    private List<Object> mapValuesForComponents(List<RecordComponent> recordComponents, Class<?> cls, YayObject yo) {
        return recordComponents.stream()
            .map(rc -> {
                // See if we need to swap the json property name to a java friendly name
                // json: { "int": 5 }
                // java: int int = 5; <- not ok
                // java: int intVariable = 5; <- "ok"
                // So json "int" -> java "intVariable" mapping
                var jsonName = config.fieldMappings().getOrDefault(cls, Map.of())
                    .getOrDefault(rc.getName(), rc.getName());


                // Could for instance be the field "opening" in Game, of type Game$Opening,
                // which could be missing,
                // or it could for instance be a field "count" of type "int" -> so would map to 0 if missing...
                YayNode yayNodeForField = yo.value().get(jsonName);

                var parameterizedType = rc.getGenericType() instanceof ParameterizedType pt
                    ? pt : null;

                try {
                    return rc.getType().isInterface() && rc.getType().isSealed()
                        ? buildFromSealedInterface(yayNodeForField, rc.getType(), parameterizedType)
                        : buildFromClass(yayNodeForField, rc.getType(), parameterizedType);
                } catch (Exception e) {
                    System.err.println("Failure while looking for " + jsonName + " of type " +
                            rc.getType() + " with (possibly) parameterized type " + parameterizedType);
                    System.err.println("""
                            Failed: T fromYayTree(YayNode node, Class<T> cls)
                            e.getMessage(): %s
                            cls.getName(): %s
                            yo:
                            =======================
                            %s
                            =======================
                            """.formatted(e.getMessage(), cls.getName(), yo));
                    e.printStackTrace(System.err);
                }
                return null;
            }).toList();
    }


    private <T> T buildFromSealedInterface(YayNode node, Class<T> cls, ParameterizedType parameterizedType) {

        if (config.customMappings().containsKey(cls)) {
            return cls.cast(config.customMappings().get(cls).apply(node));
        }

        if (cls.equals(Opt.class)) {
            if (node != null && typeClass(parameterizedType) instanceof Class<?> typeClass) {
                var value = buildFromClass(node, typeClass, null);
                @SuppressWarnings("unchecked")
                T t = (T) Opt.of(value);
                return t;
            }
            @SuppressWarnings("unchecked")
            T t = (T) Opt.empty();
            return t;
        }

        if (! (node instanceof YayObject yo
            && yo.value().keySet() instanceof Set<String> jsonFieldNames)) {
            return null;
        }

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

    static Class<?> typeClass(ParameterizedType parameterizedType) {
        return typeClass(parameterizedType, 0);
    }

    static Class<?> typeClass(ParameterizedType parameterizedType, int pos) {
        return parameterizedType != null
                && parameterizedType.getActualTypeArguments() instanceof Type[] arr
                && arr.length >= pos
                && arr[pos] instanceof Class<?> typeClass
                ? typeClass
                : null;
    }

    private <T> T buildFromClass(YayNode node, Class<T> cls, ParameterizedType parameterizedType) {

        if (config.customMappings().containsKey(cls)) {
            return cls.cast(config.customMappings().get(cls).apply(node));
        }

        if (cls.isRecord()) {
            return fromYayTree(node, cls);
        } else if (cls.equals(Optional.class)) {
            if (node != null && typeClass(parameterizedType) instanceof Class<?> typeClass) {
                var value = buildFromClass(node, typeClass, null);
                @SuppressWarnings("unchecked")
                T t = (T) Optional.ofNullable(value);
                return t;
            }
            @SuppressWarnings("unchecked")
            T t = (T) Optional.empty();
            return t;
        } else if (cls.equals(Opt.class)) {
            if (node != null && typeClass(parameterizedType) instanceof Class<?> typeClass) {
                var value = buildFromClass(node, typeClass, null);
                @SuppressWarnings("unchecked")
                T t = (T) Opt.of(value);
                return t;
            }
            @SuppressWarnings("unchecked")
            T t = (T) Opt.empty();
            return t;
        } else if (cls.isArray()) {
            Class<?> componentClass = typeClass(parameterizedType) instanceof Class<?> typeClass
                ? typeClass
                : cls.componentType();
            return cls.cast(buildArray(node, componentClass, cls));
        } else if (List.class.isAssignableFrom(cls)) {
            Class<?> componentClass = typeClass(parameterizedType) instanceof Class<?> typeClass
                ? typeClass
                : Object.class;
            return cls.cast(buildList(node, componentClass));
        } else if (Map.class.isAssignableFrom(cls)) {
            return switch(node) {
                case YayObject yo when typeClass(parameterizedType, 1) instanceof Class<?> mapContentTypeClass
                    -> cls.cast(yo.value().entrySet().stream()
                            .collect(Collectors.toMap(
                                    e -> e.getKey(),
                                    e -> mapContentTypeClass.isInterface() && mapContentTypeClass.isSealed()
                                        ? buildFromSealedInterface(node, mapContentTypeClass, null)
                                        : buildFromClass(e.getValue(), mapContentTypeClass, null)
                                    )));
                case null, default -> cls.cast(Map.of());
            };
        } else if (cls.isEnum()) {
            return switch(node) {
                case YayString(var string) -> Arrays.stream(cls.getEnumConstants())
                    .filter(enumConstant -> String.valueOf(enumConstant).equals(string))
                    .findFirst()
                    .orElse(null);
                case YayNumber(var number) -> {
                    try {
                        yield cls.cast(MethodHandles.lookup()
                            .findStatic(cls, "valueOf", MethodType.methodType(cls, int.class))
                            .invoke(number.intValue()));
                    } catch (Throwable t) {
                        t.printStackTrace(System.err);
                        yield null;
                    }
                }
                case null, default -> null;
            };
        } else {
            return switch (node) {
                case YayNumber(var number) when ZonedDateTime.class.equals(cls)
                    -> cls.cast(Util.fromLong(number.longValue()));
                case YayString(var string) when ZonedDateTime.class.equals(cls)
                    -> cls.cast(ZonedDateTime.parse(string, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
                case YayString(var string) when URI.class.equals(cls)
                    -> cls.cast(URI.create(string));
                case null, default
                    -> buildFromPrimitive(node, cls);
            };
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
        return switch(node) {
            case YayArray(List<YayNode> list) when (cls.isInterface() && cls.isSealed()) -> list.stream()
                .map(yn -> (T) cls.cast(buildFromSealedInterface(yn, cls, null)))
                .toList();
            case YayArray(List<YayNode> list) -> list.stream()
                .map(yn -> (T) buildFromClass(yn, cls, null))
                .toList();
            case null, default -> List.of();
        };
    }

    // Hiding this hideous thing here at the bottom,
    // where nobody will see it... Hmm, but then who is reading this comment?
    private <T> T buildFromPrimitive(YayNode node, Class<T> cls) {
        Object o = switch(node) {
            case YayValue yv -> switch (yv) {
                case YayNumber(var number) -> {
                    if (cls == Short.class) {
                        yield number.shortValue();
                    } else if (cls == Integer.class) {
                        yield number.intValue();
                    } else if (cls == Long.class) {
                        yield number.longValue();
                    } else if (cls == Float.class) {
                        yield number.floatValue();
                    } else if (cls == Double.class) {
                        yield number.doubleValue();
                    } else {
                        yield number;
                    }
                }
                case YayString(var string) -> string;
                case YayBool(var bool)     -> bool;
                case YayNull()             -> null;
                default                    -> null;
            };
            case null, default -> {
                if (cls.isPrimitive()) {
                    if (cls == boolean.class) {
                        yield false;
                    } else if (cls == char.class) {
                        yield '\u0000';
                    } else if (cls == byte.class) {
                        yield (byte) 0;
                    } else if (cls == short.class) {
                        yield (short) 0;
                    } else if (cls == int.class) {
                        yield (int) 0;
                    } else if (cls == long.class) {
                        yield (long) 0;
                    } else if (cls == float.class) {
                        yield (float) 0;
                    } else if (cls == double.class) {
                        yield (double) 0;
                    }
                }
                yield null;
            }
        };
        if (o == null) return null;

        if (cls.isPrimitive()) {
            @SuppressWarnings("unchecked")
            T t = (T) o;
            return t;
        }

        T t = cls.cast(o);
        return t;
    }
}
