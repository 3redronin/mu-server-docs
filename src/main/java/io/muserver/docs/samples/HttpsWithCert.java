package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.File;

import static io.muserver.MuServerBuilder.muServer;

public class HttpsWithCert {
    public static void main(String[] args) {

        SSLContext sslContext = SSLContextBuilder.sslContext()
            .withKeystoreType("JKS")
            .withKeystorePassword("Very5ecure")
            .withKeyPassword("ActuallyNotSecure")
            .withKeystore(new File("src/main/java/io/muserver/docs/samples/HttpsCert.jks"))
            .build();

        MuServer server = muServer()
            .withHttpsPort(10443)
            .withHttpsConfig(sslContext)
            .addHandler(Method.GET, "/", (req, resp, pp) -> resp.write("This is HTTPS"))
            .start();

        server.changeSSLContext(sslContext);

        System.out.println("Server started at " + server.uri());

    }
}
