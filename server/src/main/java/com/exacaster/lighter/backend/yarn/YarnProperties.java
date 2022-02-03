package com.exacaster.lighter.backend.yarn;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import java.util.StringJoiner;

@ConfigurationProperties("lighter.yarn")
@Requires(property="lighter.yarn.enabled", value = "true")
public class YarnProperties {

    private final String kerberosPrincipal;
    private final String kerberosKeytab;

    @ConfigurationInject
    public YarnProperties(@Nullable String kerberosPrincipal, @Nullable String kerberosKeytab) {
        this.kerberosPrincipal = kerberosPrincipal;
        this.kerberosKeytab = kerberosKeytab;
    }

    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }

    public String getKerberosKeytab() {
        return kerberosKeytab;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", YarnProperties.class.getSimpleName() + "[", "]")
                .add("kerberosPrincipal='" + kerberosPrincipal + "'")
                .add("kerberosKeytab='" + kerberosKeytab + "'")
                .toString();
    }
}
