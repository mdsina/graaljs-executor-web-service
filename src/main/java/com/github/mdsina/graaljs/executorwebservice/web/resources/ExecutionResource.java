package com.github.mdsina.graaljs.executorwebservice.web.resources;

import com.github.mdsina.graaljs.executorwebservice.execution.JsDebugTaskWorker;
import com.github.mdsina.graaljs.executorwebservice.execution.JsSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.execution.JsExecutionResult;
import com.github.mdsina.graaljs.executorwebservice.script.Script;
import com.github.mdsina.graaljs.executorwebservice.script.ScriptStorageService;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class ExecutionResource {

    private final ScriptStorageService scriptStorageService;
    private final JsSourceExecutor jsSourceExecutor;
    private final JsDebugTaskWorker jsDebugTaskWorker;

    @PostMapping("/script/{scriptId}")
    public JsExecutionResult performCall(
        @PathVariable String scriptId,
        @RequestBody CallRequest request
    ) throws Exception {

        Script script = scriptStorageService.getScript(scriptId);
        return jsSourceExecutor.execute(
            script.getId(),
            script.getBody(),
            request.getInputs()
        );
    }

    @PostMapping("/script/{scriptId}/debug")
    public String debugCall(
        @PathVariable String scriptId,
        @RequestBody CallRequest request
    ) {
        Script script = scriptStorageService.getScript(scriptId);
        return jsSourceExecutor.executeDebug(
            script.getId(),
            script.getBody(),
            request.getInputs()
        );
    }

    @PostMapping("/script/debug-output/{debugId}")
    public JsExecutionResult debugCall(@PathVariable String debugId) throws ExecutionException, InterruptedException {
        var debugTask = jsDebugTaskWorker.getDebugTask(debugId);
        if (debugTask == null) {
            throw new RuntimeException("Debug not found");
        }
        if (debugTask.isCancelled()) {
            throw new RuntimeException("Debug was cancelled.");
        }
        if (debugTask.isCompletedExceptionally()) {
            throw new RuntimeException("Debug completed exceptionally");
        }

        return debugTask.get();
    }
}
