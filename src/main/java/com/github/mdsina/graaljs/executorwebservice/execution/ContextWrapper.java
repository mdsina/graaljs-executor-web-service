package com.github.mdsina.graaljs.executorwebservice.execution;

import static com.github.mdsina.graaljs.executorwebservice.util.ScriptBodyToNamespaceWrapper.getNamespace;
import static com.github.mdsina.graaljs.executorwebservice.util.ScriptBodyToNamespaceWrapper.wrapBodyToNamespace;

import com.github.mdsina.graaljs.executorwebservice.util.JsonConverter;
import com.github.mdsina.graaljs.executorwebservice.dto.ScriptDto;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextWrapper {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Context context;
    private final JsonConverter jsonConverter;

    private final Map<String, ScriptSourceInfo> compiledScripts = new ConcurrentHashMap<>();

    public ContextWrapper(Context context) {
        this.context = context;
        jsonConverter = new JsonConverter(context.getBindings("js").getMember("JSON"));
    }

    public Context getContext() {
        return context;
    }

    public Value getScriptBindings(ScriptDto script) {
        String id = script.getId();
        logger.trace("Enter script compilation " + id);

        ScriptSourceInfo info = compiledScripts.get(id);

        try {
            if (info == null || info.getLastModified().isBefore(script.getModifyDate())) {
                if (info == null) {
                    info = new ScriptSourceInfo();
                    info.setNamespace(getNamespace(id));
                }
                logger.trace("Perform script compilation " + id);

                String namespace = info.getNamespace();
                String body = wrapBodyToNamespace(namespace, script.getBody());
                Source source = compileScript(body, namespace);
                info.setLastModified(script.getModifyDate());
                info.setSource(source);

                compiledScripts.putIfAbsent(id, info);
            } else {
                logger.trace("Script already compiled " + id + ". Skipping.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            logger.trace("Exit script compilation " + id);
        }

        return context.getBindings("js").getMember(info.getNamespace());
    }

    public JsonConverter getJsonConverter() {
        return jsonConverter;
    }

    public Source compileScript(String body, String namespace) throws IOException {
        Source source = Source.newBuilder("js", body, namespace).cached(true).build();
        context.eval(source);
        return source;
    }

    private class ScriptSourceInfo {

        private LocalDateTime lastModified;
        private String namespace;
        private Source source; // need link to save compiled cache in Engine? or forever in Context?

        public ScriptSourceInfo() {
        }

        public LocalDateTime getLastModified() {
            return lastModified;
        }

        public void setLastModified(LocalDateTime lastModified) {
            this.lastModified = lastModified;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public Source getSource() {
            return source;
        }

        public void setSource(Source source) {
            this.source = source;
        }
    }

}
