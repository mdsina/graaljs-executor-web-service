package com.github.mdsina.graaljs.executorwebservice.util;

import com.github.mdsina.graaljs.executorwebservice.script.Script;
import com.github.mdsina.graaljs.executorwebservice.script.ScriptStorageService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.apache.commons.io.FileUtils;

public class ScriptUtil {

    public static Script getScript(String name) {
        URL resource = ScriptStorageService.class.getClassLoader().getResource("js/" + name + ".js");
        File file = new File(resource.getFile());
        String content;
        try {
            content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Script.builder()
            .id(name)
            .body(content)
            .modifyDate(LocalDateTime.now())
            .build();
    }
}
