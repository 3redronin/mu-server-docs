package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuStats;

import static io.muserver.MuServerBuilder.httpServer;

public class StatisticsExample {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler((req, resp) -> {
                MuStats stats = req.server().stats();
                long completedRequests = stats.completedRequests();
                long bytesRead = stats.bytesRead();
                long bytesSent = stats.bytesSent();
                resp.write("There have been " + completedRequests + " requests with " + bytesRead
                    + " uploaded and " + bytesSent + " downloaded.");
                return true;
            })
            .start();
        System.out.println("Server started at " + server.uri().resolve("/handle-it"));
    }
}
