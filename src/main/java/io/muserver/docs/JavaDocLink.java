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
        String name = c.substring(i + 1);
        String url = "https://www.javadoc.io/page/io.muserver/mu-server/latest/" + pack.replace(".", "/") + "/" + name + ".html";
        return "<a href=\"" + url + "\">" + Mutils.htmlEncode(name) + "</a>";

    }
}
