package com.github.mdsina.graaljs.executorwebservice.configuration;

import com.github.mdsina.graaljs.executorwebservice.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.context.ScriptScopeBeanFactoryPostProcessor;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.graalvm.polyglot.Engine;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Configuration
public class ScriptExecutionConfiguration {

    @Component
    @Validated
    @EnableConfigurationProperties
    @ConfigurationProperties("script.execution")
    public class NashornConfigurationProps {

        @NotNull
        @Min(1L)
        private int poolSize;

        public int getPoolSize() {
            return poolSize;
        }

        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }
    }

    @Bean
    public Engine getEngine() {
        return Engine.create();
    }

    @Bean
    public RandomDataGenerator randomDataGenerator() {
        return new RandomDataGenerator();
    }

    @Bean
    public ScriptScopeBeanFactoryPostProcessor scriptScopeBeanFactoryPostProcessor() {
        return new ScriptScopeBeanFactoryPostProcessor();
    }

    @DependsOn("scriptScopeBeanFactoryPostProcessor")
    @Bean
    public ScriptExecutionScope scriptExecutionScope(ConfigurableListableBeanFactory factory) {
        return (ScriptExecutionScope) factory.getRegisteredScope(ScriptExecutionScope.ID);
    }
}
