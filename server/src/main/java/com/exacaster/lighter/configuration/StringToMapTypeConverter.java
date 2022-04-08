package com.exacaster.lighter.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class StringToMapTypeConverter implements TypeConverter<String, Map<String, String>> {
    private final ObjectMapper mapper;

    public StringToMapTypeConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Optional<Map<String, String>> convert(String object, Class<Map<String, String>> targetType, ConversionContext context) {
        try {
            return Optional.of(mapper.readValue(object, targetType));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }
}
