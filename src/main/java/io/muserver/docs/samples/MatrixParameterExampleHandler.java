package io.muserver.docs.samples;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import io.muserver.rest.PathMatch;
import io.muserver.rest.UriPattern;

import javax.ws.rs.core.PathSegment;
import java.util.Map;

// Run RequestExample.java to test this

public class MatrixParameterExampleHandler implements RouteHandler {
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) {
        PathMatch matcher = UriPattern.uriTemplateToRegex("/search/{type}/{subtype}").matcher(request.uri());
        if (matcher.fullyMatches()) {
            PathSegment typeSegment = matcher.segments().get("type");
            response.sendChunk("Search type=" + typeSegment.getPath());
            for (String rating : typeSegment.getMatrixParameters().get("rating")) {
                response.sendChunk("; rating=" + rating);
            }
            String hasPool = typeSegment.getMatrixParameters().getFirst("hasPool");
            response.sendChunk("; hasPool=" + hasPool + "\n\n");

            PathSegment subTypeSegment = matcher.segments().get("subtype");
            String beds = subTypeSegment.getMatrixParameters().getFirst("beds");
            response.sendChunk("Subtype=" + subTypeSegment.getPath() + " with beds=" + beds);
        } else {
            response.write("URL didn't match");
        }
    }
}
