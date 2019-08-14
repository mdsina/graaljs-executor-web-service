package com.github.mdsina.graaljs.executorwebservice.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.CallRequestDto;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import com.github.mdsina.graaljs.executorwebservice.execution.JavaScriptSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.execution.JsExecutionResult;
import com.github.mdsina.graaljs.executorwebservice.service.ScriptStorageService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
public class ExecutionResource {

    private final ScriptStorageService scriptStorageService;
    private final JavaScriptSourceExecutor javaScriptSourceExecutor;
    private final ObjectMapper objectMapper;

    public ExecutionResource(
        ScriptStorageService scriptStorageService,
        JavaScriptSourceExecutor javaScriptSourceExecutor,
        ObjectMapper objectMapper
    ) {
        this.scriptStorageService = scriptStorageService;
        this.javaScriptSourceExecutor = javaScriptSourceExecutor;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/script/{scriptId}")
    public JsExecutionResult performCall(
        @PathVariable String scriptId, @RequestBody CallRequestDto request
    ) throws Exception {

        ScriptDto script = scriptStorageService.getScript(scriptId);
        JsExecutionResult result = javaScriptSourceExecutor.execute(
            script,
            request.getInputs()
        );

        return result;
    }
}
