package io.muserver.docs.samples;

import io.muserver.MuHandler;
import io.muserver.MuRequest;
import io.muserver.MuResponse;
import io.muserver.MuServer;

import javax.ws.rs.ClientErrorException;

import static io.muserver.MuServerBuilder.httpServer;

public class FilterAndStateExample {

    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler(new LoggingHandler())
            .addHandler(new HostHeaderChecker())
            .addHandler(new FakeAuthHandler())
            .addHandler(new HelloHandler())
            .start();
        System.out.println("Server started at " + server.uri());
    }

    // Log the request method, path, and IP address
    static class LoggingHandler implements MuHandler {
        public boolean handle(MuRequest request, MuResponse response) {
            System.out.println("Recieved " + request + " from " + request.remoteAddress());
            return false; // so that the next handler is invoked
        }
    }

    // Block any requests where the Host header is not localhost
    static class HostHeaderChecker implements MuHandler {
        public boolean handle(MuRequest request, MuResponse response) {
            if (!request.uri().getHost().equals("localhost")) {
                throw new ClientErrorException("The host header must be 'localhost'", 400);
            }
            return false;
        }
    }

    // Puts a username on the request for subsequent handlers to use
    static class FakeAuthHandler implements MuHandler {
        public boolean handle(MuRequest request, MuResponse response) {
            String username = "bob"; // hard coded for demo
            request.attribute("username", username);
            return false;
        }
    }

    static class HelloHandler implements MuHandler {
        public boolean handle(MuRequest request, MuResponse response) {
            String username = (String)request.attribute("username");
            response.write("Hello, " + username);
            return true;
        }
    }
}
