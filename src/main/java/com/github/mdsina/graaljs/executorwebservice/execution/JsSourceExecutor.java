package com.github.mdsina.graaljs.executorwebservice.execution;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProviderFactory;
import com.github.mdsina.graaljs.executorwebservice.bindings.context.ContextFactory;
import com.github.mdsina.graaljs.executorwebservice.cache.SourceCache;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverter;
import com.github.mdsina.graaljs.executorwebservice.logging.SLF4JOutputStreamBridge;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class JsSourceExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final SourceCache sourceCache;
    private final ContextFactory contextFactory;
    private final ObjectMapper objectMapper;
    private final BindingsProviderFactory bindingsProviderFactory;
    private final JsDebugTaskWorker jsDebugTaskWorker;

    public String executeDebug(String scriptName, String body, List<Variable> inputs) {
        return jsDebugTaskWorker.executeDebugTask(uuid -> () -> {
            try {
                return executeOnContext(
                    sourceCache.getSource(scriptName, body),
                    inputs,
                    (outputStream, errorStream) -> contextFactory.getDebugContext(outputStream, errorStream, uuid)
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public JsExecutionResult execute(String scriptName, String body, List<Variable> inputs) throws IOException {
        return executeOnContext(
            sourceCache.getSource(scriptName, body),
            inputs,
            contextFactory::getContext
        );
    }

    private JsExecutionResult executeOnContext(
        Source source,
        List<Variable> inputs,
        BiFunction<OutputStream, OutputStream, Context> contextFactory
    ) throws IOException {

        var outputBuilder = new StringBuilder();
        var errorBuilder = new StringBuilder();

        var outputStreamBridge = SLF4JOutputStreamBridge.newBuilder()
            .logLevel(Level.INFO)
            .addConsumer(outputBuilder::append)
            .build();

        var errorStreamBridge = SLF4JOutputStreamBridge.newBuilder()
            .logLevel(Level.INFO)
            .addConsumer(errorBuilder::append)
            .build();

        String json;

        try (Context context = contextFactory.apply(outputStreamBridge, errorStreamBridge)) {
            var dataBridge = new ScriptDataBridge(
                new JsonConverter(context.getBindings("js").getMember("JSON")),
                objectMapper
            );
            dataBridge.setInputs(inputs);

            var bindings = context.getBindings("js");
            var bindingsProviders = bindingsProviderFactory.getBindingsProviders(dataBridge);
            bindingsProviders.forEach(o -> o.setBindings(bindings));

            context.eval(source);
            context.getBindings("js").getMember("callFunction").executeVoid();

            logger.trace("{}.callFunction called", source.getName());

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
