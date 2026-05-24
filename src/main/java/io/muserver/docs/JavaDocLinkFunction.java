package io.muserver.docs;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.muserver.Mutils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JavaDocLinkFunction implements Function {

    private final boolean isLocal;

    public JavaDocLinkFunction(boolean isLocal) {
        this.isLocal = isLocal;
    }

    @Override
    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        String c = (String) args.get("className");

        if (isLocal) {
            try {
                Class.forName(c);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Invalid class " + args);
            }
        }

        int i = c.lastIndexOf('.');
        String pack = c.substring(0, i);
        String artifact = artifactForPackage(pack);

        String name = c.substring(i + 1);
        String url;
        if (pack.startsWith("io.muserver")) {
            url = "/javadocs/" + artifact + "/" + pack.replace(".", "/") + "/" + name + ".html";
        } else {
            url = externalUrl(pack, artifact, name);
        }
        return "<a href=\"" + url + "\">" + Mutils.htmlEncode(name) + "</a>";

    }

    private static String artifactForPackage(String pack) {
        if (pack.startsWith("io.muserver.murp")) {
            return "murp";
        }
        if (pack.startsWith("io.muserver.acme")) {
            return "mu-acme";
        }
        return "mu-server";
    }

    private static String externalUrl(String pack, String artifact, String name) {
        if (pack.startsWith("jakarta.ws.rs")) {
            return "https://www.javadoc.io/doc/jakarta.ws.rs/jakarta.ws.rs-api/latest/" + pack.replace(".", "/") + "/" + name + ".html";
        }
        return "https://www.javadoc.io/page/io.muserver/" + artifact + "/latest/" + pack.replace(".", "/") + "/" + name + ".html";
    }

    @Override
    public List<String> getArgumentNames() {
        return Collections.singletonList("className");
    }
}
