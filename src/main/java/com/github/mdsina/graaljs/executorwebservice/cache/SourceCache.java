package com.github.mdsina.graaljs.executorwebservice.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.graalvm.polyglot.Source;
import org.springframework.stereotype.Component;

@Component
public class SourceCache {

    private final Map<String, Source> sourcesCache = new ConcurrentHashMap<>();

    public Source getSource(String scriptName, String body) {
        return sourcesCache.computeIfAbsent(scriptName, name -> compileScript(name, body));
    }

    private Source compileScript(String scriptName, String body) {
        return Source.newBuilder("js", body, scriptName).cached(false).buildLiteral();
    }
}
