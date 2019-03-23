package io.muserver.docs.samples;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;

import java.util.List;
import java.util.Map;

// Run RequestExample.java to test this

public class QueryStringExampleHandler implements RouteHandler {
    public void handle(MuRequest request, MuResponse response, Map<String,String> pathParams) {

        // Returns null if there is no parameter with that value
        String something = request.query().get("something");

        // Specifying a default value:
        String somethingElse = request.query().get("something", "default value");

        // Getting a list, e.g. for ?something=value1&something=value2
        List<String> somethingList = request.query().getAll("something");

        // Gets a number, or returns the default value if it's missing or not a number.
        // There are also getFloat, getDouble, getLong and getBoolean methods
        int intValue = request.query().getInt("something", 42);

        response.sendChunk(something + " / " + somethingElse + " / " + somethingList + " / " + intValue);

        // You can loop through all query string values
        for (Map.Entry<String, List<String>> entry : request.query().all().entrySet()) {
            response.sendChunk('\n' + entry.getKey() + "=" + entry.getValue());
        }
    }
}
