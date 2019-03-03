package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.rest.Authorizer;
import io.muserver.rest.BasicAuthSecurityFilter;
import io.muserver.rest.RestHandlerBuilder;
import io.muserver.rest.UserPassAuthenticator;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.muserver.MuServerBuilder.httpsServer;
import static io.muserver.Mutils.htmlEncode;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

public class JaxRSBasicAuth {

    private static final Map<String, Map<String, List<String>>> usersToPasswordToRoles = new HashMap<>();


    public static void main(String[] args) {

        usersToPasswordToRoles.put("Daniel", singletonMap("s@curePa55word!", asList("User", "Admin")));
        usersToPasswordToRoles.put("Frank", singletonMap("password123", asList("User")));

        MyUserPassAuthenticator authenticator = new MyUserPassAuthenticator(usersToPasswordToRoles);
        MyAuthorizer authorizer = new MyAuthorizer();

        MuServer server = httpsServer()
            .addHandler(
                RestHandlerBuilder.restHandler(new Thing())
                    .addRequestFilter(new BasicAuthSecurityFilter("My-App", authenticator, authorizer))
            )
            .addHandler(Method.GET, "/", (request, response, pathParams) -> {
                response.contentType("text/html");
                response.write(getDemoPageHtml());
            })
            .start();

        System.out.println("Browse the demo at " + server.uri());
    }

    private static String getDemoPageHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Basic Auth Demo</title><style>th, td { padding: 20px; }</style></head><body><h1>Users</h1><table><thead><tr><th>Name</th><th>Password</th><th>Roles</th></tr></thead><tbody>");
        for (Map.Entry<String, Map<String, List<String>>> user : usersToPasswordToRoles.entrySet()) {
            html.append("<tr><td>" + htmlEncode(user.getKey()) + "</td>");
            Map.Entry<String, List<String>> entry = user.getValue().entrySet().stream().findFirst().get();
            html.append("<td>" + htmlEncode(entry.getKey()) + "</td><td>" + htmlEncode(entry.getValue().stream().collect(Collectors.joining(", "))) + "</td></tr>");
        }

        html.append("</tbody></table>");

        html.append("<h2>End points</h2><ul>" +
            "<li><a href=\"/things/read\">Read (requires User role)</a></li>" +
            "<li><a href=\"/things/admin\">Admin (requires Admin role)</a></li>" +
            "</ul>");

        html.append("</body></html>");
        return html.toString();
    }

    private static class MyUser implements Principal {
        private final String name;
        private final List<String> roles;
        private MyUser(String name, List<String> roles) {
            this.name = name;
            this.roles = roles;
        }
        @Override
        public String getName() {
            return name;
        }
        public boolean isInRole(String role) {
            return roles.contains(role);
        }
    }

    static class MyUserPassAuthenticator implements UserPassAuthenticator {
        private final Map<String, Map<String, List<String>>> usersToPasswordToRoles;

        public MyUserPassAuthenticator(Map<String, Map<String, List<String>>> usersToPasswordToRoles) {
            this.usersToPasswordToRoles = usersToPasswordToRoles;
        }

        @Override
        public Principal authenticate(String username, String password) {
            Principal principal = null;
            Map<String, List<String>> user = usersToPasswordToRoles.get(username);
            if (user != null) {
                List<String> roles = user.get(password);
                if (roles != null) {
                    principal = new MyUser(username, roles);
                }
            }
            return principal;
        }
    }

    static class MyAuthorizer implements Authorizer {
        @Override
        public boolean isInRole(Principal principal, String role) {
            if (principal == null) {
                return false;
            }
            MyUser user = (MyUser)principal;
            return user.isInRole(role);
        }
    }


    @Path("/things")
    @Produces("text/plain")
    private static class Thing {

        @GET
        @Path("/read")
        public String readStuff(@Context SecurityContext securityContext) {
            if (!securityContext.isUserInRole("User")) {
                throw new ClientErrorException("This requires a User role", 403);
            }
            return "Reading stuff securely? " + securityContext.isSecure();
        }

        @GET
        @Path("/admin")
        public String doAdmin(@Context SecurityContext securityContext) {
            if (!securityContext.isUserInRole("Admin")) {
                throw new ClientErrorException("This requires an Admin role", 403);
            }
            return "Doing admin";
        }

    }


}
