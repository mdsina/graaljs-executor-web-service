package com.github.mdsina.graaljs.executorwebservice.execution;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.cache.SourceCache;
import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverter;
import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverterProxy;
import com.github.mdsina.graaljs.executorwebservice.logging.SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder;
import com.github.mdsina.graaljs.executorwebservice.spring.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptSourceExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ScriptExecutionScope scope;
    private final ExecutionScopeDataBridge dataBridge;
    private final JsonConverterProxy jsonConverterProxy;
    private final SourceCache sourceCache;
    private final ObjectFactory<Context> contextObjectFactory;
    private final SLF4JOutputStreamBridgeBuilder outputStreamBuilder;
    private final SLF4JOutputStreamBridgeBuilder errorStreamBuilder;
    private final ObjectMapper objectMapper;

    public JavaScriptSourceExecutor(
        ScriptExecutionScope scope,
        ExecutionScopeDataBridge dataBridge,
        JsonConverterProxy jsonConverterProxy,
        SourceCache sourceCache,
        ObjectFactory<Context> contextObjectFactory,
        @Qualifier("jsOutputStreamBuilder") SLF4JOutputStreamBridgeBuilder outputStreamBuilder,
        @Qualifier("jsErrorStreamBuilder") SLF4JOutputStreamBridgeBuilder errorStreamBuilder,
        ObjectMapper objectMapper
    ) {
        this.scope = scope;
        this.dataBridge = dataBridge;
        this.jsonConverterProxy = jsonConverterProxy;
        this.sourceCache = sourceCache;
        this.contextObjectFactory = contextObjectFactory;
        this.outputStreamBuilder = outputStreamBuilder;
        this.errorStreamBuilder = errorStreamBuilder;
        this.objectMapper = objectMapper;
    }

    public JsExecutionResult execute(String scriptName, String body, List<Variable> inputs) throws Exception {
        String activationId = scope.activate();

        try {
            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();

            dataBridge.setInputs(inputs);
            outputStreamBuilder.addConsumer(outputBuilder::append);
            errorStreamBuilder.addConsumer(errorBuilder::append);

            Source source = sourceCache.getSource(scriptName, body);
            String json;

            try (Context context = contextObjectFactory.getObject()) {
                jsonConverterProxy.setJsonConverter(new JsonConverter(context.getBindings("js").getMember("JSON")));

                context.eval(source);
                context.getBindings("js").getMember("callFunction").executeVoid();
                logger.trace("{}.callFunction called", activationId);
                json = objectMapper.writeValueAsString(dataBridge.getOutputs());
            }

            List<Variable> outputs = objectMapper.readValue(json, new TypeReference<List<Variable>>() {});

            return new JsExecutionResult(
                outputs,
                outputBuilder.toString(),
                errorBuilder.toString()
            );
        } finally {
            scope.deactivate();
        }
    }
}
