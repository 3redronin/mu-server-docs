package io.muserver.docs;

import org.jtwig.functions.FunctionRequest;
import org.jtwig.render.RenderRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JavaDocLinkTest {

    private final JavaDocLink link = new JavaDocLink();

    @Test
    public void createsJavaDocLinks() {
        assertEquals("<a href=\"https://www.javadoc.io/page/io.muserver/mu-server/latest/io/muserver/Mutils.html\">Mutils</a>",
            link.execute(new MockRequest("io.muserver.Mutils")));
    }

    private class MockRequest extends FunctionRequest {
        private final String[] args;

        public MockRequest(String... args) {
            super(new RenderRequest(null, null), null, null, null);
            this.args = args;
        }

        @Override
        public Object get(int index) {
            return args[index];
        }
    }
}