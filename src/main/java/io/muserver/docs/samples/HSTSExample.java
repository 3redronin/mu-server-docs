package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.handlers.HttpsRedirectorBuilder;

import java.util.concurrent.TimeUnit;

import static io.muserver.MuServerBuilder.muServer;

public class HSTSExample {
    public static void main(String[] args) {
        MuServer server = muServer()
            .withHttpPort(20080)
            .withHttpsPort(20443)
            .addHandler(
                HttpsRedirectorBuilder.toHttpsPort(20443)
                    .withHSTSExpireTime(365, TimeUnit.DAYS)
                    .includeSubDomains(true)
            )
            .addHandler(Method.GET, "/", (req, resp, pp) -> resp.write("This is HTTPS"))
            .start();
        System.out.println("Server started at " + server.httpUri());
    }
}
