package io.muserver.docs.samples;

import io.muserver.Headers;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.handlers.ResourceHandlerBuilder;
import io.muserver.handlers.ResourceType;

import java.util.Map;

import static java.util.Arrays.asList;

public class ResourceMimeTypes {
    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(resourceHandler())
            .start();
        System.out.println("Started server at " + server.uri());
    }

    public static ResourceHandlerBuilder resourceHandler() {
        Map<String, ResourceType> resourceTypes = ResourceType.getDefaultMapping();
        boolean useGzip = false;
        resourceTypes.put("dan",
            new ResourceType("text/plain",
                Headers.http2Headers()
                    .add("cache-control", "max-age=300")
                    .add("x-custom-header", "danbo"),
                useGzip, asList("dan")
            ));
        return ResourceHandlerBuilder
            .fileOrClasspath("src/main/resources/web", "/web")
            .withExtensionToResourceType(resourceTypes);
    }
}