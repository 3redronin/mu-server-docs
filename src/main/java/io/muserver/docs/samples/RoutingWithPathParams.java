package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;

import static io.muserver.MuServerBuilder.httpServer;

public class RoutingWithPathParams {
    public static void main(String[] args) {
        MuServer server = httpServer()
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
            .start();
        System.out.println("Server started at " + server.uri().resolve("/routes/noparam"));
    }
}
