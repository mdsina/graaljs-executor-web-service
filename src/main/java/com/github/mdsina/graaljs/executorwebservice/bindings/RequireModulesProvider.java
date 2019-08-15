package com.github.mdsina.graaljs.executorwebservice.bindings;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.graalvm.polyglot.Value;
import org.springframework.util.StringUtils;

public class RequireModulesProvider implements BindingsProvider {

    private final Map<String, Object> modules;

    public RequireModulesProvider(Set<RequireModule> modules) {
        this.modules = modules.stream()
            .collect(Collectors.toMap(RequireModule::getName, RequireModule::getExported));
    }

    public Object require(String moduleName) {
        if (StringUtils.isEmpty(moduleName)) {
            throw new RuntimeException("module name cannot be empty or must be set");
        }

        return Optional.ofNullable(modules.get(moduleName))
            .orElseThrow(() -> new RuntimeException("module with name `" + moduleName + "` not found."));
    }

    @Override
    public void setBindings(Value bindings) {
        bindings.putMember("require", (Function<String, Object>) this::require);
    }
}
