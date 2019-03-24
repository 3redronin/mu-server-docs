package io.muserver.docs.samples;

import io.muserver.*;

import java.util.Map;
import java.util.Optional;

// Run RequestExample.java to test this

public class CookieExampleHandler implements RouteHandler {
    public void handle(MuRequest request, MuResponse response, Map<String,String> pathParams) {

        switch (request.query().get("action", "view")) {
            case "view":

                Optional<String> cookie = request.cookie("mu-example-cookie");
                String cookieValue = cookie.isPresent() ? cookie.get() : "(no cookie)";
                response.write("Cookie value from request is " + cookieValue);

                break;
            case "create":

                Cookie newCookie = CookieBuilder.newCookie()
                    .withName("mu-example-cookie")
                    .withValue("CookieValue" + System.currentTimeMillis())
                    .withMaxAgeInSeconds(45)
                    .withPath("/model") // only send over /model/*
                    .secure(false) // only send over HTTPS
                    .httpOnly(true) // disable JavaScript access
                    .build();

                response.addCookie(newCookie);
                response.write("Added cookie: " + newCookie);

                break;
            case "delete":

                // The name, path, and domain values must match the created-cookie values
                Cookie toDelete = CookieBuilder.newCookie()
                    .withName("mu-example-cookie")
                    .withValue("")
                    .withMaxAgeInSeconds(0)
                    .withPath("/model")
                    .build();

                response.addCookie(toDelete);
                response.write("Deleted cookie " + toDelete);

                break;
        }

    }
}
