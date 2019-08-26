package com.github.mdsina.graaljs.executorwebservice.util;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

public class UriUtil {

    private static final Map<String, String> CHARACTERS_TO_ENCODE = Map.of("[", "%5B", "]", "%5D", ":", "%3A");

    public static URI createUri(String url, Map<String, ?> requestQueryParams) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(url);

        return uriComponentsBuilder
            .replaceQueryParams(liftParams(uriComponentsBuilder.build().getQueryParams(), requestQueryParams))
            .replacePath(encodePath(uriComponentsBuilder.build(false).getPath()))
            .build(true).toUri();
    }

    private static String encodePath(String path) {
        // check manual encoding from js for path
        path = UriUtils.decode(path, StandardCharsets.UTF_8.name());
        // re-encode
        path = UriUtils.encodePath(path, StandardCharsets.UTF_8.name());

        return path;
    }

    private static MultiValueMap<String, String> liftParams(
        MultiValueMap<String, String> paramsInline,
        Map<String, ?> requestQueryParams
    ) {
        // For inline query parameters specified in URI
        Map<String, List<?>> queryParams = new HashMap<>(paramsInline);

        if (requestQueryParams != null) {
            requestQueryParams.forEach((k, v) -> {
                List<Object> value = new ArrayList<>();
                if (v instanceof Map) {
                    throw new RuntimeException("Query param '" + k + "' cannot be object");
                } else if (v instanceof List) {
                    value.addAll((List) v);
                } else {
                    value.add(v);
                }
                queryParams.put(k, value);
            });
        }

        return encodeAndReplaceParams(queryParams);
    }

    private static MultiValueMap<String, String> encodeAndReplaceParams(Map<String, List<?>> params) {
        if (!params.isEmpty()) {
            Map<String, List<String>> newQueryParams = params
                .entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        e -> encodeAndReplace(e.getKey()),
                        e -> e.getValue().stream()
                            .map(v -> encodeAndReplace(String.valueOf(v)))
                            .collect(Collectors.toList())
                    )
                );

            return CollectionUtils.toMultiValueMap(newQueryParams);
        }

        return CollectionUtils.toMultiValueMap(Collections.emptyMap());
    }

    private static String encodeAndReplace(String text) {
        // decode value if that was manually encoded
        String decoded = UriUtils.decode(String.valueOf(text), StandardCharsets.UTF_8.name());
        String encoded = UriUtils.encodeQueryParam(decoded, StandardCharsets.UTF_8.name());

        for (Entry<String, String> entry : CHARACTERS_TO_ENCODE.entrySet()) {
            encoded = StringUtils.replace(encoded, entry.getKey(), entry.getValue());
        }

        return encoded;
    }
}