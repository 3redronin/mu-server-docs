package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.Mutils;
import io.muserver.RouteHandler;
import io.muserver.docs.ViewRenderer;

import java.util.Date;
import java.util.Map;

import static java.util.Arrays.asList;

public class MutilsHandler implements RouteHandler {
    private final ViewRenderer renderer;

    public MutilsHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) throws Exception {
        Date now = new Date();
        renderer.render(response, "mutils",
            renderer.model()
                .with("title", "Mutils")
                .with("examples", asList(
                    new Example("Encoding & Decoding", "Mutils.urlEncode(\"Hello world!\")", Mutils.urlEncode("Hello world!")),
                    new Example("", "Mutils.urlDecode(\"Hello%20world%21\")", Mutils.urlDecode("Hello%20world!")),
                    new Example("", "Mutils.htmlEncode(\"<b>This & that</b>\")", Mutils.htmlEncode("<b>This & that</b>")),
                    new Example("HTTP Dates", "Mutils.toHttpDate(new Date())", Mutils.toHttpDate(now)),
                    new Example("", "Mutils.fromHttpDate(\"" + Mutils.toHttpDate(now) + "\").toString()", Mutils.fromHttpDate(Mutils.toHttpDate(now)).toString()),
                    new Example("Object Utils", "Mutils.coalesce(null, null, \"First non-null value\")", Mutils.coalesce(null, null, "First non-null value")),
                    new Example("String Utils", "Mutils.nullOrEmpty(\"\")", String.valueOf(Mutils.nullOrEmpty(""))),
                    new Example("", "Mutils.hasValue(\"Something\")", String.valueOf(Mutils.hasValue("Something"))),
                    new Example("", "Mutils.trim(\"/some/path/\", \"/\")", Mutils.trim("/some/path/", "/"))


                ))
        );


    }

    private static class Example {
        public final String area;
        public final String input;
        public final String output;

        private Example(String area, String input, String output) {
            this.area = area;
            this.input = input;
            this.output = output;
        }
    }
}
