package com.github.mdsina.graaljs.executorwebservice.bindings;

import com.github.mdsina.graaljs.executorwebservice.execution.ExecutionScopeDataBridge;
import com.github.mdsina.graaljs.executorwebservice.exports.SampleUtil;
import com.github.mdsina.graaljs.executorwebservice.modules.QueryStringModule;
import com.github.mdsina.graaljs.executorwebservice.modules.SampleUtilModule;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BindingsProviderFactory {

    private final QueryStringModule queryStringModule;

    public BindingsProviderFactory(QueryStringModule queryStringModule) {
        this.queryStringModule = queryStringModule;
    }

    public List<BindingsProvider> getBindingsProviders(ExecutionScopeDataBridge scriptDataBridge) {
        return List.of(
            new RequireModulesProvider(Set.of(
                queryStringModule,
                new SampleUtilModule(new SampleUtil(scriptDataBridge))
            ))
        );
    }
}
