package com.snow.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class JsonUtil {

    private static final ObjectMapper mapper =
            new ObjectMapper().setVisibility(
                    PropertyAccessor.FIELD,
                    JsonAutoDetect.Visibility.ANY
            );

    public static <T> T deserialize(InputStream in, Class<T> clazz) throws IOException {
        return mapper.readValue(in, clazz);
    }

    public static String serialize(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

}
