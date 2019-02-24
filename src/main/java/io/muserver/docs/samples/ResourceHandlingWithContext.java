package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.handlers.ResourceHandlerBuilder;

import static io.muserver.ContextHandlerBuilder.context;

public class ResourceHandlingWithContext {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(context("static")
                .addHandler(ResourceHandlerBuilder.classpathHandler("/web"))
            )
            .start();
        System.out.println("Stylesheet available at "
            + server.uri().resolve("/static/mu.css"));
    }
}