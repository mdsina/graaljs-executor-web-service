package com.github.mdsina.graaljs.executorwebservice.bindings;

import org.graalvm.polyglot.Value;

public interface BindingsProvider {

    void setBindings(Value bindings);
}
