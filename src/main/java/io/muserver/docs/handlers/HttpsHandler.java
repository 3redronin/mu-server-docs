package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import io.muserver.docs.ViewRenderer;

import java.util.Map;

public class HttpsHandler implements RouteHandler {
    private final ViewRenderer renderer;

    public HttpsHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) throws Exception {
        renderer.render(response, "https",
            renderer.model()
                .with("title", "HTTPS Configuration")
        );

    }

}
