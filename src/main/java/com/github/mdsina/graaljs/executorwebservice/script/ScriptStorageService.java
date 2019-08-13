package com.github.mdsina.graaljs.executorwebservice.script;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

@Service
public class ScriptStorageService {

    private static final LocalDateTime START_TIME = LocalDateTime.now(); // just for test purposes
    private static final Map<String, Script> SCRIPTS = new ConcurrentHashMap<>();

    static {
        // In real world sources loaded from database with versioning
        URL resource = ScriptStorageService.class.getClassLoader().getResource("js/TEST.js");
        File file = new File(resource.getFile());
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        IntStream.range(1, 1001).forEach(i -> {
            String id = "TEST_" + i;
            SCRIPTS.put(
                id,
                Script.builder()
                    .id(id)
                    .body(content)
                    .modifyDate(START_TIME)
                    .build()
            );
        });
    }

    public Script getScript(String scriptId) {
        Script content = SCRIPTS.get(scriptId);
        if (content == null) {
            throw new RuntimeException("Script " + scriptId + " not found");
        }
        return content;
    }
}
