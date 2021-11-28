package io.muserver.docs.samples;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.RestHandlerBuilder;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class JacksonJaxRSExampleTest {

    private final MuServer server = MuServerBuilder.httpServer()
        .addHandler(
            RestHandlerBuilder.restHandler(new JacksonJaxRSExample.UserResource())
                .addCustomWriter(new JacksonJaxbJsonProvider())
                .addCustomReader(new JacksonJaxbJsonProvider())
        )
        .start();
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    public void writerWorks() throws Exception {
        HttpResponse<String> resp = client.send(HttpRequest.newBuilder()
            .uri(server.uri().resolve("/users"))
            .build(), HttpResponse.BodyHandlers.ofString());
        JSONObject body = new JSONObject(resp.body());
        assertThat(body.getString("name"), equalTo("John Smith"));
        assertThat(body.getInt("age"), equalTo(40));
        assertThat(body.getBoolean("isActive"), equalTo(true));
    }

    @Test
    public void readerWorks() throws Exception {
        HttpResponse<String> resp = client.send(HttpRequest.newBuilder()
            .uri(server.uri().resolve("/users"))
            .method("POST", HttpRequest.BodyPublishers.ofString("{\"isActive\":false,\"name\":\"John Smith\",\"age\":41}"))
            .header("content-type", "application/json")
            .build(), HttpResponse.BodyHandlers.ofString());
        assertThat(resp.body(), equalTo("I got a user with isActive=false and name John Smith and age 41"));
    }

    @After
    public void stopIt() {
        server.stop();
    }

}