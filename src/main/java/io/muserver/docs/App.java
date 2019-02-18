package io.muserver.docs;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.Mutils;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.DefaultEnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static io.muserver.MuServerBuilder.muServer;
import static io.muserver.handlers.ResourceHandlerBuilder.fileOrClasspath;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {

        boolean isLocal = args.length == 1 && args[0].equals("local");

        MuServer server = muServer()
                .withHttpPort(8080)
                .withHttpsPort(8443)
                .addShutdownHook(true)
                .addHandler(Method.GET, "/", new HomeHandler(getTemplateLoader(isLocal)))
                .addHandler((req, resp) -> {
                    log.info("Recieved " + req + " from " + req.remoteAddress());
                    return false;
                })
                .addHandler(fileOrClasspath("src/main/resources/web", "/web"))
                .start();

        log.info("Started at " + server.uri());

    }

    public static JtwigModel model() {
        return JtwigModel.newModel()
            .with("version", "0.19.3");
    }


    private static TemplateLoader getTemplateLoader(boolean isLocal) throws IOException {
        if (isLocal) {
            File viewBase = new File("src/main/resources/views").getCanonicalFile();
            if (!viewBase.isDirectory()) {
                throw new RuntimeException("Could not find " + Mutils.fullPath(viewBase));
            }
            EnvironmentConfiguration config = EnvironmentConfigurationBuilder.configuration()
                .parser().withoutTemplateCache()
                .and()
                .build();
            return relativePath -> JtwigTemplate.fileTemplate(new File(viewBase, relativePath + ".html"), config);
        } else {
            EnvironmentConfiguration config = new DefaultEnvironmentConfiguration();
            return relativePath -> JtwigTemplate.classpathTemplate("/views/" + relativePath + ".html", config);
        }
    }

}
