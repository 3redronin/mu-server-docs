package io.muserver.docs.handlers;

import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.RouteHandler;
import io.muserver.acme.AcmeCertManager;
import io.muserver.docs.ViewRenderer;

import java.util.Map;

public class LetsEncryptHandler implements RouteHandler {
    private final ViewRenderer renderer;

    public LetsEncryptHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) {
        renderer.render(response, "letsencrypt",
            renderer.model()
                .with("title", "Free, automated SSL certs with Let's Encrypt")
                .with("acmeversion", AcmeCertManager.artifactVersion())
        );
    }

}
