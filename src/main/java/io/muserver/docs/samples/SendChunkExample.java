package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;

public class SendChunkExample {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(Method.GET, "/", (request, response, path) -> {

                response.contentType("text/html;charset=utf-8");
                response.sendChunk("Will send some chunks...<br>");
                for (int i = 0; i < 10; i++) {
                    response.sendChunk("Chunk " + i + "<br>");
                    Thread.sleep(500);
                }
                response.sendChunk("<br>Response complete.<br>");

            })
            .start();
        System.out.println("See some chunks at " + server.uri());
    }
}
