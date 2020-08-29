package io.muserver.docs;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JavaDocLinkTest {

    private final JavaDocLinkFunction link = new JavaDocLinkFunction(true);

    @Test
    public void createsJavaDocLinks() {
        assertEquals("<a href=\"https://www.javadoc.io/page/io.muserver/mu-server/latest/io/muserver/Mutils.html\">Mutils</a>",
            link.execute(Collections.singletonMap("className", "io.muserver.Mutils"), null, null, 0));
    }

    @Test
    public void murpIsSupported() {
        assertEquals("<a href=\"https://www.javadoc.io/page/io.muserver/murp/latest/io/muserver/murp/ReverseProxyBuilder.html\">ReverseProxyBuilder</a>",
            link.execute(Collections.singletonMap("className", "io.muserver.murp.ReverseProxyBuilder"), null, null, 0));
    }

}