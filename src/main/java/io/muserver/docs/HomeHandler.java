package io.muserver.docs;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import org.jtwig.JtwigModel;

import java.util.Map;

public class HomeHandler implements RouteHandler {

    private final TemplateLoader loader;
    public HomeHandler(TemplateLoader loader) {
        this.loader = loader;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) throws Exception {
        JtwigModel model = App.model();
        response.contentType("text/html");
        loader.load("home").render(model, response.outputStream());
    }

}
