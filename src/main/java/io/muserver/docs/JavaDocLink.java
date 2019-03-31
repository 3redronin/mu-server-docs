package io.muserver.docs;

import io.muserver.Mutils;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;

public class JavaDocLink extends SimpleJtwigFunction {
    @Override
    public String name() {
        return "javadoc";
    }

    @Override
    public Object execute(FunctionRequest request) {
        String c = request.get(0).toString();
        int i = c.lastIndexOf('.');
        String pack = c.substring(0, i);
        String artifact;
        switch (pack) {
            case "io.muserver":
                artifact = "mu-server";
                break;
            case "io.muserver.murp":
                artifact = "murp";
                break;
            default:
                throw new RuntimeException("Unrecognised package: " + pack);
        }

        String name = c.substring(i + 1);
        String url = "https://www.javadoc.io/page/io.muserver/" + artifact + "/latest/" + pack.replace(".", "/") + "/" + name + ".html";
        return "<a href=\"" + url + "\">" + Mutils.htmlEncode(name) + "</a>";

    }
}
