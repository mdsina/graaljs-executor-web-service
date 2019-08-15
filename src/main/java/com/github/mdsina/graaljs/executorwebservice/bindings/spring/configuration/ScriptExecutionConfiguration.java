package com.github.mdsina.graaljs.executorwebservice.bindings.spring.configuration;

import java.math.BigDecimal;
import java.util.List;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScriptExecutionConfiguration {

    @Bean
    public HostAccess hostAccess() {
        return HostAccess.newBuilder(HostAccess.ALL)
            // https://github.com/graalvm/graaljs/issues/165#issuecomment-493926312
            .targetTypeMapping(Long.class, Object.class, null, v -> v)
            // converted to BigDecimal because do not need to handle representation conversion from scientific
            .targetTypeMapping(Double.class, Object.class, null, BigDecimal::valueOf)
            .targetTypeMapping(List.class, Object.class, null, v -> v)
            .build();
    }

    @Bean
    public Engine getEngine() {
        return Engine.create();
    }
}
