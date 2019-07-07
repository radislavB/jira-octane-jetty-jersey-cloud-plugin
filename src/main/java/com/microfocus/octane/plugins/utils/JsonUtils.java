package com.microfocus.octane.plugins.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public class JsonUtils {

    public static Map parseToMap(String jsonStr) {
        return parse(jsonStr, Map.class);
    }

    public static <T> T parse(String content, Class<T> valueType) {
        final ObjectMapper mapper = new ObjectMapper();

        try {
            T value = mapper.readValue(content, valueType);
            return value;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse :" + e.getMessage(), e);
        }
    }

    public static String toJson(Object obj) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert to Json :" + e.getMessage(), e);
        }

    }
}
