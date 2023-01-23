package io.muserver.docs.handlers;

import io.muserver.*;
import io.muserver.docs.ViewRenderer;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class HttpsHandler implements RouteHandler {
    private final ViewRenderer renderer;

    public HttpsHandler(ViewRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) {

        SSLInfo sslInfo = request.server().sslInfo();
        renderer.render(response, "https",
            renderer.model()
                .with("title", "HTTPS Configuration")
                .with("sslProvider", sslInfo.providerName())
                .with("sslProtocols", String.join(", ", sslInfo.protocols()))
                .with("sslCiphers", String.join(", ", sslInfo.ciphers()))
                .with("certs", sslInfo.certificates().stream().map(cert -> cert.getSubjectX500Principal() + " - from " + cert.getNotBefore() + " to " + cert.getNotAfter()).collect(Collectors.toList()))
        );
    }




}
