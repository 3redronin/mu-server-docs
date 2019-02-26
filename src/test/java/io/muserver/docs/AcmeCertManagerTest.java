package io.muserver.docs;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.security.KeyPair;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Ignore("under development")
public class AcmeCertManagerTest {

    private final File configDir = new File("target/testdata/" + UUID.randomUUID());

    @Test
    public void canCreateKeyPairs() throws Exception {
        KeyPair created = AcmeCertManager.loadOrCreateKeypair(configDir);
        KeyPair loaded = AcmeCertManager.loadOrCreateKeypair(configDir);
        assertThat(loaded.getPrivate(), equalTo(created.getPrivate()));
        assertThat(loaded.getPublic(), equalTo(created.getPublic()));
    }

    @Test
    public void canLoginWithANewAccount() throws Exception {
        AcmeCertManager acmeCertManager = new AcmeCertManager(configDir, URI.create("acme://letsencrypt.org/staging"),
            "muserver.example.org");
        acmeCertManager.start();


    }

}