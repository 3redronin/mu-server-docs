package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.handlers.ResourceHandlerBuilder;

public class ResourceHandling {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(ResourceHandlerBuilder.fileHandler("/path/on/disk"))
            .start();
        System.out.println("Started server at " + server.uri());
    }
}