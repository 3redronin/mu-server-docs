package io.muserver.docs;

import io.muserver.*;
import io.muserver.docs.handlers.HomeHandler;
import io.muserver.docs.handlers.MimeTypesHandler;
import io.muserver.docs.handlers.MutilsHandler;
import io.muserver.docs.handlers.ResourceHandlingHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static io.muserver.MuServerBuilder.muServer;
import static io.muserver.handlers.ResourceHandlerBuilder.fileOrClasspath;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        boolean isLocal = args.length == 1 && args[0].equals("local");

        ViewRenderer renderer = getTemplateLoader(isLocal);

        MuServer server = muServer()
            .withHttpPort(8080)
            .withHttpsPort(8443)
            .addShutdownHook(true)
            .addHandler((req, resp) -> {
                log.info("Recieved " + req + " from " + req.remoteAddress());
                return false;
            })
            .addHandler(Method.GET, "/", new HomeHandler(renderer))
            .addHandler(Method.GET, "/mutils", new MutilsHandler(renderer))
            .addHandler(Method.GET, "/resources", new ResourceHandlingHandler(renderer))
            .addHandler(Method.GET, "/resources/mime-types", new MimeTypesHandler(renderer))
            .addHandler(fileOrClasspath("src/main/resources/web", "/web"))
            .start();

        log.info("Started at " + server.uri());

    }

    private static ViewRenderer getTemplateLoader(boolean isLocal) throws IOException {
        File viewBase;
        EnvironmentConfigurationBuilder builder = EnvironmentConfigurationBuilder.configuration()
            .escape().withInitialEngine("html")
            .and()
            .functions().add(new SourceCodeInjector(isLocal))
            .and();
        if (isLocal) {
            viewBase = new File("src/main/resources/views").getCanonicalFile();
            if (!viewBase.isDirectory()) {
                throw new RuntimeException("Could not find " + Mutils.fullPath(viewBase));
            }
            builder = builder
                .parser().withoutTemplateCache()
                .and();

        } else {
            viewBase = null;
        }
        EnvironmentConfiguration config = builder.build();

        return new ViewRenderer() {
            @Override
            public void render(MuResponse response, String relativePath, JtwigModel model) {
                response.contentType(ContentTypes.TEXT_HTML);
                JtwigTemplate template;
                if (isLocal) {
                    template = JtwigTemplate.fileTemplate(new File(viewBase, relativePath + ".html"), config);
                } else {
                    template = JtwigTemplate.classpathTemplate("/views/" + relativePath + ".html", config);
                }
                try (OutputStream out = new BufferedOutputStream(response.outputStream(), 4096)) {
                    template.render(model, out);
                } catch (IOException e) {
                    log.info("Error closing stream: " + e.getMessage());
                }
            }

            @Override
            public JtwigModel model() {
                return new JtwigModel()
                    .with("version", "0.19.4");
            }
        };


    }

}
