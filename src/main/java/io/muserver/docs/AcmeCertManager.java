package io.muserver.docs;

import io.muserver.MuHandler;
import io.muserver.Mutils;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeRetryAfterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class AcmeCertManager {
    private static final Logger log = LoggerFactory.getLogger(AcmeCertManager.class);

    private final String[] domains;
    private final File configDir;
    private final URI acmeServerURi;
    private Login login;
    private volatile String currentToken;
    private volatile String currentContent;

    public AcmeCertManager(File configDir, URI acmeServerURi, String... domains) {
        this.acmeServerURi = acmeServerURi;
        this.domains = domains;
        this.configDir = configDir;
    }

    public void start() throws Exception {
        log.info("Logging in...");
        login = login();
        log.info("Logged in with " + login.getAccount().getLocation());
        orderCert();
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

    private void orderCert() throws AcmeException, InterruptedException {
        Order order = login.getAccount().newOrder()
            .domains(domains)
            .create();
        for (Authorization authorization : order.getAuthorizations()) {
            if (authorization.getStatus() != Status.VALID) {
                processAuth(authorization);
            }
        }
        // TODO: get the CSR, but you can't, so use bouncy castle, from https://shredzone.org/maven/acme4j/usage/order.html
//        order.execute(csr);
    }

    private void processAuth(Authorization auth) throws AcmeException, InterruptedException {
        Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
        if (challenge == null) {
            throw new RuntimeException("Could not create cert as no HTTP01 auth was available");
        }
        currentToken = challenge.getToken();
        currentContent = challenge.getAuthorization();
        challenge.trigger();
        while (auth.getStatus() != Status.VALID) {
            if (auth.getStatus() == Status.INVALID) {
                throw new RuntimeException("Auth status is invalid. Aborting attempt.");
            }
            log.info("Waiting for cert request to be authorised...");
            Thread.sleep(3000L);
            try {
                auth.update();
            } catch (AcmeRetryAfterException e) {
                long waitTime = e.getRetryAfter().getEpochSecond() - System.currentTimeMillis();
                waitTime = Math.max(500, waitTime);
                waitTime = Math.min(10 * 60000, waitTime);
                Thread.sleep(waitTime);
            }
        }
        currentContent = null;
        currentToken = null;
    }

    public Login login() throws Exception {
        KeyPair keyPair = loadOrCreateKeypair(configDir);
        Session session = new Session(acmeServerURi);
        Account account = loadOrCreateAccount(session, keyPair);
        return session.login(account.getLocation(), keyPair);
    }

    private static Account loadOrCreateAccount(Session session, KeyPair keyPair) throws AcmeException {
        return new AccountBuilder()
            .agreeToTermsOfService()
            .useKeyPair(keyPair)
            .create(session);
    }

    static KeyPair loadOrCreateKeypair(File dir) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new RuntimeException("Could not create " + Mutils.fullPath(dir));
        }

        File publicKeyFile = new File(dir, "userkey.public");
        File privateKeyFile = new File(dir, "userkey.private");
        KeyPair keyPair;
        if (publicKeyFile.isFile() && privateKeyFile.isFile()) {
            log.info("Using keys " + Mutils.fullPath(publicKeyFile) + " and " + Mutils.fullPath(privateKeyFile));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(readFile(publicKeyFile));
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(readFile(privateKeyFile));
            keyPair = new KeyPair(keyFactory.generatePublic(publicKeySpec), keyFactory.generatePrivate(privateKeySpec));
        } else {
            log.info("Creating " + Mutils.fullPath(publicKeyFile) + " and " + Mutils.fullPath(privateKeyFile));
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.genKeyPair();
            byte[] publicBytes = keyPair.getPublic().getEncoded();
            byte[] privateBytes = keyPair.getPrivate().getEncoded();
            try (FileOutputStream pubfos = new FileOutputStream(publicKeyFile);
                 FileOutputStream prifos = new FileOutputStream(privateKeyFile)) {
                pubfos.write(publicBytes);
                prifos.write(privateBytes);
            }
        }
        return keyPair;
    }

    private static byte[] readFile(File publicKeyFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(publicKeyFile)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Mutils.copy(fis, baos, 8192);
            return baos.toByteArray();
        }
    }

}
