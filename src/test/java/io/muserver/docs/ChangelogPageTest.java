package io.muserver.docs;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.docs.handlers.VanillaHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.muserver.MuServerBuilder.httpServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class ChangelogPageTest {
    private MuServer server;

    @Test
    public void rendersPermalinksAndCopyButtonsForEveryRelease() throws IOException {
        ViewRenderer renderer = ViewRenderer.create(true);
        server = httpServer()
            .addHandler(Method.GET, "/changelog", new VanillaHandler(renderer, "changelog", "Changelog"))
            .start();

        Request request = new Request.Builder()
            .url(server.uri().resolve("/changelog").toString())
            .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            assertThat(response.code(), is(200));
            String html = response.body().string();
            assertThat(html, containsString("<a class=\"release-anchor\" href=\"#v2.4.2\">2.4.2</a>"));
            assertThat(html, containsString("<a class=\"release-anchor\" href=\"#v2.4.1\">2.4.1</a>"));
            int releaseCount = countMatches(html, "<div class=\"release[^\"]*\"");
            int anchoredReleaseCount = countMatches(html, "<div class=\"release[^\"]*\" id=\"v[0-9.]+\">");
            int permalinkCount = countMatches(html, "<a class=\"release-anchor\" href=\"#v[0-9.]+\">[0-9.]+</a>");
            assertThat(releaseCount, greaterThan(80));
            assertThat(anchoredReleaseCount, is(releaseCount));
            assertThat(permalinkCount, is(releaseCount));
            assertThat(html, containsString("<div class=\"release minor\" id=\"v0.55.5\">"));
            assertThat(html, containsString("<h2 class=\"release-heading\" id=\"2.0.0\">"));
            assertThat(html, containsString("button.setAttribute('data-release-id', release.id)"));
            assertThat(html, containsString("new URL(window.location.pathname, window.location.origin)"));
            assertThat(html, containsString("navigator.clipboard.writeText(text)"));
            assertThat(html, containsString("button.setAttribute('data-tooltip', 'Copy link')"));
            assertThat(html, containsString("role=\"status\" aria-live=\"polite\""));
            assertThat(html, containsString("Link to release ' + releaseVersion + ' copied"));
            assertThat(html, containsString("button.classList.add('suppress-tooltip')"));
            assertThat(html, containsString("button.addEventListener('mouseenter'"));
            assertThat(html, containsString("function resetCopyButton(button)"));
        }
    }

    private static int countMatches(String value, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(value);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop();
        }
    }
}
