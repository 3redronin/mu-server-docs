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
            InputStream in;
            if (isLocal) {
                in = new FileInputStream("src/main/java/io/muserver/docs/samples/" + name);
            } else {
                in = getClass().getResourceAsStream("/samples/" + name);
            }
            byte[] bytes = Mutils.toByteArray(in, 8192);
            String code = new String(bytes, UTF_8);

            String lang = "";
            if (ext.equals("java")) {
                lang = "java";
                code = code.substring(code.indexOf("public class "));
            }

            return "<pre><code class=\"language-" + lang + "\">" + code + "</pre></code>";
        } catch (Exception e) {
            log.error("Error rendering source code", e);
            return "Error";
        }
    }
}
