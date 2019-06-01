package io.muserver.docs.samples;

import io.muserver.Http2ConfigBuilder;
import io.muserver.Method;
import io.muserver.MuServer;

import static io.muserver.MuServerBuilder.httpsServer;

public class Http2Example {
    public static void main(String[] args) {
        MuServer server = httpsServer()
            .withHttp2Config(Http2ConfigBuilder.http2EnabledIfAvailable())
            .addHandler(Method.GET, "/", (req, resp, pp) -> {
                resp.write("The HTTP protocol is " + req.protocol());
            })
            .start();
        System.out.println("Server started at " + server.uri());
    }
}
