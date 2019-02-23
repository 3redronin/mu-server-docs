package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.SsePublisher;
import io.muserver.handlers.ResourceHandlerBuilder;

import static io.muserver.MuServerBuilder.httpServer;

public class ServerSentEventsExample {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler(Method.GET, "/sse/counter", (request, response, pathParams) -> {

                SsePublisher publisher = SsePublisher.start(request, response);
                new Thread(() -> count(publisher)).start();

            })
            .addHandler(ResourceHandlerBuilder.fileOrClasspath("src/main/resources/samples", "/samples"))
            .start();
        System.out.println("Open " + server.uri().resolve("/sse.html") + " to see some numbers.");
    }

    public static void count(SsePublisher publisher) {
        for (int i = 0; i < 100; i++) {
            try {
                publisher.send("Number " + i);
                Thread.sleep(1000);
            } catch (Exception e) {
                // The user has probably disconnected so stopping
                break;
            }
        }
        publisher.close();
    }
}
