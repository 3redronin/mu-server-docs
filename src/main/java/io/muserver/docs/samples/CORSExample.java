package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.handlers.CORSHandlerBuilder;
import io.muserver.rest.CORSConfig;
import io.muserver.rest.CORSConfigBuilder;
import io.muserver.rest.RestHandlerBuilder;

public class CORSExample {
    public static void main(String[] args) {

        CORSConfig corsConfig = CORSConfigBuilder.corsConfig()
            // the origins allowed (no need to specify your own origin)
            .withAllowedOrigins("https://example.com")
            // also allow requests from localhost (useful for development)
            .withLocalhostAllowed()
            // the headers that cross origins can send
            .withAllowedHeaders("Content-Type", "Authorization")
            // the headers that are allowed to be read by the browser
            .withExposedHeaders("X-Custom-Header")
            // allow cookies and credentials to be sent by the browser
            .withAllowCredentials(true)
            // Cache preflight response for 1 hour
            .withMaxAge(3600)
            .build();

        // Preferred way to configure CORS when using jax-rs
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(RestHandlerBuilder.restHandler(/* resources */)
                .withCORS(corsConfig)
            )
            .start();

        // Or, if you are not using jax-rs, you can add a CORS handler in which case the methods
        // need to be added manually
        server = MuServerBuilder.httpServer()
            .addHandler(CORSHandlerBuilder.corsHandler()
                .withAllowedMethods(Method.GET, Method.HEAD, Method.POST)
                .withCORSConfig(corsConfig)
            )
            .addHandler(Method.GET, "/", (request, response, pathParams) -> {
                response.write("Hello, world");
            })
            .start();
        System.out.println("Started server at " + server.uri());
    }
}