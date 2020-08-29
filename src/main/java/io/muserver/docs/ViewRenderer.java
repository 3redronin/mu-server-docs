package io.muserver.docs;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.extension.Function;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.loader.FileLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.muserver.ContentTypes;
import io.muserver.MuResponse;
import io.muserver.MuServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

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