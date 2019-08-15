package com.github.mdsina.graaljs.executorwebservice.execution;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProvider;
import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProviderFactory;
import com.github.mdsina.graaljs.executorwebservice.bindings.context.ContextFactory;
import com.github.mdsina.graaljs.executorwebservice.cache.SourceCache;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverter;
import com.github.mdsina.graaljs.executorwebservice.logging.SLF4JOutputStreamBridge;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptSourceExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SourceCache sourceCache;
    private final ContextFactory contextObjectFactory;
    private final ObjectMapper objectMapper;
    private final BindingsProviderFactory bindingsProviderFactory;

    public JavaScriptSourceExecutor(
        SourceCache sourceCache,
        ContextFactory contextObjectFactory,
        ObjectMapper objectMapper,
        BindingsProviderFactory bindingsProviderFactory
    ) {
        this.sourceCache = sourceCache;
        this.contextObjectFactory = contextObjectFactory;
        this.objectMapper = objectMapper;
        this.bindingsProviderFactory = bindingsProviderFactory;
    }

    public JsExecutionResult execute(String scriptName, String body, List<Variable> inputs) throws Exception {
        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder errorBuilder = new StringBuilder();

        SLF4JOutputStreamBridge outputStreamBridge = SLF4JOutputStreamBridge.newBuilder()
            .logLevel(Level.DEBUG)
            .addConsumer(outputBuilder::append)
            .build();

        SLF4JOutputStreamBridge errorStreamBridge = SLF4JOutputStreamBridge.newBuilder()
            .logLevel(Level.DEBUG)
            .addConsumer(errorBuilder::append)
            .build();

        Source source = sourceCache.getSource(scriptName, body);
        String json;

        try (Context context = contextObjectFactory.getContext(outputStreamBridge, errorStreamBridge)) {
            ScriptDataBridge dataBridge = new ScriptDataBridge(
                new JsonConverter(context.getBindings("js").getMember("JSON")),
                objectMapper
            );
            dataBridge.setInputs(inputs);

            Value bindings = context.getBindings("js");

            List<BindingsProvider> bindingsProviders = bindingsProviderFactory.getBindingsProviders(dataBridge);
            bindingsProviders.forEach(o -> o.setBindings(bindings));

            context.eval(source);
            context.getBindings("js").getMember("callFunction").executeVoid();
            logger.trace("{}.callFunction called", scriptName);
            json = objectMapper.writeValueAsString(dataBridge.getOutputs());
        }

        List<Variable> outputs = objectMapper.readValue(json, new TypeReference<List<Variable>>() {});

        return new JsExecutionResult(
            outputs,
            outputBuilder.toString(),
            errorBuilder.toString()
        );
    }
}
