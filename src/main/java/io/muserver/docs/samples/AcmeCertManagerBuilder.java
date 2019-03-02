package io.muserver.docs.samples;

import io.muserver.Mutils;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class AcmeCertManagerBuilder {

    private List<String> domains = new ArrayList<>();
    private File configDir;
    private URI acmeServerURI;
    private String organization;
    private String organizationalUnit;
    private String country;
    private String state;

    public AcmeCertManager build() {
        Mutils.notNull("configDir", configDir);
        Mutils.notNull("acmeServerURI", acmeServerURI);
        if (domains.isEmpty()) {
            throw new IllegalStateException("Please specify a domain");
        }
        return new AcmeCertManager(configDir, acmeServerURI, organization, organizationalUnit, country, state, domains);
    }

    public AcmeCertManagerBuilder withDomain(String domain) {
        this.domains.add(domain);
        return this;
    }

    public AcmeCertManagerBuilder withConfigDir(File configDir) {
        this.configDir = configDir;
        return this;
    }

    public AcmeCertManagerBuilder withAcmeServerURI(URI acmeServerURI) {
        this.acmeServerURI = acmeServerURI;
        return this;
    }

    public AcmeCertManagerBuilder withOrganization(String organization) {
        this.organization = organization;
        return this;
    }

    public AcmeCertManagerBuilder withOrganizationalUnit(String organizationalUnit) {
        this.organizationalUnit = organizationalUnit;
        return this;
    }

    public AcmeCertManagerBuilder withCountry(String country) {
        this.country = country;
        return this;
    }

    public AcmeCertManagerBuilder withState(String state) {
        this.state = state;
        return this;
    }

    public static AcmeCertManagerBuilder acmeCertManager() {
        return new AcmeCertManagerBuilder();
    }

    public static AcmeCertManagerBuilder letsEncrypt() {
        return acmeCertManager().withAcmeServerURI(URI.create("acme://letsencrypt.org/"));
    }
    public static AcmeCertManagerBuilder letsEncryptStaging() {
        return acmeCertManager().withAcmeServerURI(URI.create("acme://letsencrypt.org/staging"));
    }
}
