package io.muserver.docs.samples;

import io.muserver.*;

public class Hello {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(Method.GET, "/", (request, response, pathParams) -> {
                response.write("Hello, world");
            })
            .start();
        System.out.println("Started server at " + server.uri());
    }
}