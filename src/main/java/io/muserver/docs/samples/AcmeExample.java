package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.acme.AcmeCertManager;
import io.muserver.acme.AcmeCertManagerBuilder;

public class AcmeExample {

    public static void main(String[] args) throws Exception {

        AcmeCertManager certManager = AcmeCertManagerBuilder.letsEncryptStaging()
            .withDomain("your-domain.example.org")
            .withConfigDir("target/ssl-config")
            .build();

        MuServer server = MuServerBuilder.muServer()
            .withHttpPort(80)
            .withHttpsPort(443)
            .withHttpsConfig(certManager.createSSLContext())
            .addHandler(certManager.createHandler())
            .addHandler(Method.GET, "/", (req, resp, path) -> {
                resp.write("Hello, world");
            })
            .start();

        certManager.start(server);
        System.out.println("Started server at " + server.uri());

    }

}
