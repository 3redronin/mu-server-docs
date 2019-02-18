package io.muserver.docs;

import io.muserver.Method;
import io.muserver.MuServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.muserver.MuServerBuilder.muServer;
import static io.muserver.handlers.ResourceHandlerBuilder.fileOrClasspath;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        boolean isLocal = args.length == 1 && args[0].equals("local");

        MuServer server = muServer()
                .withHttpPort(8080)
                .withHttpsPort(8443)
                .addShutdownHook(true)
                .addHandler(Method.GET, "/", new HomeHandler(isLocal))
                .addHandler((req, resp) -> {
                    log.info("Recieved " + req + " from " + req.remoteAddress());
                    return false;
                })
                .addHandler(fileOrClasspath("src/main/resources/web", "/web"))
                .start();

        log.info("Started at " + server.uri());

    }

}
