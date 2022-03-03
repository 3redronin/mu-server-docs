package io.muserver.docs.samples;

import io.muserver.*;
import io.muserver.handlers.ResourceHandlerBuilder;

import static io.muserver.MuServerBuilder.httpServer;

public class WebSocketExample {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler(
                WebSocketHandlerBuilder.webSocketHandler()
                    .withPath("/echo-socket")
                    .withWebSocketFactory(new MuWebSocketFactory() {
                        @Override
                        public MuWebSocket create(MuRequest request, Headers responseHeaders) {
                            return new BaseWebSocket() {
                                @Override
                                public void onText(String message, boolean isLast, DoneCallback onComplete) {
                                    session().sendText("Received " + message, onComplete);
                                }
                            };
                        }
                    })
            )
            .addHandler(ResourceHandlerBuilder.fileOrClasspath("src/main/resources/samples", "/samples"))
            .start();
        System.out.println("Open " + server.uri().resolve("/websocket.html") + " to try.");
    }
}
