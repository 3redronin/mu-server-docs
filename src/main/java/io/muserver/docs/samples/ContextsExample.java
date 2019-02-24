package io.muserver.docs.samples;

import io.muserver.*;

public class ContextsExample {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(Method.GET, "/something", (request, response, pathParams) -> {
                sendInfo(request, response, "No context");
            })
            .addHandler(
                ContextHandlerBuilder.context("api")
                    .addHandler(Method.GET, "/something", (request, response, pathParams) -> {
                        sendInfo(request, response, "First level context");
                    })
                    .addHandler(
                        ContextHandlerBuilder.context("nested")
                            .addHandler(Method.GET, "/something", (request, response, pathParams) -> {
                                sendInfo(request, response, "Nested context");
                            })
                    )
            )
            .start();
        System.out.println("Started server at " + server.uri().resolve("/something"));
    }

    private static void sendInfo(MuRequest request, MuResponse response, String description) {
        response.contentType("text/html");
        response.write(description + "<br>" +
            "Full path: " + request.uri().getPath() + "<br>" +
            "Context path: " + request.contextPath() + "<br>" +
            "Relative path: " + request.relativePath()
        );
    }
}