package io.muserver.docs;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.extension.AbstractExtension;
import io.pebbletemplates.pebble.extension.Filter;
import io.pebbletemplates.pebble.extension.Function;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.template.EvaluationContext;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import io.muserver.ContentTypes;
import io.muserver.MuResponse;
import io.muserver.MuServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;

public interface ViewRenderer {
    void render(MuResponse response, String relativePath, Map<String,Object> model);

    MapBuilder model();

    static ViewRenderer create(boolean isLocal) {
        PebbleEngine.Builder builder = new PebbleEngine.Builder();
        if (isLocal) {
            FileLoader loader = new FileLoader();
            loader.setCharset("utf-8");
            loader.setPrefix("src/main/resources/views");
            builder = builder.loader(loader).cacheActive(false);
        } else {
            ClasspathLoader loader = new ClasspathLoader(ViewRenderer.class.getClassLoader());
            loader.setCharset("utf-8");
            loader.setPrefix("views/");
            builder = builder.loader(loader).cacheActive(true);
        }
        return new ViewRendererImpl(isLocal, builder
            .extension(new AbstractExtension() {
                @Override
                public Map<String, Filter> getFilters() {
                    return super.getFilters();
                }

                @Override
                public Map<String, Function> getFunctions() {
                    Map<String,Function> fs = new HashMap<>();
                    fs.put("javadoc", new JavaDocLinkFunction(isLocal));
                    fs.put("source", new SourceCodeInjectorFunction(isLocal));
                    fs.put("artifactVersion", new ArtifactVersionFunction());
                    return fs;
                }
            })
            .build());
    }
}

class ViewRendererImpl implements ViewRenderer {
    private static final Logger log = LoggerFactory.getLogger(ViewRendererImpl.class);

    private static final String muVersion = MuServer.artifactVersion();
    private final boolean isLocal;
    private final PebbleEngine engine;

    ViewRendererImpl(boolean isLocal, PebbleEngine engine) {
        this.isLocal = isLocal;
        this.engine = engine;
    }

    @Override
    public void render(MuResponse response, String relativePath, Map<String, Object> model) {
        response.contentType(ContentTypes.TEXT_HTML_UTF8);
        PebbleTemplate template = engine.getTemplate(relativePath + ".html");
        try (PrintWriter writer = response.writer()) {
            try {
                template.evaluate(writer, model, Locale.US);
            } catch (Exception e) {
                String id = UUID.randomUUID().toString();
                log.error("Error rending " + relativePath + " with ERRID=" + id, e);
                try {
                    writer.append("\"/><br><br><h1>Server Error</h1><p>Sorry, something went wrong. Error ID=").append(id).append("</p>");
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public MapBuilder model() {
        MapBuilder model = new MapBuilder();
        model.put("version", muVersion);
        model.put("isLocal", isLocal);
        return model;
    }
}

class ArtifactVersionFunction implements Function {

    @Override
    public Object execute(Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
        String groupId = (String) args.get("groupId");
        String artifactId = (String) args.get("artifactId");
        return version(groupId, artifactId);
    }

    @Override
    public List<String> getArgumentNames() {
        return Arrays.asList("groupId", "artifactId");
    }


    static String version(String groupId, String artifactId) {
        String v;
        try {
            Properties props = new Properties();
            InputStream in = MuServer.class.getResourceAsStream("/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");
            if (in == null) {
                v = "RELEASE";
            } else {
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
                v = props.getProperty("version");
            }
        } catch (Exception ex) {
            v = "RELEASE";
        }
        return v;
    }
}