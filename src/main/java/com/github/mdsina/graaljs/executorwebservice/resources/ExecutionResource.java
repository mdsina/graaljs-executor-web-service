package com.github.mdsina.graaljs.executorwebservice.resources;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.domain.InvocationInfo;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.CallRequestDto;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import com.github.mdsina.graaljs.executorwebservice.execution.JavaScriptSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.service.JavaScriptSourceMetaParser;
import com.github.mdsina.graaljs.executorwebservice.service.ScriptStorageService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1")
public class ExecutionResource {

    private final JavaScriptSourceMetaParser javaScriptSourceMetaParser;
    private final ScriptStorageService scriptStorageService;
    private final JavaScriptSourceExecutor javaScriptSourceExecutor;
    private final ObjectMapper objectMapper;

    public ExecutionResource(
        JavaScriptSourceMetaParser javaScriptSourceMetaParser,
        ScriptStorageService scriptStorageService,
        JavaScriptSourceExecutor javaScriptSourceExecutor,
        ObjectMapper objectMapper
    ) {
        this.javaScriptSourceMetaParser = javaScriptSourceMetaParser;
        this.scriptStorageService = scriptStorageService;
        this.javaScriptSourceExecutor = javaScriptSourceExecutor;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/script/{scriptId}/invocationInfo")
    public ResponseEntity<InvocationInfo> getScriptInfo(@PathVariable String scriptId) {
        final ScriptDto script = scriptStorageService.getScript(scriptId);
        return ResponseEntity.ok(
            javaScriptSourceMetaParser.parse(script.getBody()).getInvocationInfo()
        );
    }

    @PostMapping("/script/{scriptId}")
    public ResponseEntity<List<Variable>> performCall(
        @PathVariable String scriptId, @RequestBody CallRequestDto request
    ) throws Exception {

        ScriptDto script = scriptStorageService.getScript(scriptId);
        List<Map<String, Object>> result = javaScriptSourceExecutor.execute(
            script,
            request.getInputs(),
            javaScriptSourceMetaParser.parse(script.getBody()).getInvocationInfo()
        );
        // Lazy converting just for test purposes. TODO: replace with builder
        String json = objectMapper.writeValueAsString(result);
        return ResponseEntity.ok(objectMapper.readValue(json, new TypeReference<List<Variable>>() {}));
    }
}
