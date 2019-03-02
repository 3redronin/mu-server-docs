package io.muserver.docs.samples;

import io.muserver.MuHandler;
import io.muserver.MuServer;
import io.muserver.Mutils;
import io.muserver.SSLContextBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeRetryAfterException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.security.KeyPair;
import java.security.Security;
import java.util.List;
import java.util.concurrent.Callable;

public class AcmeCertManager {
    private static final Logger log = LoggerFactory.getLogger(AcmeCertManager.class);

    private final List<String> domains;
    private final File configDir;
    private final File certFile;
    private final URI acmeServerURi;
    private final File accountKeyFile;
    private final File domainKeyFile;
    private Login login;
    private volatile String currentToken;
    private volatile String currentContent;
    private final String organization;
    private final String organizationalUnit;
    private final String country;
    private final String state;
    private volatile MuServer muServer;

    AcmeCertManager(File configDir, URI acmeServerURi, String organization, String organizationalUnit, String country, String state, List<String> domains) {
        this.acmeServerURi = acmeServerURi;
        this.organization = organization;
        this.organizationalUnit = organizationalUnit;
        this.country = country;
        this.state = state;
        this.domains = domains;
        this.configDir = configDir;
        certFile = new File(configDir, "cert-chain.crt");
        accountKeyFile = new File(configDir, "acme-account-key.pem");
        domainKeyFile = new File(configDir, "domain-key.pem");
        Security.addProvider(new BouncyCastleProvider());
    }

    public void start(MuServer muServer) throws Exception {
        this.muServer = muServer;
        acquireCert();
    }

    private void acquireCert() throws Exception {
        log.info("Logging in...");
        KeyPair keyPair = loadOrCreateKeypair(accountKeyFile);
        Session session = new Session(acmeServerURi);
        Account account = new AccountBuilder()
            .agreeToTermsOfService()
            .useKeyPair(keyPair)
            .create(session);
        login = session.login(account.getLocation(), keyPair);
        log.info("Logged in with " + login.getAccount().getLocation());
        Order order = orderCert();
        waitUntilValid("Waiting for certificate order", order::getStatus, () -> { order.update(); return null; });
        Certificate cert = order.getCertificate();
        try (FileWriter fw = new FileWriter(certFile)) {
            cert.writeCertificate(fw);
        }
        log.info("Cert written to " + Mutils.fullPath(certFile) + " and it expires on " + cert.getCertificate().getNotAfter());

    }

    public SSLContext createSSLContext() {
        if (!certFile.isFile()) {
            log.info("No cert available yet. Using self-signed cert.");
            return SSLContextBuilder.unsignedLocalhostCert();
        }

        return SSLContextBuilder.unsignedLocalhostCert();
    }

    public MuHandler createHandler() {
        return (request, response) -> {
            String token = currentToken;
            String content = currentContent;
            if (token == null || content == null || !request.uri().getPath().equals("/.well-known/acme-challenge/" + token)) {
                return false;
            }
            response.contentType("text/plain");
            response.write(content);
            return true;
        };
    }

    private Order orderCert() throws Exception {
        Order order = login.getAccount().newOrder()
            .domains(domains)
            .create();
        for (Authorization authorization : order.getAuthorizations()) {
            if (authorization.getStatus() != Status.VALID) {
                processAuth(authorization);
            }
        }
        KeyPair domainKeyPair = loadOrCreateKeypair(domainKeyFile);

        CSRBuilder csrb = new CSRBuilder();
        csrb.addDomains(domains);
        if (country != null) csrb.setCountry(country);
        if (organizationalUnit != null) csrb.setOrganizationalUnit(organizationalUnit);
        if (state != null) csrb.setState(state);
        if (organization != null) csrb.setOrganization(organization);
        csrb.sign(domainKeyPair);
        byte[] csr = csrb.getEncoded();
        order.execute(csr);
        return order;
    }

    private void processAuth(Authorization auth) throws Exception {
        Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
        if (challenge == null) {
            throw new RuntimeException("Could not create cert as no HTTP01 auth was available");
        }
        currentToken = challenge.getToken();
        currentContent = challenge.getAuthorization();
        challenge.trigger();

        waitUntilValid("Waiting for authorization challenge to complete", auth::getStatus, () -> { auth.update(); return null; });
        currentContent = null;
        currentToken = null;
    }

    private static void waitUntilValid(String description, Callable<Status> getStatus, Callable<Void> update) throws Exception {
        Status curStatus;
        int maxAttempts = 100;
        while ( (curStatus = getStatus.call()) != Status.VALID) {
            if (curStatus == Status.INVALID) {
                throw new RuntimeException(description + " but status is INVALID. Aborting attempt.");
            }
            log.info(description + ". Current status is " + curStatus);
            Thread.sleep(3000L);
            try {
                update.call();
            } catch (AcmeRetryAfterException e) {
                long waitTime = e.getRetryAfter().getEpochSecond() - System.currentTimeMillis();
                waitTime = Math.max(500, waitTime);
                waitTime = Math.min(10 * 60000, waitTime);
                Thread.sleep(waitTime);
            }
            maxAttempts--;
            if (maxAttempts == 0) {
                throw new RuntimeException(description + " but timed out.");
            }
        }
    }

    static KeyPair loadOrCreateKeypair(File file) throws IOException {
        File dir = file.getParentFile();
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new RuntimeException("Could not create " + Mutils.fullPath(dir));
        }
        KeyPair keyPair;
        if (file.isFile()) {
            log.info("Using key " + Mutils.fullPath(file));
            try (FileReader fr = new FileReader(file)) {
                keyPair = KeyPairUtils.readKeyPair(fr);
            }
        } else {
            log.info("Creating " + Mutils.fullPath(file));
            keyPair = KeyPairUtils.createKeyPair(2048);
            try (FileWriter fw = new FileWriter(file)) {
                KeyPairUtils.writeKeyPair(keyPair, fw);
            }
        }
        return keyPair;
    }

}
