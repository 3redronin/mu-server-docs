package io.muserver.docs.samples;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

// Run RequestExample.java to test this

public class FormDataExampleHandler implements RouteHandler {
    public void handle(MuRequest request, MuResponse response, Map<String,String> pathParams) throws IOException {

        // Returns null if there is no parameter with that value
        String something = request.form().get("something");

        // Specifying a default value:
        String somethingElse = request.form().get("something", "default value");

        // Gets a number, or returns the default value if it's missing or not a number.
        // There are also getFloat, getDouble, getLong and getBoolean methods
        int intValue = request.form().getInt("numberValue", 42);

        boolean checkboxValue = request.form().getBoolean("checkboxValue");

        response.sendChunk(something + "\n" + somethingElse + "\n" + intValue + "\n" + checkboxValue);

        // You can loop through all the form data
        response.sendChunk("\n\nLooping through all form values:");
        for (Map.Entry<String, List<String>> entry : request.form().all().entrySet()) {
            response.sendChunk('\n' + entry.getKey() + "=" + entry.getValue());
        }
    }
}
