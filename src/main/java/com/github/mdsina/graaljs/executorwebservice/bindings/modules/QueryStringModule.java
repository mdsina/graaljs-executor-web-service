package com.github.mdsina.graaljs.executorwebservice.bindings.modules;

import com.github.mdsina.graaljs.executorwebservice.bindings.RequireModule;
import com.github.mdsina.graaljs.executorwebservice.bindings.exports.QueryString;
import org.springframework.stereotype.Component;

@Component
public class QueryStringModule implements RequireModule {

    private final QueryString queryString;

    public QueryStringModule(QueryString queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getName() {
        return "querystring";
    }

    @Override
    public Object getExported() {
        return queryString;
    }
}
