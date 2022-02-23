package com.exacaster.lighter.configuration;

import com.exacaster.lighter.configuration.AppConfiguration.PermanentSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

@Singleton
public class PermanentSessionTypeConverter implements TypeConverter<Map, PermanentSession> {

    private final ObjectMapper mapper;

    public PermanentSessionTypeConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * When deserializing configuration, Micronaut by default converts config properties with minus (-) sign
     * to camel case values. In that case `spark.kubernetes.driver.secrets.spark-secret` key becomes
     * `spark.kubernetes.driver.secrets.sparkSecret` that is unexpected behaviour.
     * This converter is a workaround for this problem.
     */
    @Override
    public Optional<PermanentSession> convert(Map object, Class<PermanentSession> targetType, ConversionContext context) {
       return Optional.of(mapper.convertValue(object, PermanentSession.class));
    }
}
