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
import com.github.mdsina.graaljs.executorwebservice.execution.JavaScriptSourceExecutor;
import com.github.mdsina.graaljs.executorwebservice.execution.JsExecutionResult;
import com.github.mdsina.graaljs.executorwebservice.script.Script;
import com.github.mdsina.graaljs.executorwebservice.script.ScriptStorageService;
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
        InvocationInfo invocationInfo = InvocationInfo.builder()
            .inputs(List.of(
                Variable.builder()
                    .name("RU_STR")
                    .value("Привет, Graal!")
                    .build()
            ))
            .outputs(List.of(
                Variable.builder()
                    .name("EN_STR")
                    .build()
            ))
            .build();

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
        assertEquals("EN_STR", response.getOutputs().get(0).getName());
        assertEquals("Privet, Graal!", response.getOutputs().get(0).getValue());
    }

    @Test
    public void perfTest() throws Exception {
        int ITERATIONS = 100;
        for (int iter = 0; iter < ITERATIONS; iter++) {
            long total = 0, start = System.currentTimeMillis(), last = start;
            for (int i = 1; i < 10_00; i++) {
                Script script = scriptStorageService.getScript("PERF_1");
                javaScriptSourceExecutor.execute(
                    script.getId(),
                    script.getBody(),
                    List.of(Variable.builder().name("I").value("PERF_TEST").build())
                );
                total += 1;
                if (i % 1_00 == 0) {
                    long now = System.currentTimeMillis();
                    System.out.printf("%d (%d ms)%n", i / 1_00, now - last);
                    last = now;
                }
            }
            System.out.printf("iter %d: total: %d (%d ms)%n", iter, total, System.currentTimeMillis() - start);
        }
    }
}

