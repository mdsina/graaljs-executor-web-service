package com.github.mdsina.graaljs.executorwebservice.execution;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProvider;
import com.github.mdsina.graaljs.executorwebservice.bindings.BindingsProviderFactory;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import java.lang.invoke.MethodHandles;
import java.util.List;
import org.apache.commons.pool2.ObjectPool;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptSourceExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ObjectPool<ContextWrapper> pool;
    private final BindingsProviderFactory bindingsProviderFactory;
    private final ObjectMapper objectMapper;

    public JavaScriptSourceExecutor(
        @Qualifier("graalObjectPool") ObjectPool<ContextWrapper> pool,
        BindingsProviderFactory bindingsProviderFactory,
        ObjectMapper objectMapper
    ) {
        this.pool = pool;
        this.bindingsProviderFactory = bindingsProviderFactory;
        this.objectMapper = objectMapper;
    }

    public JsExecutionResult execute(
        ScriptDto script,
        List<Variable> inputs
    ) throws Exception {

        ContextWrapper contextWrapper = pool.borrowObject();
        try {
            ExecutionScopeDataBridge executionScopeDataBridge = new ExecutionScopeDataBridge(
                contextWrapper.getJsonConverter(),
                objectMapper
            );
            executionScopeDataBridge.setInputs(inputs);

            Context context = contextWrapper.getContext();
            Value bindings = context.getBindings("js");

            // additional memory leak! Just for correct benchmark test in current git branch
            List<BindingsProvider> bindingsProviders = bindingsProviderFactory.getBindingsProviders(
                executionScopeDataBridge
            );
            bindingsProviders.forEach(o -> o.setBindings(bindings));

            Value namespaceObj = contextWrapper.getScriptBindings(script);

            namespaceObj.getMember("callFunction").executeVoid();
            logger.trace("{}.callFunction called", script.getId());

            return new JsExecutionResult(
                executionScopeDataBridge.getOutputs(),
                null,
                null
            );
        } finally {
            pool.returnObject(contextWrapper);
        }
    }
}
