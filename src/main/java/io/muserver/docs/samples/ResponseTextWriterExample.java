package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;

import java.io.PrintWriter;

public class ResponseTextWriterExample {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(Method.GET, "/", (request, response, path) -> {

                response.contentType("text/html;charset=utf-8");
                try (PrintWriter writer = response.writer()) {
                    writer.append("Will send some messages...<br>");
                    for (int i = 0; i < 10; i++) {
                        writer.append("Sending message " + i + "<br>");
                        writer.flush();
                        Thread.sleep(500);
                    }
                    writer.append("<br>Response complete.<br>");
                }

            })
            .start();
        System.out.println("See some streaming at " + server.uri());
    }
}
