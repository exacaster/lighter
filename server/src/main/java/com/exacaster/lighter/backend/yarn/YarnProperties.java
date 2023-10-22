package com.exacaster.lighter.backend.yarn;

import io.micronaut.context.annotation.ConfigurationInject;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;

@ConfigurationProperties("lighter.yarn")
@Requires(property="lighter.yarn.enabled", value = "true")
public class YarnProperties {

    private final KerberosProperties kerberos;

    @ConfigurationInject
    public YarnProperties(@Nullable KerberosProperties kerberos) {
        this.kerberos = kerberos;
    }

    @ConfigurationProperties("kerberos")
    public KerberosProperties getKerberos() {
        return kerberos;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("kerberos", kerberos)
                .toString();
    }

    @ConfigurationProperties("kerberos")
    public static class KerberosProperties {

        private final String principal;
        private final String keytab;

        @ConfigurationInject
        public KerberosProperties(@NotBlank String principal, @NotBlank String keytab) {
            this.principal = principal;
            this.keytab = keytab;
        }

        public String getPrincipal() {
            return principal;
        }

        public String getKeytab() {
            return keytab;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("principal", principal)
                    .append("keytab", keytab)
                    .toString();
        }
    }
}
