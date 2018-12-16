package com.github.mdsina.graaljs.executorwebservice.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.domain.JavaScriptSourceMetaInfo;
import java.io.IOException;
import org.springframework.stereotype.Component;

@Component
public class JavaScriptSourceMetaParser {

    private static final String META_PREFIX = "meta = {";

    private final ObjectMapper javaScriptObjectMapper;

    public JavaScriptSourceMetaParser(ObjectMapper objectMapper) {
        javaScriptObjectMapper = objectMapper.copy();
        javaScriptObjectMapper.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        javaScriptObjectMapper.enable(Feature.ALLOW_TRAILING_COMMA);
        javaScriptObjectMapper.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        javaScriptObjectMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
    }

    public JavaScriptSourceMetaInfo parse(String body) {
        final int metaStartIndex = body.indexOf(META_PREFIX);

        if (metaStartIndex >= 0) {
            final String rawRequest = body.substring(metaStartIndex + META_PREFIX.length() - 1);
            try {
                return javaScriptObjectMapper.readValue(rawRequest, JavaScriptSourceMetaInfo.class);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot parse meta from source: " + e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException(String.format("Source must start from \"%s\"", META_PREFIX));
        }
    }
}
