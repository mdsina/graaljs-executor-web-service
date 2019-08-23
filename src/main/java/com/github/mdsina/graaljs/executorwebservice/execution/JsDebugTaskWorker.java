package com.github.mdsina.graaljs.executorwebservice.execution;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class JsDebugTaskWorker {

    private final static long TIMEOUT = 5;

    private final RandomBasedGenerator generator = Generators.randomBasedGenerator();
    private final Map<String, CompletableFuture<JsExecutionResult>> tasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);

    public String executeDebugTask(Function<String, Supplier<JsExecutionResult>> taskFactory) {
        final String uuid = generator.generate().toString();

        tasks.put(uuid, CompletableFuture.supplyAsync(taskFactory.apply(uuid)));
        scheduledExecutorService.schedule(() -> tasks.computeIfPresent(uuid, (k, v) -> {
            if (
                !(v.isCancelled()
                    || v.isCompletedExceptionally()
                    || v.isDone())
            ) {
                v.cancel(true);
            }

            return null;
        }), TIMEOUT, TimeUnit.MINUTES);

        return uuid;
    }

    public CompletableFuture<JsExecutionResult> getDebugTask(String uuid) {
        return tasks.get(uuid);
    }
}
