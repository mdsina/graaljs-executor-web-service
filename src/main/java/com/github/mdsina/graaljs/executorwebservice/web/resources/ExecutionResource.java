package com.github.mdsina.graaljs.executorwebservice.web.resources;

import com.github.mdsina.graaljs.executorwebservice.execution.JavaScriptSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.execution.JsExecutionResult;
import com.github.mdsina.graaljs.executorwebservice.script.Script;
import com.github.mdsina.graaljs.executorwebservice.script.ScriptStorageService;
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
    private final JavaScriptSourceExecutor javaScriptSourceExecutor;

    @PostMapping("/script/{scriptId}")
    public JsExecutionResult performCall(
        @PathVariable String scriptId, @RequestBody CallRequest request
    ) throws Exception {

        Script script = scriptStorageService.getScript(scriptId);
        return javaScriptSourceExecutor.execute(
            script.getId(),
            script.getBody(),
            request.getInputs()
        );
    }
}
