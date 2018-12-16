package com.github.mdsina.graaljs.executorwebservice.modules;

import com.github.mdsina.graaljs.executorwebservice.bindings.RequireModule;
import com.github.mdsina.graaljs.executorwebservice.exports.SampleUtil;
import org.springframework.stereotype.Component;

@Component
public class SampleUtilModule implements RequireModule {

    private final SampleUtil sampleUtil;

    public SampleUtilModule(SampleUtil sampleUtil) {
        this.sampleUtil = sampleUtil;
    }

    @Override
    public String getName() {
        return "sample-util";
    }

    @Override
    public Object getExported() {
        return sampleUtil;
    }
}
