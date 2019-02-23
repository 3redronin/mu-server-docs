package io.muserver.docs;

import io.muserver.Mutils;
import org.jtwig.functions.FunctionRequest;
import org.jtwig.functions.SimpleJtwigFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SourceCodeInjector extends SimpleJtwigFunction {
    private static final Logger log = LoggerFactory.getLogger(SourceCodeInjector.class);
    private final boolean isLocal;

    public SourceCodeInjector(boolean isLocal) {
        this.isLocal = isLocal;
    }

    @Override
    public String name() {
        return "source";
    }

    @Override
    public Object execute(FunctionRequest request) {
        try {
            String name = request.get(0).toString();
            String ext = name.substring(name.lastIndexOf('.') + 1);
            String dir = (ext.equals("java")) ? "src/main/java/io/muserver/docs/samples" : "src/main/resources/samples";
            InputStream in;
            if (isLocal) {
                in = new FileInputStream(dir + "/" + name);
            } else {
                in = getClass().getResourceAsStream("/samples/" + name);
            }
            byte[] bytes = Mutils.toByteArray(in, 8192);
            String code = new String(bytes, UTF_8);

            String lang = "";
            if (ext.equals("java")) {
                lang = "java";
                code = code.substring(code.indexOf("public class ")).trim();
            } else if (ext.equals("html")) {
                lang = "markup";
                code = code.substring(code.indexOf("<body>") + 6, code.indexOf("</body>"));
                code = code.trim();
            }

            return "<pre><code class=\"language-" + lang + "\">" + Mutils.htmlEncode(code) + "</pre></code>" +
                "<a class=\"github-link\" href=\"https://github.com/3redronin/mu-server-docs/blob/master/" + dir + "/"
                + Mutils.urlEncode(name) + "\">(see full file)</a>";
        } catch (Exception e) {
            log.error("Error rendering source code", e);
            return "Error";
        }
    }
}
