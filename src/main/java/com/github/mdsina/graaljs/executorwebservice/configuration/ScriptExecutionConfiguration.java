package com.github.mdsina.graaljs.executorwebservice.configuration;

import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProvider;
import com.github.mdsina.graaljs.executorwebservice.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.context.ScriptScopeBeanFactoryPostProcessor;
import com.github.mdsina.graaljs.executorwebservice.execution.ContextWrapper;
import com.github.mdsina.graaljs.executorwebservice.logging.SLF4JOutputStreamBridge;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
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
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.slf4j.event.Level;
import org.springframework.beans.factory.ObjectFactory;
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
    public static class ExecConfigurationProps {

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

    @Bean("jsOutputStreamBuilder")
//    @com.github.mdsina.graaljs.executorwebservice.context.annotation.ScriptExecutionScope
    public SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder outputStreamBuilder() {
        return SLF4JOutputStreamBridge.newBuilder().logLevel(Level.DEBUG);
    }

    @Bean("jsErrorStreamBuilder")
//    @com.github.mdsina.graaljs.executorwebservice.context.annotation.ScriptExecutionScope
    public SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder errorStreamBuilder() {
        return SLF4JOutputStreamBridge.newBuilder().logLevel(Level.DEBUG);
    }

    @Bean("jsOutputStream")
//    @com.github.mdsina.graaljs.executorwebservice.context.annotation.ScriptExecutionScope
    public SLF4JOutputStreamBridge outputStream(
        @Qualifier("jsOutputStreamBuilder") SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder builder
    ) {
        return builder.build();
    }

    @Bean("jsErrorStream")
//    @com.github.mdsina.graaljs.executorwebservice.context.annotation.ScriptExecutionScope
    public SLF4JOutputStreamBridge errorStream(
        @Qualifier("jsErrorStreamBuilder") SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder builder
    ) {
        return builder.build();
    }

    @Bean
    @Scope("prototype")
    public Context getContext(
        Set<BindingsProvider> bindingsProviders,
        Engine engine,
        HostAccess hostAccess,
        @Qualifier("jsOutputStream") OutputStream outputStream,
        @Qualifier("jsErrorStream") OutputStream errorStream
    ) {
        Context context = Context.newBuilder("js")
            .allowAllAccess(true)
            .allowHostAccess(hostAccess)
            .out(outputStream)
            .err(errorStream)
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
        ObjectFactory<Context> contextObjectFactory
    ) {
        return new BasePooledObjectFactory<>() {
            @Override
            public ContextWrapper create() {
                return getContextWrapper(contextObjectFactory.getObject());
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
        ExecConfigurationProps props
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
