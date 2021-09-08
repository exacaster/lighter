package com.exacaster.lighter.backend.kubernetes;

import static io.micronaut.core.convert.format.MapFormat.MapTransformation.FLAT;
import static io.micronaut.core.naming.conventions.StringConvention.RAW;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.format.MapFormat;
import java.util.Map;
import java.util.StringJoiner;

@ConfigurationProperties("lighter.kubernetes")
@Requires(property="lighter.kubernetes.enabled", value = "true")
public class KubernetesProperties{

    private final String namespace;
    private final Integer maxLogSize;
    private final String master;
    private final Map<String, String> submitProps;

    @ConfigurationInject
    public KubernetesProperties(String namespace, Integer maxLogSize, String master,
            @MapFormat(transformation = FLAT, keyFormat = RAW)
            Map<String, String> submitProps) {
        this.namespace = namespace;
        this.maxLogSize = maxLogSize;
        this.master = master;
        this.submitProps = submitProps;
    }

    public String getNamespace() {
        return namespace;
    }

    public Integer getMaxLogSize() {
        return maxLogSize;
    }

    public String getMaster() {
        return master;
    }

    public Map<String, String> getSubmitProps() {
        return submitProps;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", KubernetesProperties.class.getSimpleName() + "[", "]")
                .add("namespace='" + namespace + "'")
                .add("maxLogSize=" + maxLogSize)
                .add("master=" + master)
                .toString();
    }
}
