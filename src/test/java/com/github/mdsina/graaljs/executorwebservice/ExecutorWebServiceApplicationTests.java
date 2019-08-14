package com.github.mdsina.graaljs.executorwebservice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.domain.InvocationInfo;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import com.github.mdsina.graaljs.executorwebservice.execution.JavaScriptSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.execution.JsExecutionResult;
import com.github.mdsina.graaljs.executorwebservice.service.ScriptStorageService;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExecutorWebServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScriptStorageService scriptStorageService;
    @Autowired
    private JavaScriptSourceExecutor javaScriptSourceExecutor;

    @Test
    public void checkTransliterateOutput() throws Exception {
        InvocationInfo invocationInfo = new InvocationInfo(new ArrayList<>(), new ArrayList<>());
        invocationInfo.getInputs().add(new Variable("RU_STR", "Привет, Graal!"));
        invocationInfo.getOutputs().add(new Variable( "EN_STR"));

        String content = this.mockMvc
            .perform(
                post("/api/v1/script/TEST_1")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(invocationInfo))
            )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsExecutionResult response = objectMapper.readValue(content, new TypeReference<JsExecutionResult>() {});
        assertFalse(response.getOutputs().isEmpty());
        assertNotEquals(null, response.getOutputs().get(0));
        assertEquals("EN_STR", response.getOutputs().get(0).get("name"));
        assertEquals("Privet, Graal!", response.getOutputs().get(0).get("value"));
    }

    @Test
    public void perfTest() throws Exception {
        int ITERATIONS = 100;
        for (int iter = 0; iter < ITERATIONS; iter++) {
            long total = 0, start = System.currentTimeMillis(), last = start;
            for (int i = 1; i < 10_000; i++) {
                ScriptDto script = scriptStorageService.getScript("PERF_1");
                javaScriptSourceExecutor.execute(
                    script,
                    List.of(new Variable("I", "PERF_TEST"))
                );
                total += 1;
                if (i % 1_000 == 0) {
                    long now = System.currentTimeMillis();
                    System.out.printf("%d (%d ms)%n", i / 1_000, now - last);
                    last = now;
                }
            }
            System.out.printf("iter %d: total: %d (%d ms)%n", iter, total, System.currentTimeMillis() - start);
        }
    }
}

