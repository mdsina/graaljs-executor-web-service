package com.github.mdsina.graaljs.executorwebservice.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProviderFactory;
import com.github.mdsina.graaljs.executorwebservice.bindings.context.ContextFactory;
import com.github.mdsina.graaljs.executorwebservice.cache.SourceCache;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverter;
import com.github.mdsina.graaljs.executorwebservice.logging.SLF4JOutputStreamBridge;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.event.Level;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class JsSourceExecutor {

    private final SourceCache sourceCache;
    private final ContextFactory contextFactory;
    private final ObjectMapper objectMapper;
    private final BindingsProviderFactory bindingsProviderFactory;
    private final JsDebugTaskWorker jsDebugTaskWorker;

//    public String executeDebug(String scriptName, String body, List<Variable> inputs) {
//        return jsDebugTaskWorker.executeDebugTask(uuid -> () ->
//            executeOnContext(
//                sourceCache.getSource(scriptName, body),
//                inputs,
//                (outputStream, errorStream) -> contextFactory.getDebugContext(outputStream, errorStream, uuid)
//            )
//        );
//    }

    public Mono<JsExecutionResult> execute(String scriptName, String body, List<Variable> inputs) {
        return executeOnContext(
            sourceCache.getSource(scriptName, body),
            inputs,
            contextFactory::getContext
        );
    }

    private Mono<JsExecutionResult> executeOnContext(
        Source source,
        List<Variable> inputs,
        BiFunction<OutputStream, OutputStream, Context> contextFactory
    ) {
        return Mono.subscriberContext()
            .flatMap(ctx -> Mono.create(sink -> {
                Context context = ctx.get("ctx");
                JsonConverter jsonConverter = ctx.get("jsonConverter");

                HashMap<String, Object> mappedInputs = new HashMap<>();
                for (Variable input : inputs) {
                    mappedInputs.put(input.getName(), input.getValue());
                }

                Object parsedInputs;
                try {
                    parsedInputs = jsonConverter.parse(objectMapper.writeValueAsString(mappedInputs));
                } catch (JsonProcessingException e) {
                    sink.error(e);
                    return;
                }

                context.eval(source);

                Value promise = context.getBindings("js")
                    .getMember("callFunction")
                    .execute(parsedInputs);

                promise.getMember("then").executeVoid(
                    (Consumer<Object>) sink::success,
                    (Consumer<Object>) (o) -> sink.error(new RuntimeException(Value.asValue(o).toString()))
                );
            }))
            .flatMap(res -> Mono.subscriberContext().map(ctx -> {
                JsonConverter jsonConverter = ctx.get("jsonConverter");

                String json = jsonConverter.stringify(res);

                Map<String, Object> outputs;
                try {
                    outputs = objectMapper.readValue(json, new TypeReference<>() {});
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

                List<Variable> outputVariables = new ArrayList<>();
                outputs.forEach((k, v) -> outputVariables.add(Variable.builder().name(k).value(v).build()));

                return new JsExecutionResult(
                    outputVariables,
                    ctx.<StringBuilder>get("outputBuilder").toString(),
                    ctx.<StringBuilder>get("errorBuilder").toString()
                );
            }))
            .subscriberContext(ctx -> {
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

                Context context = contextFactory.apply(outputStreamBridge, errorStreamBridge);

                var jsonConverter = new JsonConverter(context.getBindings("js").getMember("JSON"));

                var bindings = context.getBindings("js");
                var bindingsProviders = bindingsProviderFactory.getBindingsProviders(jsonConverter);
                bindingsProviders.forEach(o -> o.applyTo(bindings));

                return ctx.put("ctx", context)
                    .put("outputBuilder", outputBuilder)
                    .put("jsonConverter", jsonConverter)
                    .put("errorBuilder", errorBuilder);
            });
    }
}
