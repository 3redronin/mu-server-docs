package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import io.muserver.docs.ViewRenderer;
import io.muserver.handlers.ResourceType;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

public class MimeTypesHandler implements RouteHandler {
    private final ViewRenderer renderer;

    public MimeTypesHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) throws Exception {
        renderer.render(response, "mime-types",
            renderer.model()
                .with("title", "Mime Types")
                .with("types", ResourceType.getResourceTypes().stream()
                    .map(rt -> {
                        String extensions = rt.extensions().isEmpty() ? "(default)" : String.join("<br>", rt.extensions);
                        String headers = rt.headers().entries().stream()
                            .map(e -> e.getKey() + ": " + e.getValue())
                            .collect(Collectors.joining("<br>"));
                        return new TypeModel(rt.mimeType().toString(), extensions, rt.gzip() ? "Yes" : "No", headers);
                    })
                    .sorted(Comparator.comparing(t -> t.mimeType))
                    .collect(Collectors.toList())
                )
        );

    }

    private static class TypeModel {
        public final String mimeType;
        public final String extensions;
        public final String gzip;
        public final String headers;

        private TypeModel(String mimeType, String extensions, String gzip, String headers) {
            this.mimeType = mimeType;
            this.extensions = extensions;
            this.gzip = gzip;
            this.headers = headers;
        }
    }
}
