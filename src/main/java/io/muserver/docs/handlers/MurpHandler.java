package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import io.muserver.docs.ViewRenderer;
import io.muserver.murp.Murp;

import java.util.Map;

public class MurpHandler implements RouteHandler {
    private final ViewRenderer renderer;

    public MurpHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) {
        renderer.render(response, "murp",
            renderer.model()
                .with("title", "Murp: the reverse proxy handler")
                .with("murpversion", Murp.artifactVersion())
        );
    }

}
