package io.muserver.docs;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import org.jtwig.JtwigModel;

import java.util.Map;

public class HomeHandler extends ViewHandler implements RouteHandler {

    public HomeHandler(boolean isLocal) {
        super(isLocal);
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) throws Exception {
        JtwigModel model = model();
        render(response, "home", model);
    }



}
