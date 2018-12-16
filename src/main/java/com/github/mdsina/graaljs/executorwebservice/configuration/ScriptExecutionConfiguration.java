package com.github.mdsina.graaljs.executorwebservice.configuration;

import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProvider;
import com.github.mdsina.graaljs.executorwebservice.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.context.ScriptScopeBeanFactoryPostProcessor;
import com.github.mdsina.graaljs.executorwebservice.execution.ContextWrapper;
import java.util.Set;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
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
    @Scope("prototype")
    public Context getContext(Set<BindingsProvider> bindingsProviders, Engine engine) {
        Context context = Context.newBuilder("js")
            .allowAllAccess(true)
            .engine(engine)
            .build();
        Value bindings = context.getBindings("js");
        bindingsProviders.forEach(o -> o.setBindings(bindings));

        return context;
    }

    @Bean
    @Scope("prototype")
    public ContextWrapper getContextWrapper(Context context) {
        return new ContextWrapper(context);
    }

    @Bean(name = "graalObjectFactory")
    public PooledObjectFactory<ContextWrapper> scriptEnginePooledObjectFactory(
        Set<BindingsProvider> bindingsProviders, Engine engine
    ) {
        return new BasePooledObjectFactory<>() {
            @Override
            public ContextWrapper create() {
                return getContextWrapper(getContext(bindingsProviders, engine));
            }

            @Override
            public PooledObject<ContextWrapper> wrap(ContextWrapper obj) {
                return new DefaultPooledObject<>(obj);
            }
        };
    }

    @Bean(name = "graalObjectPool")
    public ObjectPool<ContextWrapper> scriptEngineObjectPool(
        @Qualifier("graalObjectFactory") PooledObjectFactory<ContextWrapper> factory,
        NashornConfigurationProps props
    ) throws Exception {
        GenericObjectPoolConfig<ContextWrapper> config = new GenericObjectPoolConfig<>();

        config.setMaxTotal(props.getPoolSize());
        config.setMaxIdle(props.getPoolSize());
        config.setMinIdle(props.getPoolSize());
        config.setJmxEnabled(false); // TODO: configure jmx into pool

        GenericObjectPool<ContextWrapper> pool = new GenericObjectPool<>(factory, config);
        pool.preparePool();

        return pool;
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
