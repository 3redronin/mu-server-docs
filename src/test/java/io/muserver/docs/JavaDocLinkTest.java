package io.muserver.docs;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavaDocLinkTest {

    private final JavaDocLink link = new JavaDocLink();

    @Test
    public void createsJavaDocLinks() {
        assertEquals("<a href=\"https://www.javadoc.io/page/io.muserver/mu-server/latest/io/muserver/Mutils.html\">Mutils</a>",
            link.execute(new MockJtwigRequest("io.muserver.Mutils")));
    }

}