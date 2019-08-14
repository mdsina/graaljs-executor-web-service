package com.github.mdsina.graaljs.executorwebservice.execution;

import com.github.mdsina.graaljs.executorwebservice.context.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import com.github.mdsina.graaljs.executorwebservice.logging.SLF4JOutputStreamBridge.SLF4JOutputStreamBridgeBuilder;
import java.lang.invoke.MethodHandles;
import java.util.List;
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
    private final SLF4JOutputStreamBridgeBuilder outputStreamBuilder;
    private final SLF4JOutputStreamBridgeBuilder errorStreamBuilder;
    private final JsonConverterProxy jsonConverterProxy;

    public JavaScriptSourceExecutor(
        @Qualifier("graalObjectPool") ObjectPool<ContextWrapper> pool,
        ScriptExecutionScope executionScope,
        ExecutionScopeDataBridge executionScopeDataBridge,
        RandomDataGenerator randomDataGenerator,
        @Qualifier("jsOutputStreamBuilder") SLF4JOutputStreamBridgeBuilder outputStreamBuilder,
        @Qualifier("jsErrorStreamBuilder") SLF4JOutputStreamBridgeBuilder errorStreamBuilder,
        JsonConverterProxy jsonConverterProxy
    ) {
        this.pool = pool;
        this.executionScope = executionScope;
        this.executionScopeDataBridge = executionScopeDataBridge;
        this.randomDataGenerator = randomDataGenerator;
        this.outputStreamBuilder = outputStreamBuilder;
        this.errorStreamBuilder = errorStreamBuilder;
        this.jsonConverterProxy = jsonConverterProxy;
    }

    public JsExecutionResult execute(
        ScriptDto script,
        List<Variable> inputs
    ) throws Exception {

        // nextHexString can cause same value on multiple threads
        String scriptRunId = script.getId() + "_" + randomDataGenerator.nextHexString(16);
        executionScope.activate(scriptRunId);

        ContextWrapper contextWrapper = pool.borrowObject();
        try {
            Value namespaceObj = contextWrapper.getScriptBindings(script);
            jsonConverterProxy.setJsonConverter(contextWrapper.getJsonConverter());

            executionScopeDataBridge.setInputs(inputs);

            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();
            outputStreamBuilder.addConsumer(outputBuilder::append);
            errorStreamBuilder.addConsumer(errorBuilder::append);

            namespaceObj.getMember("callFunction").executeVoid();
            logger.trace("{}.callFunction called", script.getId());

            return new JsExecutionResult(
                executionScopeDataBridge.getOutputs(),
                outputBuilder.toString(),
                errorBuilder.toString()
            );
        } finally {
            pool.returnObject(contextWrapper);
            executionScope.deactivate();
        }
    }
}
