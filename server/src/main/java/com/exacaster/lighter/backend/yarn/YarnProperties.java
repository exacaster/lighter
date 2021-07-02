package com.exacaster.lighter.backend.yarn;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;

@ConfigurationProperties("lighter.yarn")
@Requires(property="lighter.yarn.enabled", value = "true")
public class YarnProperties {

}
