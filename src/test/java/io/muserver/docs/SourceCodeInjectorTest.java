package io.muserver.docs;

import io.muserver.Mutils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class SourceCodeInjectorTest {

    private SourceCodeInjector injector = new SourceCodeInjector(true);

    @Test
    public void javaIgnoresImports() {
        String result = (String) injector.execute(new MockJtwigRequest("UploadExample.java"));
        assertThat(result, startsWith("<pre><code class=\"language-java\">public class UploadExample {"));
        assertThat(result, endsWith("}</pre></code><a class=\"github-link\" " +
            "href=\"https://github.com/3redronin/mu-server-docs/blob/master/src/main/java/io/muserver/docs/samples/UploadExample.java\">" +
            "(see full file)</a>"));
    }

    @Test
    public void htmlJustHasBody() {
        String result = (String) injector.execute(new MockJtwigRequest("upload.html"));
        assertThat(result, startsWith("<pre><code class=\"language-markup\">" + Mutils.htmlEncode("<h1>Upload demo</h1>")));
        assertThat(result, endsWith(Mutils.htmlEncode("</form>") + "</pre></code><a class=\"github-link\" " +
            "href=\"https://github.com/3redronin/mu-server-docs/blob/master/src/main/resources/samples/upload.html\">" +
            "(see full file)</a>"));
    }

}