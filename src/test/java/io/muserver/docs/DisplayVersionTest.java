package io.muserver.docs;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.docs.handlers.VanillaHandler;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Test;

import static io.muserver.MuServerBuilder.httpServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

public class DisplayVersionTest {
    @Test
    public void configuredStableVersionIsAdvertised() throws Exception {
        assertDownloadVersion(null, "2.4.2");
    }

    @Test
    public void systemPropertyOverridesConfiguredVersion() throws Exception {
        assertDownloadVersion(" 2.3.4 ", "2.3.4");
    }

    @Test
    public void blankOverrideUsesDependencyVersion() throws Exception {
        assertDownloadVersion(" ", MuServer.artifactVersion());
    }

    private void assertDownloadVersion(String override, String expected) throws Exception {
        String key = "mu-server.display-version";
        String previous = System.getProperty(key);
        MuServer server = null;
        try {
            if (override == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, override);
            }
            server = httpServer()
                .addHandler(Method.GET, "/download",
                    new VanillaHandler(ViewRenderer.create(false), "download", "Download Mu Server"))
                .start();
            Request request = new Request.Builder().url(server.uri().resolve("/download").toString()).build();
            try (Response response = new OkHttpClient().newCall(request).execute()) {
                assertThat(response.code(), is(200));
                String html = response.body().string();
                assertThat(html, containsString("Version " + expected));
                assertThat(html, containsString("&lt;version&gt;" + expected + "&lt;/version&gt;"));
                assertThat(html, containsString("version: '" + expected + "'"));
                assertThat(html, containsString("https://repo.maven.apache.org/maven2/io/muserver/mu-server/"
                    + expected + "/mu-server-" + expected + ".jar"));
            }
        } finally {
            if (server != null) {
                server.stop();
            }
            if (previous == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, previous);
            }
        }
    }
}
