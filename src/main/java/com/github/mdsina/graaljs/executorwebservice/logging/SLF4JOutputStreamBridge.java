package com.github.mdsina.graaljs.executorwebservice.logging;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class SLF4JOutputStreamBridge extends ByteArrayOutputStream {

    private static final Logger logger = LoggerFactory.getLogger("js.logger");

    private final List<Consumer<String>> consumers;
    private final Consumer<String> loggingFunc;

    public static class SLF4JOutputStreamBridgeBuilder {

        private Level level = Level.INFO;
        private final List<Consumer<String>> consumers = new ArrayList<>();

        SLF4JOutputStreamBridgeBuilder() {
        }

        public SLF4JOutputStreamBridgeBuilder addConsumer(Consumer<String> consumer) {
            consumers.add(consumer);
            return this;
        }

        public SLF4JOutputStreamBridgeBuilder logLevel(Level level) {
            this.level = level;
            return this;
        }

        public SLF4JOutputStreamBridge build() {
            return new SLF4JOutputStreamBridge(level, consumers);
        }
    }

    public static SLF4JOutputStreamBridgeBuilder newBuilder() {
        return new SLF4JOutputStreamBridgeBuilder();
    }

    private SLF4JOutputStreamBridge(Level level, List<Consumer<String>> consumers) {
        Objects.requireNonNull(consumers, "Consumers cannot be null");
        Objects.requireNonNull(level, "Log level cannot be null");

        this.consumers = consumers;

        switch (level) {
            case ERROR:
                this.loggingFunc = logger::error;
                break;
            case WARN:
                this.loggingFunc = logger::warn;
                break;
            case DEBUG:
                this.loggingFunc = logger::debug;
                break;
            case TRACE:
                this.loggingFunc = logger::trace;
                break;
            case INFO:
            default:
                this.loggingFunc = logger::info;
                break;
        }
    }

    @Override
    public void flush() {
        if (size() == 0) {
            return;
        }

        String temp = toString();
        String strippedStr = temp;
        // flush() called once per graal's console.log() and etc.
        // Last \n need to be stripped because slf4j adds \n to the end too.
        if (strippedStr.endsWith("\n")) {
            strippedStr = strippedStr.substring(0, strippedStr.length() - 1);
        }

        loggingFunc.accept(strippedStr);

        try {
            consumers.forEach(c -> c.accept(temp));
        } finally {
            reset();
        }
    }
}
