package com.github.mdsina.graaljs.executorwebservice.util;

import java.util.regex.Pattern;

public class ScriptBodyToNamespaceWrapper {

    private static final Pattern META_PATTERN = Pattern.compile("(meta\\s*=)", Pattern.MULTILINE);
    private static final Pattern CALL_FUNC_PATTERN = Pattern.compile("(callFunction\\s*=)", Pattern.MULTILINE);

    public static String wrapBodyToNamespace(String namespace, String body) {
        String newBody = body;
        newBody = namespace + " = {};\n" +
            namespace + ".init = function() {\n" + newBody + "\n};\n" +
            namespace + ".init();\n";

        newBody = META_PATTERN.matcher(newBody).replaceFirst(namespace + ".meta =");
        newBody = CALL_FUNC_PATTERN.matcher(newBody).replaceFirst(namespace + ".callFunction =");

        return newBody;
    }

    public static String getNamespace(String scriptId) {
        return "NAMESPACE_" + scriptId;
    }
}
