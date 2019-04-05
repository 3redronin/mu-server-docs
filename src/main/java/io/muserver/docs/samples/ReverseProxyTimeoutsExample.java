package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.murp.HttpClientBuilder;
import io.muserver.murp.ReverseProxyBuilder;
import io.muserver.murp.UriMapper;

import java.net.URI;

import static io.muserver.MuServerBuilder.httpServer;

public class ReverseProxyTimeoutsExample {
    public static void main(String[] args) {
        MuServer reverseProxy = httpServer()
            .addHandler(
                ReverseProxyBuilder.reverseProxy()
                    .withUriMapper(UriMapper.toDomain(URI.create("http://example.org")))
                    .proxyHostHeader(false)
                    .withTotalTimeout(20000) // 20 seconds
                    .withHttpClient(
                        HttpClientBuilder.httpClient()
                            .withAddressResolutionTimeoutMillis(15000) // 15 seconds
                            .withConnectTimeoutMillis(15000) // 15 seconds
                            .withIdleTimeoutMillis(60000) // 60 seconds
                            .build()
                    )
            )
            .start();
        System.out.println("Reverse proxy started at " + reverseProxy.uri());
    }
}
