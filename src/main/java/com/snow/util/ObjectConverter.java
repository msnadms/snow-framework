package com.snow.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ObjectConverter {

    private static final Map<Class<?>, Function<String, Object>> CONVERTERS;

    static {
        CONVERTERS = new HashMap<>();
        CONVERTERS.put(Integer.class, Integer::parseInt);
        CONVERTERS.put(Double.class, Double::parseDouble);
        CONVERTERS.put(Float.class, Float::parseFloat);
        CONVERTERS.put(Long.class, Long::parseLong);
        CONVERTERS.put(Boolean.class, Boolean::parseBoolean);
        CONVERTERS.put(String.class, (s) -> s);
    }

    private ObjectConverter() {}

    public static Object convert(String value, Class<?> type) {
        var converter = CONVERTERS.get(type);
        if (type.isEnum()) {
            // Safe because type.isEnum() is true, so type always extends Enum class
            @SuppressWarnings({ "unchecked", "rawtypes" })
            var result = Enum.valueOf((Class<? extends Enum>) type, value);
            return result;
        }
        if (converter == null) {
            throw new IllegalArgumentException("Unsupported parameter type: " + type);
        }
        try {
            return converter.apply(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(value + " is not a valid " + type, e);
        }
    }
}
