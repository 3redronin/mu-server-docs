package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.RestHandlerBuilder;

import jakarta.ws.rs.*;
import tools.jackson.jakarta.rs.json.JacksonJsonProvider;

public class JacksonJaxRSExample {

    public static void main(String[] args) {
        UserResource userResource = new UserResource();
        JacksonJsonProvider jacksonJsonProvider = new JacksonJsonProvider();
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(
                RestHandlerBuilder.restHandler(userResource)
                    .addCustomWriter(jacksonJsonProvider)
                    .addCustomReader(jacksonJsonProvider)
            )
            .start();

        System.out.println("API example: " + server.uri().resolve("/users"));
    }

    public static class User {
        public boolean isActive;
        public String name;
        public int age;
    }

    @Path("/users")
    static class UserResource {

        @GET
        @Produces("application/json")
        public User getUser() {
            User user = new User();
            user.isActive = true;
            user.name = "John Smith";
            user.age = 40;
            return user;
        }

        @POST
        @Consumes("application/json")
        @Produces("text/plain")
        public String postUser(User user) {
            return "I got a user with isActive=" + user.isActive
                + " and name " + user.name + " and age " + user.age;
        }

    }

}
