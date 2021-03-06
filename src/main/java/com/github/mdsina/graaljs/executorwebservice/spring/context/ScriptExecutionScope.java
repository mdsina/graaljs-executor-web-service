package com.github.mdsina.graaljs.executorwebservice.spring.context;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class ScriptExecutionScope implements Scope {

    public static final String ID = "js-script";

    private final ThreadLocal<Map<String, Map<String, Object>>> context = ThreadLocal.withInitial(HashMap::new);
    private final ThreadLocal<String> currentExecutionId = ThreadLocal.withInitial(() -> null);

    private final RandomDataGenerator randomDataGenerator;

    public ScriptExecutionScope(RandomDataGenerator randomDataGenerator) {
        this.randomDataGenerator = randomDataGenerator;
    }

    @Override
    public Object get(String s, ObjectFactory<?> objectFactory) {
        Object object = resolveContextualObject(s);
        if (object != null) {
            return object;
        }
        String id = currentExecutionId.get();
        if (id == null) {
            throw new IllegalStateException("Scope is not activated.");
        }
        Map<String, Object> map = context.get().get(id);
        if (map == null) {
            throw new IllegalStateException("Scope not activated");
        }

        return map.compute(s, (k, v) -> objectFactory.getObject());
    }

    @Override
    public Object remove(String s) {
        return context.get().get(currentExecutionId.get()).remove(s);
    }

    @Override
    public void registerDestructionCallback(String s, Runnable runnable) {
    }

    @Override
    public Object resolveContextualObject(String s) {
        String id = currentExecutionId.get();
        if (id == null) {
            return null;
        }

        Map<String, Object> map = context.get().get(id);
        if (map == null) {
            return null;
        }

        return map.get(s);
    }

    @Override
    public String getConversationId() {
        return ID;
    }

    public String activate() {
        String scriptExecutionId = randomDataGenerator.nextHexString(10) + "_" + Thread.currentThread().getName();
        Map<String, Map<String, Object>> executionsBeans = context.get();
        if (executionsBeans.containsKey(scriptExecutionId)) {
            throw new IllegalStateException("Scope already activated (execution_id=" + scriptExecutionId + ")");
        }
        executionsBeans.put(scriptExecutionId, new HashMap<>());
        currentExecutionId.set(scriptExecutionId);

        return scriptExecutionId;
    }

    public void deactivate() {
        String id = currentExecutionId.get();
        Map<String, Map<String, Object>> executionsBeans = context.get();
        if (!executionsBeans.containsKey(id)) {
            throw new IllegalStateException("Scope is not activated (execution_id=" + id + ")");
        }
        executionsBeans.get(id).clear();
        executionsBeans.remove(id);
    }
}
