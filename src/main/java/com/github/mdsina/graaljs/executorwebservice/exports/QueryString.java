package com.github.mdsina.graaljs.executorwebservice.exports;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

@Component
public class QueryString {

    public String stringify(Map<String, ?> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        params.forEach(builder::queryParam);

        return UriUtils.encodeQuery(builder.build().getQuery(), StandardCharsets.UTF_8.name());
    }
}
