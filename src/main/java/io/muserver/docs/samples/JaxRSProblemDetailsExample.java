package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.ProblemDetailsException;
import io.muserver.rest.ProblemDetailsExceptionMapperBuilder;
import io.muserver.rest.RestHandlerBuilder;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.net.URI;


public class JaxRSProblemDetailsExample {

    public static void main(String[] args) {
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(
                RestHandlerBuilder.restHandler(new ValidationResource())
                    .addExceptionMapper(Throwable.class,
                        ProblemDetailsExceptionMapperBuilder.problemDetailsExceptionMapper()
                            .withLog4xxProblemDetailsInstanceIds(true)
                            .withLog5xxProblemDetailsInstanceIds(true)
                            .build()
                    )
            )
            .start();

        System.out.println("Problem details example: " + server.uri().resolve("/users/validate"));
    }

    @Path("/users")
    public static class ValidationResource {
        @GET
        @Path("/validate")
        public String validate() {
            throw ProblemDetailsException.builder(422)
                .withTitle("Validation Failed")
                .withDetail("Field 'email' is required")
                .withType(URI.create("https://example.com/problems/validation"))
                .addExtensionMember("errorCode", "USR_001")
                .build();
        }
    }
}
