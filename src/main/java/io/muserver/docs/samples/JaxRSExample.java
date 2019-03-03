package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.RestHandlerBuilder;
import org.json.JSONObject;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.Map;

public class JaxRSExample {

    public static void main(String[] args) {

        Map<Integer, String> users = new HashMap<>();
        users.put(1, "Mike");
        users.put(2, "Sam");
        users.put(3, "Dan");
        UserResource userResource = new UserResource(users);

        MuServer server = MuServerBuilder.httpServer()
            .addHandler(RestHandlerBuilder.restHandler(userResource))
            .start();

        System.out.println("API example: " + server.uri().resolve("/jaxrsexample/users/1"));

    }

    @Path("/jaxrsexample/users")
    public static class UserResource {

        private final Map<Integer, String> users;

        public UserResource(Map<Integer, String> users) {
            this.users = users;
        }

        @GET
        @Path("/{id}")
        @Produces("application/json")
        public String get(@PathParam("id") int id) {
            String name = users.get(id);
            if (name == null) {
                throw new NotFoundException("No user with id " + id);
            }
            return new JSONObject()
                .put("id", id)
                .put("name", name)
                .toString(4);
        }

    }
}