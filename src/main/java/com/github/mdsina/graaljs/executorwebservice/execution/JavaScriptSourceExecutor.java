package com.github.mdsina.graaljs.executorwebservice.execution;

import com.github.mdsina.graaljs.executorwebservice.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.pool2.ObjectPool;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class JavaScriptSourceExecutor {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ObjectPool<ContextWrapper> pool;
    private final ScriptExecutionScope executionScope;
    private final RandomDataGenerator randomDataGenerator;
    private final ExecutionScopeDataBridge executionScopeDataBridge;

    public JavaScriptSourceExecutor(
        @Qualifier("graalObjectPool") ObjectPool<ContextWrapper> pool,
        ScriptExecutionScope executionScope,
        ExecutionScopeDataBridge executionScopeDataBridge,
        RandomDataGenerator randomDataGenerator
    ) {
        this.pool = pool;
        this.executionScope = executionScope;
        this.executionScopeDataBridge = executionScopeDataBridge;
        this.randomDataGenerator = randomDataGenerator;
    }

    public List<Map<String, Object>> execute(
        ScriptDto script,
        List<Variable> inputs
    ) throws Exception {

        // nextHexString can cause same value on multiple threads
        String scriptRunId = script.getId() + "_" + randomDataGenerator.nextHexString(16);
        executionScope.activate(scriptRunId);

        ContextWrapper contextWrapper = pool.borrowObject();
        try {
            Value namespaceObj = contextWrapper.getScriptBindings(script);

            executionScopeDataBridge.setInputs(inputs);

            namespaceObj.getMember("callFunction").executeVoid();
            logger.trace("{}.callFunction called", script.getId());

            return executionScopeDataBridge.getOutputs();
        } finally {
            pool.returnObject(contextWrapper);
            executionScope.deactivate();
        }
    }
}
