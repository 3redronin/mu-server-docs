package io.muserver.docs;

import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.muserver.Mutils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SourceCodeInjectorFunction implements Function {
    private static final Logger log = LoggerFactory.getLogger(SourceCodeInjectorFunction.class);
    private final boolean isLocal;

    public SourceCodeInjectorFunction(boolean isLocal) {
        this.isLocal = isLocal;
    }


    @Override
    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        try {
            String name = (String) args.get("filename");
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

    @Override
    public List<String> getArgumentNames() {
        return Collections.singletonList("filename");
    }
}
