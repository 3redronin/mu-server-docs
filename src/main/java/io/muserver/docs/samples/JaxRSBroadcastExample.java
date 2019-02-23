package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.handlers.ResourceHandlerBuilder;
import io.muserver.rest.MuRuntimeDelegate;
import io.muserver.rest.RestHandlerBuilder;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.muserver.MuServerBuilder.httpServer;

public class JaxRSBroadcastExample {

    public static void main(String[] args) {
        TimeResource timeResource = new TimeResource();
        timeResource.start();

        MuServer server = httpServer()
            .addHandler(RestHandlerBuilder.restHandler(timeResource))
            .addHandler(ResourceHandlerBuilder.fileOrClasspath("src/main/resources/samples", "/samples"))
            .start();
        System.out.println("Example started at " + server.uri().resolve("/sse.html"));
    }

}

@Path("/sse/counter")
class TimeResource {

    long count = 0;
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    Sse sse = MuRuntimeDelegate.createSseFactory();
    SseBroadcaster broadcaster = sse.newBroadcaster();

    @GET
    @Produces("text/event-stream")
    public void registerListener(@Context SseEventSink eventSink) {
        broadcaster.register(eventSink);
    }

    public void start() {
        executor.scheduleAtFixedRate(() -> {
            count++;
            String data = "Number " + count;
            broadcaster.broadcast(sse.newEvent(data));
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

}
