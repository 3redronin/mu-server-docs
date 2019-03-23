package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;

import static io.muserver.MuServerBuilder.httpServer;

public class RequestExample {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler(Method.GET, "/query", new QueryStringExampleHandler())
            .addHandler(Method.POST, "/forms", new FormDataExampleHandler())
            .addHandler(Method.GET, "/headers", new HeaderExampleHandler())
            .start();
        System.out.println("Query string example: " + server.uri().resolve("/query?something=hello&something=something%20else&another=more"));
        System.out.println("Form data example: " + server.uri().resolve("/forms"));
        System.out.println("Header example: " + server.uri().resolve("/headers"));
    }
}
