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

import static io.muserver.MuServerBuilder.httpServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;

public class AgentSkillsPageTest {
    private MuServer server;

    @Test
    public void rendersInstallInstructionsAndTopLinks() throws IOException {
        ViewRenderer renderer = ViewRenderer.create(true);
        server = httpServer()
            .addHandler(Method.GET, "/agent-skills", new VanillaHandler(renderer, "agent-skills", "Agent Skills"))
            .start();

        Request request = new Request.Builder()
            .url(server.uri().resolve("/agent-skills").toString())
            .build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            assertThat(response.code(), is(200));
            String html = response.body().string();
            assertThat(html, containsString("<h1>Agent Skills</h1>"));
            assertThat(html, containsString("npx skills add 3redronin/mu-server-skills"));
            assertThat(html, containsString("--agent codex"));
            assertThat(html, containsString("--agent github-copilot"));
            assertThat(html, containsString("class=\"bash copyable-command\""));
            assertThat(html, containsString("navigator.clipboard.writeText(text)"));

            int version = html.indexOf("class=\"current-version\"");
            int changeLog = html.indexOf("href=\"/changelog\">Change log</a>");
            int agentSkills = html.indexOf("href=\"/agent-skills\">Agent Skills</a>");
            assertThat(version, greaterThanOrEqualTo(0));
            assertThat(version, lessThan(changeLog));
            assertThat(changeLog, lessThan(agentSkills));
        }
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop();
        }
    }
}
