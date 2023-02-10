package io.muserver.docs.samples;

import io.muserver.HttpsConfigBuilder;
import io.muserver.Method;
import io.muserver.MuServer;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.stream.Stream;

import static io.muserver.MuServerBuilder.muServer;

public class ClientCert {
    public static void main(String[] args) throws Exception {

        KeyStore certificateAuthorityStore = KeyStore.getInstance("PKCS12");
        try (InputStream caStream = ClientCert.class.getResourceAsStream("/samples/client-cert/ca.p12")) {
            certificateAuthorityStore.load(caStream, "password".toCharArray());
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        trustManagerFactory.init(certificateAuthorityStore);
        TrustManager trustManager = Stream.of(trustManagerFactory.getTrustManagers())
            .filter(tm -> tm instanceof X509TrustManager)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Could not find the certificate authority trust store"));

        HttpsConfigBuilder httpsConfig = HttpsConfigBuilder.unsignedLocalhost()
            .withClientCertificateTrustManager(trustManager);

        MuServer server = muServer()
            .withHttpsPort(10443)
            .withHttpsConfig(httpsConfig)
            .addHandler(Method.GET, "/", (req, resp, pp) -> {
                Optional<Certificate> certificate = req.connection().clientCertificate();
                if (certificate.isPresent() && certificate.get() instanceof X509Certificate) {
                    X509Certificate clientCert = (X509Certificate) certificate.get();
                    X500Principal subject = clientCert.getSubjectX500Principal();

                    resp.sendChunk("Client cert received\n" +
                        "\nName: " + subject.getName() +
                        "\nValid dates: " + clientCert.getNotBefore() + " to " + clientCert.getNotAfter() +
                        "\n\n"
                    );

                    try {
                        clientCert.checkValidity();
                        resp.sendChunk("The certificate is valid");
                    } catch (CertificateNotYetValidException | CertificateExpiredException e) {
                        resp.sendChunk("The certificate not current");
                    }

                } else {
                    resp.write("No valid client certificate was sent");
                }
            })
            .start();

        System.out.println("Server started at " + server.uri());

    }
}
