package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import io.muserver.docs.ViewRenderer;

import java.util.Map;

public class HomeHandler implements RouteHandler {

    private final ViewRenderer renderer;
    public HomeHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) throws Exception {
        renderer.render(response, "home", renderer.model());
    }

}
