package com.github.mdsina.graaljs.executorwebservice.bindings;

import com.github.mdsina.graaljs.executorwebservice.bindings.exports.SampleUtil;
import com.github.mdsina.graaljs.executorwebservice.bindings.modules.QueryStringModule;
import com.github.mdsina.graaljs.executorwebservice.bindings.modules.SampleUtilModule;
import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverter;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BindingsProviderFactory {

    private final QueryStringModule queryStringModule;

    public List<BindingsProvider> getBindingsProviders(JsonConverter jsonConverter) {
        return List.of(
            new RequireModulesProvider(Set.of(
                queryStringModule,
                new SampleUtilModule(new SampleUtil(jsonConverter))
            ))
        );
    }
}
