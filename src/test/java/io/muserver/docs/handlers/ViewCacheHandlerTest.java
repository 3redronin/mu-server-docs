package io.muserver.docs.handlers;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.Mutils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.util.Date;

import static io.muserver.MuServerBuilder.httpServer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ViewCacheHandlerTest {

    private MuServer server;
    private OkHttpClient client = new OkHttpClient();

    @Test
    public void returns304IfNotModifiedSinceLastUpdate() throws IOException {

        ViewCacheHandler cacheHandler = new ViewCacheHandler("/jaxrs");

        server = httpServer()
            .addHandler(cacheHandler)
            .addHandler(Method.GET, "/jaxrs", (request, response, pathParams) -> {
                response.write("This is the original value");
            })
            .start();


        Date lastModified;
        try (Response resp = call(request(server.uri().resolve("/jaxrs")))) {
            assertContentSent(resp);
            lastModified = Mutils.fromHttpDate(resp.header("Last-Modified"));
        }

        try (Response resp = call(
            request(server.uri().resolve("/jaxrs"))
                .header("If-Modified-Since", Mutils.toHttpDate(new Date(lastModified.getTime() - 100000)))
        )) {
            assertContentSent(resp);
        }

        try (Response resp = call(
            request(server.uri().resolve("/jaxrs"))
            .header("If-Modified-Since", Mutils.toHttpDate(lastModified))
        )) {
            assertContentNotSent(resp);
        }

        try (Response resp = call(
            request(server.uri().resolve("/jaxrs"))
                .header("If-Modified-Since", Mutils.toHttpDate(new Date(lastModified.getTime() + 100000)))
        )) {
            assertContentNotSent(resp);
        }

    }

    private static void assertContentNotSent(Response resp) throws IOException {
        assertThat(resp.code(), is(304));
        assertThat(resp.body().string(), is(""));
        assertThat(resp.header("Content-Length"), is(nullValue()));
        assertThat(resp.header("Content-Type"), is(nullValue()));
        assertThat(resp.header("Last-Modified"), is(nullValue()));
        assertThat(resp.header("Cache-Control"), is("must-revalidate, max-age=600"));
        assertThat(resp.header("Date"), is(notNullValue()));
    }

    private static void assertContentSent(Response resp) throws IOException {
        assertThat(resp.code(), is(200));
        assertThat(resp.body().string(), is("This is the original value"));
        assertThat(resp.header("Date"), is(notNullValue()));
        assertThat(resp.header("Content-Length"), is("26"));
        assertThat(resp.header("Content-Type"), is("text/plain;charset=utf-8"));
        assertThat(resp.header("Last-Modified"), is(notNullValue()));
        assertThat(resp.header("Cache-Control"), is("must-revalidate, max-age=600"));
    }

    private Response call(Request.Builder request) throws IOException {
        return client.newCall(
            request.build()
        ).execute();
    }

    private Request.Builder request(URI uri) {
        return new Request.Builder().url(uri.toString());
    }

    @After
    public void stop() {
        server.stop();
    }

}