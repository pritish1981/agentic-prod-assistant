package com.bofa.agentic.util;

import java.util.Objects;

import com.bofa.agentic.exception.AgentException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/*
 *Below is a production-grade JsonUtils class — not a basic ObjectMapper wrapper.

This is designed the way enterprise backend platforms structure JSON utilities.

You get:

✅ Thread-safe ObjectMapper
✅ High-performance configuration
✅ Java Time support
✅ Fail-safe parsing
✅ Exception-safe methods
✅ Generic type support
✅ Logging ready
✅ Works perfectly with Spring Boot
✅ Handles AI payloads safely 
 * 
 */

public final class JsonUtils {
	
	private static final ObjectMapper MAPPER = createMapper();

    private JsonUtils() {
        // Prevent instantiation
    }

    private static ObjectMapper createMapper() {

        ObjectMapper mapper = new ObjectMapper();

        // Java 8+ date/time support
        mapper.registerModule(new JavaTimeModule());

        // Do NOT fail on unknown fields (critical for LLM responses)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Allow empty objects
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // Pretty useful for debugging AI payloads
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        return mapper;
    }

    // ==============================
    // Serialize
    // ==============================

    public static String toJson(Object object) {

        try {
            return MAPPER.writeValueAsString(object);

        } catch (JsonProcessingException ex) {

            throw new AgentException(
                    "JSON_SERIALIZATION_ERROR",
                    "Failed to serialize object to JSON",
                    false
            );
        }
    }

    // ==============================
    // Deserialize
    // ==============================

    public static <T> T fromJson(String json, Class<T> clazz) {

        try {
            return MAPPER.readValue(json, clazz);

        } catch (Exception ex) {

            throw new AgentException(
                    "JSON_DESERIALIZATION_ERROR",
                    "Failed to deserialize JSON to " + clazz.getSimpleName(),
                    false
            );
        }
    }

    // ==============================
    // Deserialize Generic Types
    // (VERY important for Lists / Maps)
    // ==============================

    public static <T> T fromJson(String json, TypeReference<T> typeRef) {

        try {
            return MAPPER.readValue(json, typeRef);

        } catch (Exception ex) {

            throw new AgentException(
                    "JSON_DESERIALIZATION_ERROR",
                    "Failed to deserialize generic JSON type",
                    false
            );
        }
    }

    // ==============================
    // Safe Parse (returns null)
    // Useful for non-critical AI payloads
    // ==============================

    public static <T> T tryFromJson(String json, Class<T> clazz) {

        try {
            return MAPPER.readValue(json, clazz);

        } catch (Exception ex) {
            return null;
        }
    }

    // ==============================
    // Convert Object → Another Type
    // VERY useful in planner outputs
    // ==============================

    public static <T> T convert(Object source, Class<T> clazz) {

        Objects.requireNonNull(source, "Source object cannot be null");

        try {
            return MAPPER.convertValue(source, clazz);

        } catch (IllegalArgumentException ex) {

            throw new AgentException(
                    "JSON_CONVERSION_ERROR",
                    "Failed to convert object to " + clazz.getSimpleName(),
                    false
            );
        }
    }

    // ==============================
    // Expose Mapper (advanced usage)
    // ==============================

    public static ObjectMapper mapper() {
        return MAPPER;
    }

}
