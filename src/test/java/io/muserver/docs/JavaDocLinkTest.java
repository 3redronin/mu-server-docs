package io.muserver.docs;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JavaDocLinkTest {

    private final JavaDocLinkFunction link = new JavaDocLinkFunction(true);

    @Test
    public void createsJavaDocLinks() {
        assertEquals("<a href=\"/javadocs/io/muserver/Mutils.html\">Mutils</a>",
            link.execute(Collections.singletonMap("className", "io.muserver.Mutils"), null, null, 0));
    }

    @Test
    public void murpIsSupported() {
        assertEquals("<a href=\"/javadocs/io/muserver/murp/ReverseProxyBuilder.html\">ReverseProxyBuilder</a>",
            link.execute(Collections.singletonMap("className", "io.muserver.murp.ReverseProxyBuilder"), null, null, 0));
    }

    @Test
    public void nonMuPackagesUseExternalJavadocs() {
        assertEquals("<a href=\"https://www.javadoc.io/doc/jakarta.ws.rs/jakarta.ws.rs-api/latest/jakarta/ws/rs/container/ResourceInfo.html\">ResourceInfo</a>",
            link.execute(Collections.singletonMap("className", "jakarta.ws.rs.container.ResourceInfo"), null, null, 0));
    }

}