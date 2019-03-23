package io.muserver.docs;

import io.muserver.*;
import io.muserver.acme.AcmeCertManager;
import io.muserver.acme.AcmeCertManagerBuilder;
import io.muserver.docs.handlers.*;
import io.muserver.docs.samples.*;
import io.muserver.handlers.HttpsRedirectorBuilder;
import io.muserver.rest.CORSConfigBuilder;
import io.muserver.rest.RestHandlerBuilder;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.muserver.ContextHandlerBuilder.context;
import static io.muserver.MuServerBuilder.muServer;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        boolean isLocal = args.length == 1 && args[0].equals("local");

        ViewRenderer renderer = getTemplateLoader(isLocal);

        AcmeCertManager acmeCertManager = AcmeCertManagerBuilder.letsEncrypt()
            .withConfigDir(new File("letsencrypt"))
            .withDomain("muserver.io")
            .disable(isLocal) // when local, a no-op manager is returned
            .build();

        MuServer server = muServer()
            .withHttpPort(8080)
            .withHttpsPort(8443)
            .withHttpsConfig(acmeCertManager.createSSLContext())
            .addHandler((req, resp) -> {
                log.info("Recieved " + req + " from " + req.remoteAddress());
                return false;
            })
            .addHandler(acmeCertManager.createHandler())
            .addHandler(
                HttpsRedirectorBuilder.toHttpsPort(443)
                    .withHSTSExpireTime(365, TimeUnit.DAYS)
                    .includeSubDomains(true)
            )
            .addHandler(isLocal ? null : new ViewCacheHandler("/", "/download", "/https", "/jaxrs", "/resources",
                "/resources/mime-types", "/contexts", "/letsencrypt", "/uploads", "/sse", "/routes"))
            .addHandler(Method.GET, "/", new HomeHandler(renderer))
            .addHandler(Method.GET, "/async", new VanillaHandler(renderer, "async", "Asynchronous non-blocking request handling"))
            .addHandler(Method.GET, "/model", new VanillaHandler(renderer, "model", "Request and Response model"))
            .addHandler(Method.GET, "/model/query", new QueryStringExampleHandler())
            .addHandler(Method.GET, "/model/headers", new HeaderExampleHandler())
            .addHandler(Method.POST, "/model/forms", new FormDataExampleHandler())
            .addHandler(Method.GET, "/download", new VanillaHandler(renderer, "download", "Download Mu Server"))
            .addHandler(Method.GET, "/mutils", new MutilsHandler(renderer))
            .addHandler(Method.GET, "/https", new VanillaHandler(renderer, "https", "HTTPS Configuration"))
            .addHandler(Method.GET, "/jaxrs", new VanillaHandler(renderer, "jaxrs", "REST services with JAX-RS"))
            .addHandler(Method.GET, "/resources", new VanillaHandler(renderer, "resource-handling", "Static resource handling"))
            .addHandler(Method.GET, "/resources/mime-types", new MimeTypesHandler(renderer))
            .addHandler(Method.GET, "/statistics", new StatisticsHandler(renderer))
            .addHandler(Method.GET, "/contexts", new VanillaHandler(renderer, "contexts", "Path Contexts"))
            .addHandler(Method.GET, "/letsencrypt", new LetsEncryptHandler(renderer))
            .addHandler(Method.GET, "/uploads", new VanillaHandler(renderer, "upload", "File Uploads"))
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
            .addHandler(createRestHandler())
            .addHandler(context("jaxrsdocs").addHandler(JaxRSDocumentationExample.createRestHandler()))
            .addHandler(ResourceMimeTypes.resourceHandler())
            .start();

        acmeCertManager.start(server);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            acmeCertManager.stop();
            server.stop();
        }));

        log.info("Started at " + server.uri());

    }

    private static RestHandlerBuilder createRestHandler() {
        Map<Integer, String> users = new HashMap<>();
        users.put(1, "Mike");
        users.put(2, "Sam");
        users.put(3, "Dan");
        return RestHandlerBuilder.restHandler(new JaxRSExample.UserResource(users))
            .withCORS(
                CORSConfigBuilder.corsConfig()
                    .withAllowedOrigins("https://petstore.swagger.io")
                    .withAllowedOriginRegex("http(s)?://localhost:[0-9]+")
            )
            .withOpenApiJsonUrl("/openapi.json")
            .withOpenApiHtmlUrl("/api.html");
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
