package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import io.muserver.docs.ViewRenderer;

import java.util.Map;

public class VanillaHandler implements RouteHandler {
    private final ViewRenderer renderer;
    private final String viewName;
    private final String title;

    public VanillaHandler(ViewRenderer renderer, String viewName, String title) {
        this.renderer = renderer;
        this.viewName = viewName;
        this.title = title;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) {
        renderer.render(response, viewName,
            renderer.model().with("title", title)
        );

    }

}
