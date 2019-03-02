package io.muserver.docs.samples;

import org.junit.Test;

import java.io.File;
import java.security.KeyPair;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AcmeCertManagerTest {

    private final File configDir = new File("target/testdata/" + UUID.randomUUID());

    @Test
    public void canCreateKeyPairs() throws Exception {
        KeyPair created = AcmeCertManager.loadOrCreateKeypair(new File(configDir, "acme-account-key.pem"));
        KeyPair loaded = AcmeCertManager.loadOrCreateKeypair(new File(configDir, "acme-account-key.pem"));
        assertThat(loaded.getPrivate(), equalTo(created.getPrivate()));
        assertThat(loaded.getPublic(), equalTo(created.getPublic()));
    }


}