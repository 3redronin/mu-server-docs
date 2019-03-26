package io.muserver.docs.samples;

import io.muserver.ContentTypes;
import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;

import java.io.InputStream;
import java.io.OutputStream;

public class ResponseOutputStreamExample {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(Method.GET, "/logo.png", (request, response, path) -> {

                response.contentType(ContentTypes.IMAGE_PNG);
                try (
                    InputStream inputStream = ResponseOutputStreamExample.class
                        .getResourceAsStream("/web/logos/android-chrome-512x512.png");
                    OutputStream responseStream = response.outputStream()) {

                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = inputStream.read(buffer)) > -1) {
                        responseStream.write(buffer, 0, read);
                    }

                }

            })
            .start();
        System.out.println("Download the logo at " + server.uri().resolve("/logo.png"));
    }
}
