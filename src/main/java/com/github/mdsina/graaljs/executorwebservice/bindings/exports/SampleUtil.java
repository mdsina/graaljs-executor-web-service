package com.github.mdsina.graaljs.executorwebservice.bindings.exports;

import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverter;
import com.github.mdsina.graaljs.executorwebservice.util.DateUtils;
import com.github.mdsina.graaljs.executorwebservice.util.TransliterationUtil;
import java.time.ZonedDateTime;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SampleUtil {

    private final JsonConverter jsonConverter;

    public String transliterate(String str) {
        return TransliterationUtil.transliterate(str);
    }

    public String formatDate(ZonedDateTime dateTime) {
        return DateUtils.formatDate(dateTime);
    }

    public String formatDate(ZonedDateTime dateTime, String format) {
        return DateUtils.formatDate(dateTime, format);
    }

    public Object request(String url, String method) {
        Function<Mono, Thenable> thenBuilder =
            value ->
                (resolve, reject) ->
                    value.doOnNext(resolve::executeVoid)
                        .doOnError(reject::executeVoid)
                        .subscribe();

        return thenBuilder.apply(
            WebClient.create(url)
                .method(HttpMethod.valueOf(method))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class)
                .flatMap(e -> Mono.justOrEmpty(e.getBody()))
                .map(jsonConverter::parse)
                .switchIfEmpty(Mono.just(Value.asValue(null)))
        );
    }

    public interface Thenable {
        void then(Value resolve, Value reject);
    }
}
