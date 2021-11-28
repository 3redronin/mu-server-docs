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
            .addHandler(Method.GET, "/model/cookie", new CookieExampleHandler())
            .addHandler(Method.GET, "/search/{type}/{subtype}", new MatrixParameterExampleHandler())
            .start();
        System.out.println("Query string example: " + server.uri().resolve("/query?something=hello&something=something%20else&another=more"));
        System.out.println("Form data example (requires POST): " + server.uri().resolve("/forms"));
        System.out.println("Header example: " + server.uri().resolve("/headers"));
        System.out.println("View cookie example: " + server.uri().resolve("/model/cookie?action=view"));
        System.out.println("Create cookie example: " + server.uri().resolve("/model/cookie?action=create"));
        System.out.println("Delete cookie example: " + server.uri().resolve("/model/cookie?action=delete"));
        System.out.println("Matrix parameter example: " + server.uri().resolve("/search/hotels;rating=5;rating=4;hasPool=true/suites;beds=2"));
    }
}
