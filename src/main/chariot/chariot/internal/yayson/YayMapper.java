package chariot.internal.yayson;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import chariot.internal.yayson.Parser.YayNode;
import chariot.internal.yayson.Parser.YayNode.*;
import chariot.internal.yayson.Parser.YayNode.YayValue.*;

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
        var node = Parser.fromString(json);

        // Here be dragons...
        T t = fromYayTree(node, cls);
        return t;
    }

    public <T> T fromYayTree(YayNode node, Class<T> cls) {

        if (cls.isInterface() && cls.isSealed()) {

            return buildFromSealedInterface(node, cls, Optional.empty());

        } else if (cls.isRecord()) {

            if (config.customMappings().containsKey(cls)) {
                var f = config.customMappings().get(cls);
                return cls.cast(f.apply(node));
            }

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
                            System.out.println("Failure while looking for " + jsonName + " of type " +
                                    rc.getType() + " with (possibly) parameterized type " + parameterizedType);
                            e.printStackTrace();
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
        return null;
    }

    private <T> T buildFromSealedInterface(YayNode node, Class<T> cls, Optional<ParameterizedType> parameterizedType) {

        if (node instanceof YayEmpty empty) {
            var opt = Arrays.stream(cls.getPermittedSubclasses())
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

        record ClassFieldMatches(Class<?> c, Long count) {}
        var bestMatch = Arrays.stream(cls.getPermittedSubclasses())
            .map(c -> new ClassFieldMatches(c,
                        Arrays.stream(c.getRecordComponents())
                        .map(rc -> rc.getName())
                        .map(name -> config.fieldMappings().getOrDefault(c, Map.of()).getOrDefault(name, name))
                        .filter(jsonFieldNames::contains)
                        .collect(Collectors.counting()))
                )
            .max((cfm1, cfm2) -> Long.compare(cfm1.count(), cfm2.count()));


        Object result = null;
        if (bestMatch.isPresent()) {
            var bestMatchClass = bestMatch.get().c();
            if (config.customMappings().containsKey(bestMatchClass)) {
                var f = config.customMappings().get(bestMatchClass);
                result = f.apply(node);
            } else {
                result = buildFromClass(node, bestMatchClass, parameterizedType);
            }
        } else {
            return null;
        }
        return cls.cast(result);
    }

    private <T> T buildFromClass(YayNode node, Class<T> cls, Optional<ParameterizedType> parameterizedType) {
        if (cls.isRecord()) {
            T r = fromYayTree(node, cls);
            return r;
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
            if (node instanceof YayValue.YayString string) {
                try {
                    var mh = MethodHandles.lookup().findStatic(cls, "valueOf", MethodType.methodType(cls, String.class));
                    var o = mh.invoke(string.value());
                    return cls.cast(o);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            } else if (node instanceof YayValue.YayNumber number) {
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
    // Will look better when Project Valhalla arrives with primitive classes
    // (but possibly before that, when I feel _too_ uncomfortable with knowing
    // this stuff resides here...)
    @SuppressWarnings("unchecked")
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
                return (T) o;
            } else {
                T t = cls.cast(o);
                return t;
            }
        }
        return null;
    }
}

