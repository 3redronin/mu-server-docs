package io.muserver.docs.samples;

import io.muserver.*;
import io.muserver.handlers.ResourceCustomizer;
import io.muserver.handlers.ResourceHandlerBuilder;

public class ResourceHandlingWithCustomization {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(
                ResourceHandlerBuilder.fileHandler("/path/on/disk")
                    .withResourceCustomizer(new ResourceCustomizer() {
                        public void beforeHeadersSent(MuRequest request, Headers responseHeaders) {
                            if (request.relativePath().endsWith(".immutable.js")) {
                                responseHeaders.set(HeaderNames.CACHE_CONTROL,
                                    "max-age=31536000, public, immutable");
                            }
                        }
                    })
            )
            .start();
        System.out.println("Started server at " + server.uri());
    }
}