package com.github.mdsina.graaljs.executorwebservice.execution;

import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProvider;
import com.github.mdsina.graaljs.executorwebservice.cache.SourceCache;
import com.github.mdsina.graaljs.executorwebservice.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptSourceExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ScriptExecutionScope executionScope;
    private final RandomDataGenerator randomDataGenerator;
    private final ExecutionScopeDataBridge executionScopeDataBridge;
    private final SourceCache sourceCache;
    private final Set<BindingsProvider> bindingsProviders;
    private final Engine engine;

    public JavaScriptSourceExecutor(
        ScriptExecutionScope executionScope,
        ExecutionScopeDataBridge executionScopeDataBridge,
        RandomDataGenerator randomDataGenerator,
        SourceCache sourceCache,
        Set<BindingsProvider> bindingsProviders,
        Engine engine
    ) {
        this.executionScope = executionScope;
        this.executionScopeDataBridge = executionScopeDataBridge;
        this.randomDataGenerator = randomDataGenerator;
        this.sourceCache = sourceCache;
        this.bindingsProviders = bindingsProviders;
        this.engine = engine;
    }

    public List<Map<String, Object>> execute(ScriptDto script, List<Variable> inputs) {
        // nextHexString can cause same value on multiple threads
        String scriptRunId = script.getId() + "_" + randomDataGenerator.nextHexString(16);
        executionScope.activate(scriptRunId);

        try {
            executionScopeDataBridge.setInputs(inputs);

            Source source = sourceCache.getSource(script.getId(), script.getBody());

            try (Context context = getContext()) {
                context.eval(source);
                context.getBindings("js").getMember("callFunction").executeVoid();
                logger.trace("{}.callFunction called", script.getId());
            }

            return executionScopeDataBridge.getOutputs();
        } finally {
            executionScope.deactivate();
        }
    }

    public Context getContext() {
        Context context = Context.newBuilder("js")
            .allowAllAccess(true)
            .allowHostAccess(HostAccess.newBuilder(HostAccess.ALL)
                .targetTypeMapping(Long.class, Object.class, null, v -> v)
                .targetTypeMapping(Double.class, Object.class, null, BigDecimal::valueOf)
                .targetTypeMapping(List.class, Object.class, null, v -> v)
                .targetTypeMapping(
                    Value.class,
                    Instant.class,
                    v -> {
                        Value isDate = Context.getCurrent().eval("js", "d => d instanceof Date");
                        return isDate.execute(v).asBoolean();
                    },
                    v -> Instant.ofEpochMilli(v.invokeMember("getTime").asLong())
                )
                .build())
            .engine(engine)
            .build();
        Value bindings = context.getBindings("js");
        bindingsProviders.forEach(o -> o.setBindings(bindings));

        return context;
    }
}
