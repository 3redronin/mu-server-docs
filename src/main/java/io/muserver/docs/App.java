package io.muserver.docs;

import io.muserver.MuServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.muserver.MuServerBuilder.muServer;
import static io.muserver.handlers.ResourceHandlerBuilder.fileOrClasspath;

public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {

        MuServer server = muServer()
                .withHttpPort(8080)
                .withHttpsPort(8443)
                .addShutdownHook(true)
                .addHandler((req, resp) -> {
                    log.info("Recieved " + req + " from " + req.remoteAddress());
                    return false;
                })
                .addHandler(fileOrClasspath("src/main/resources/web", "/web"))
                .start();

        log.info("Started at " + server.uri());

    }

}
