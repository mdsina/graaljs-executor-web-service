package com.github.mdsina.graaljs.executorwebservice.bindings.context;

import java.io.OutputStream;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ContextFactory {

    private final Engine engine;
    private final HostAccess hostAccess;

    public Context getContext(OutputStream outputStream, OutputStream errorStream) {
        return Context.newBuilder("js")
            .allowAllAccess(true)
            .allowHostAccess(hostAccess)
            .out(outputStream)
            .err(errorStream)
            .engine(engine)
            .build();
    }

    public Context getDebugContext(OutputStream outputStream, OutputStream errorStream, String id) {
        return Context.newBuilder("js")
            .allowAllAccess(true)
            .allowHostAccess(hostAccess)
            .option("inspect", "4242")
            .option("inspect.Path", id)
            .option("inspect.Suspend", "true") // should be on first line,
                // because after attaching debugger script will continue executing if it's false
            .out(outputStream)
            .err(errorStream)
            .build();
    }
}
