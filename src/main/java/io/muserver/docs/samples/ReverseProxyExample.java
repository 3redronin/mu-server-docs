package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.murp.Murp;
import io.muserver.murp.ReverseProxyBuilder;

import static io.muserver.MuServerBuilder.httpServer;
import static io.muserver.MuServerBuilder.httpsServer;

public class ReverseProxyExample {
    public static void main(String[] args) {

        // Start a target server which requests will be proxied to
        MuServer targetServer = httpServer()
            .addHandler((req, resp) -> {
                resp.sendChunk("Received " + req + " with headers:\n\n");
                req.headers().forEach(e -> resp.sendChunk(e.getKey() + "=" + e.getValue() + "\n"));
                return true;
            })
            .start();
        System.out.println("The target server is at " + targetServer.uri());


        // Start a reverse proxy that sends all requests to the target server
        MuServer reverseProxy = httpsServer()
            .addHandler((req, resp) -> {
                req.headers().set("X-Send-To-Target", "A value sent to the target");
                resp.headers().set("X-Send-To-Client", "A value returned to the client");
                return false;
            })
            .addHandler(
                ReverseProxyBuilder.reverseProxy()
                    .withUriMapper(request -> {
                        // This maps the client's URI to the target URI, which in this demo
                        // is simply the target server's URI with the client's path and querystring
                        // appended.
                        String pathAndQuery = Murp.pathAndQuery(request.uri());
                        return targetServer.uri().resolve(pathAndQuery);
                    })
                    .sendLegacyForwardedHeaders(true) // Adds X-Forwarded-*
                    .withViaName("myreverseproxy")
                    .proxyHostHeader(false)
            )
            .start();


        System.out.println("Reverse proxy started at " + reverseProxy.uri());
        System.out.println("Example URLs: " + reverseProxy.uri().resolve("/blah") + " / "
            + reverseProxy.uri().resolve("/blah?greeting=hello%20world"));
    }
}
