package com.github.mdsina.graaljs.executorwebservice.spring.configuration;

import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProvider;
import com.github.mdsina.graaljs.executorwebservice.logging.SLF4JOutputStreamBridge;
import com.github.mdsina.graaljs.executorwebservice.spring.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.spring.context.ScriptScopeBeanFactoryPostProcessor;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Value;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;

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

    @Bean("jsOutputStreamBuilder")
    @com.github.mdsina.graaljs.executorwebservice.spring.context.annotation.ScriptExecutionScope
    public SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder outputStreamBuilder() {
        return SLF4JOutputStreamBridge.newBuilder().logLevel(Level.DEBUG);
    }

    @Bean("jsErrorStreamBuilder")
    @com.github.mdsina.graaljs.executorwebservice.spring.context.annotation.ScriptExecutionScope
    public SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder errorStreamBuilder() {
        return SLF4JOutputStreamBridge.newBuilder().logLevel(Level.DEBUG);
    }

    @Bean("jsOutputStream")
    @com.github.mdsina.graaljs.executorwebservice.spring.context.annotation.ScriptExecutionScope
    public SLF4JOutputStreamBridge outputStream(
        @Qualifier("jsOutputStreamBuilder") SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder builder
    ) {
        return builder.build();
    }

    @Bean("jsErrorStream")
    @com.github.mdsina.graaljs.executorwebservice.spring.context.annotation.ScriptExecutionScope
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
    public RandomDataGenerator randomDataGenerator() {
        return new RandomDataGenerator();
    }

    @Bean
    public ScriptScopeBeanFactoryPostProcessor scriptScopeBeanFactoryPostProcessor(RandomDataGenerator generator) {
        return new ScriptScopeBeanFactoryPostProcessor(generator);
    }

    @DependsOn("scriptScopeBeanFactoryPostProcessor")
    @Bean
    public ScriptExecutionScope scriptExecutionScope(ConfigurableListableBeanFactory factory) {
        return (ScriptExecutionScope) factory.getRegisteredScope(ScriptExecutionScope.ID);
    }
}
