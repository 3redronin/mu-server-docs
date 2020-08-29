package io.muserver.docs;

import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
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
        String artifact;
        switch (pack) {
            case "io.muserver.murp":
                artifact = "murp";
                break;
            default:
                artifact = "mu-server";
                break;
        }

        String name = c.substring(i + 1);
        String url = "https://www.javadoc.io/page/io.muserver/" + artifact + "/latest/" + pack.replace(".", "/") + "/" + name + ".html";
        return "<a href=\"" + url + "\">" + Mutils.htmlEncode(name) + "</a>";

    }

    @Override
    public List<String> getArgumentNames() {
        return Collections.singletonList("className");
    }
}
