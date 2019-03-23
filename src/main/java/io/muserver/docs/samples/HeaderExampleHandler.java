package io.muserver.docs.samples;

import io.muserver.*;

import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

// Run RequestExample.java to test this

public class HeaderExampleHandler implements RouteHandler {
    public void handle(MuRequest request, MuResponse response, Map<String,String> pathParams) {

        // Returns null if there is no parameter with that value
        String userAgent = request.headers().get("User-Agent");

        // Specifying a default value:
        String defaultValue = request.headers().get("something", "default value");

        // Getting a list where a header has been added multiple times
        List<String> somethingList = request.headers().getAll("something");

        // Gets a number, or returns the default value if it's missing or not a number.
        int intValue = request.headers().getInt("something", 42);

        response.sendChunk(userAgent + "\n" + defaultValue + "\n" + somethingList + "\n" + intValue + "\n\n");

        // Parsers exist for some header values
        List<ParameterizedHeaderWithValue> accepts = request.headers().accept();
        for (ParameterizedHeaderWithValue accept : accepts) {
            String q = accept.parameter("q", "1.0");
            response.sendChunk("Accept " + accept.value() + " with q=" + q + "\n");
        }

        List<ParameterizedHeaderWithValue> acceptLanguages = request.headers().acceptLanguage();
        for (ParameterizedHeaderWithValue acceptLanguage : acceptLanguages) {
            String q = acceptLanguage.parameter("q", "1.0");
            response.sendChunk("Accept language: " + acceptLanguage.value() + " with q=" + q + "\n");
        }

        MediaType mediaType = request.headers().contentType();
        response.sendChunk("The content type of the request body is " + mediaType + "\n");

        ParameterizedHeader cacheControl = request.headers().cacheControl();
        response.sendChunk("Cache control: " + cacheControl.parameters() + "\n");

        List<ForwardedHeader> forwarded = request.headers().forwarded();
        response.sendChunk("Forwarded headers: " + ForwardedHeader.toString(forwarded) + "\n");

        // Or just loop through everything
        response.sendChunk("\n\nLooping through all headers:\n");
        for (Map.Entry<String, String> entry : request.headers()) {
            response.sendChunk(entry.getKey() + "=" + entry.getValue() + "\n");
        }
    }
}
