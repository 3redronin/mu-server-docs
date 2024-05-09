package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.openapi.OpenAPIObjectBuilder;
import io.muserver.rest.ApiResponse;
import io.muserver.rest.Description;
import io.muserver.rest.Required;
import io.muserver.rest.RestHandlerBuilder;
import org.json.JSONObject;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

import static io.muserver.openapi.ExternalDocumentationObjectBuilder.externalDocumentationObject;
import static io.muserver.openapi.InfoObjectBuilder.infoObject;
import static io.muserver.rest.CORSConfigBuilder.corsConfig;

public class JaxRSDocumentationExample {

    public static void main(String[] args) {

        MuServer server = MuServerBuilder.httpServer()
            .addHandler(createRestHandler())
            .start();

        System.out.println("API HTML: " + server.uri().resolve("/api.html"));
        System.out.println("API JSON: " + server.uri().resolve("/openapi.json"));

    }

    public static RestHandlerBuilder createRestHandler() {
        return RestHandlerBuilder.restHandler(new UserResource())
            .withCORS(corsConfig().withAllowedOriginRegex(".*"))
            .withOpenApiHtmlUrl("/api.html")
            .withOpenApiJsonUrl("/openapi.json")
            .withOpenApiDocument(
                OpenAPIObjectBuilder.openAPIObject()
                    .withInfo(
                        infoObject()
                            .withTitle("User API Documentation")
                            .withDescription("This is just a demo API that doesn't actually work!")
                            .withVersion("1.0")
                            .build())
                    .withExternalDocs(
                        externalDocumentationObject()
                            .withDescription("Documentation docs")
                            .withUrl(URI.create("https://muserver.io/jaxrs"))
                            .build()
                    )
            );
    }

    @Path("/users")
    @Description(value = "A human user", details = "Use this API to get and create users")
    public static class UserResource {

        @GET
        @Path("/{id}")
        @Produces("application/json")
        @Description("Gets a single user")
        @ApiResponse(code = "200", message = "Success")
        @ApiResponse(code = "404", message = "No user with that ID found")
        public String get(
            @Description("The ID of the user")
            @PathParam("id") int id) {
            return new JSONObject()
                .put("id", id)
                .toString(4);
        }

        @POST
        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Description("Creates a new user")
        @ApiResponse(code = "201", message = "The user was created")
        @ApiResponse(code = "400", message = "The ID or name was not specified")
        public Response create(
            @Description("A unique ID for the new user")
            @Required @FormParam("id") int id,
            @Description("The name of the user")
            @FormParam("name") String name) {
            return Response.status(201).build();
        }

    }
}