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
import com.github.mdsina.graaljs.executorwebservice.execution.JsExecutionResult;
import com.github.mdsina.graaljs.executorwebservice.script.ScriptStorageService;
import com.github.mdsina.graaljs.executorwebservice.util.ScriptUtil;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

    @MockBean
    private ScriptStorageService scriptStorageService;

    @Before
    public void before() {
        Mockito
            .when(scriptStorageService.getScript(ArgumentMatchers.anyString()))
            .thenAnswer(a -> ScriptUtil.getScript(a.getArgument(0)));
    }

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
                post("/api/v1/script/TEST")
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
    public void checkDateFormatUtilTest() throws Exception {
        OffsetDateTime dateTime = OffsetDateTime.now(ZoneOffset.UTC);
        String simple = DateTimeFormatter.ofPattern("dd.MM.yyy").format(dateTime);

        InvocationInfo invocationInfo = InvocationInfo.builder()
            .inputs(List.of(Variable.builder().name("DATE").value(dateTime.toString()).build()))
            .build();

        String content = this.mockMvc
            .perform(
                post("/api/v1/script/TEST_DATE")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(invocationInfo))
            )
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        JsExecutionResult response = objectMapper.readValue(content, new TypeReference<JsExecutionResult>() {});
        assertFalse(response.getOutputs().isEmpty());
        assertNotEquals(null, response.getOutputs().get(0));
        assertEquals("DATE", response.getOutputs().get(0).getName());
        assertEquals(simple, response.getOutputs().get(0).getValue());
    }
}

