package io.muserver.docs;

import io.muserver.*;
import io.muserver.docs.handlers.*;
import io.muserver.docs.samples.ResourceMimeTypes;
import io.muserver.docs.samples.ServerSentEventsExample;
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
            .addHandler(Method.GET, "/download", new VanillaHandler(renderer, "download", "Download Mu Server"))
            .addHandler(Method.GET, "/mutils", new MutilsHandler(renderer))
            .addHandler(Method.GET, "/https", new VanillaHandler(renderer, "https", "HTTPS Configuration"))
            .addHandler(Method.GET, "/resources", new VanillaHandler(renderer, "resource-handling", "Static resource handling"))
            .addHandler(Method.GET, "/resources/mime-types", new MimeTypesHandler(renderer))
            .addHandler(Method.GET, "/statistics", new StatisticsHandler(renderer))
            .addHandler(Method.GET, "/contexts", new VanillaHandler(renderer, "contexts", "Path Contexts"))
            .addHandler(Method.GET, "/uploads", new VanillaHandler(renderer, "upload", "File Uploads"))
            .addHandler(Method.GET, "/sse", new VanillaHandler(renderer, "sse", "Server Sent Events"))
            .addHandler(Method.GET, "/sse", new VanillaHandler(renderer, "sse", "Server Sent Events"))
            .addHandler(Method.GET, "/sse/counter", (request, response, pathParams) -> {
                SsePublisher publisher = SsePublisher.start(request, response);
                new Thread(() -> ServerSentEventsExample.count(publisher)).start();
            })

            .addHandler(Method.GET, "/routes", new VanillaHandler(renderer, "routing", "Routing with path parameters"))
            .addHandler(Method.GET, "/routes/noparam", (req, resp, pathParams) -> {
                resp.write("No parameters");
            })
            .addHandler(Method.GET, "/routes/strings/{name}", (req, resp, pathParams) -> {
                String name = pathParams.get("name");
                resp.write("The name is: " + name);
            })
            .addHandler(Method.GET, "/routes/numbers/{id : [0-9]+}", (req, resp, pathParams) -> {
                int id = Integer.parseInt(pathParams.get("id"));
                resp.write("The ID is: " + id);
            })
            .addHandler(ResourceMimeTypes.resourceHandler())
            .start();

        log.info("Started at " + server.uri());

    }

    private static ViewRenderer getTemplateLoader(boolean isLocal) throws IOException {
        File viewBase;
        EnvironmentConfigurationBuilder builder = EnvironmentConfigurationBuilder.configuration()
            .escape().withInitialEngine("html")
            .and()
            .functions().add(new SourceCodeInjector(isLocal)).add(new JavaDocLink())
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

        String muVersion = MuServer.artifactVersion();

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
                    .with("version", muVersion)
                    .with("isLocal", isLocal);
            }
        };


    }

}
