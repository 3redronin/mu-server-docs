package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;

import static io.muserver.MuServerBuilder.muServer;

public class HttpsSelfSignedCert {
    public static void main(String[] args) {
        MuServer server = muServer()
            .withHttpsPort(9443)
            .addHandler(Method.GET, "/", (req, resp, pp) -> resp.write("This is HTTPS"))
            .start();
        System.out.println("Server started at " + server.uri());
    }
}
