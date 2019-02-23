package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.MuStats;
import io.muserver.RouteHandler;
import io.muserver.docs.ViewRenderer;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

public class StatisticsHandler implements RouteHandler {
    private final ViewRenderer renderer;
    private final NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);

    public StatisticsHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) {
        MuStats stats = request.server().stats();
        renderer.render(response, "statistics",
            renderer.model()
                .with("title", "Server Statistics")
                .with("activeConnections", formatter.format(stats.activeConnections()))
                .with("completedRequests", formatter.format(stats.completedRequests()))
                .with("bytesUploaded", formatter.format(stats.bytesRead()))
                .with("bytesDownloaded", formatter.format(stats.bytesSent()))
        );
    }
}
