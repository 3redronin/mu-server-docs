package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.handlers.CSRFProtectionHandlerBuilder;

public class CSRFProtectionExample {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(CSRFProtectionHandlerBuilder.csrfProtection())
            .addHandler(Method.POST, "/submit", (request, response, pp) -> {
                response.write("Form submitted successfully!");
            })
            .start();
        System.out.println("Started server at " + server.uri());
    }
}