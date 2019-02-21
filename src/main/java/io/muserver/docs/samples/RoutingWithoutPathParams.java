package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;

import static io.muserver.MuServerBuilder.httpServer;

public class RoutingWithoutPathParams {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler((req, resp) -> {
                String path = req.uri().getPath();
                if (path.equals("/handle-it") && req.method() == Method.GET) {
                    resp.contentType("text/html");
                    resp.write("This was handled by the <b>handle-it</b> handler");
                    return true;
                } else {
                    return false;
                }
            })
            .addHandler((req, resp) -> {
                resp.contentType("text/html");
                resp.write("This was handled by the <i>catch-all</i> handler");
                return true;
            })
            .start();
        System.out.println("Server started at " + server.uri().resolve("/handle-it"));
    }
}
