package io.muserver.docs.samples;

import io.muserver.HttpsConfigBuilder;
import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.SSLCipherFilter;

import java.io.File;
import java.util.List;
import java.util.Set;

import static io.muserver.MuServerBuilder.muServer;

public class HttpsWithCert {
    public static void main(String[] args) {

        HttpsConfigBuilder httpsConfig = HttpsConfigBuilder.httpsConfig()
            .withKeystoreType("JKS")
            .withKeystorePassword("Very5ecure")
            .withKeyPassword("ActuallyNotSecure")
            .withKeystore(new File("src/main/java/io/muserver/docs/samples/HttpsCert.jks"))
            .withProtocols("TLSv1.2", "TLSv1.3")
            .withCipherFilter(new SSLCipherFilter() {
                public List<String> selectCiphers(Set<String> supportedCiphers, List<String> defaultCiphers) {
                    return defaultCiphers;
                }
            });

        MuServer server = muServer()
            .withHttpsPort(10443)
            .withHttpsConfig(httpsConfig)
            .addHandler(Method.GET, "/", (req, resp, pp) -> resp.write("This is HTTPS"))
            .start();

        System.out.println("Server started at " + server.uri());

    }
}
