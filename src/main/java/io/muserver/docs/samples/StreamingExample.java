package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.rest.RestHandlerBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.*;

public class StreamingExample {
    public static void main(String[] args) {
        FindOrders findOrders = new FindOrders();
        MuServer server = MuServerBuilder.httpServer()
            .addHandler(RestHandlerBuilder.restHandler(findOrders))
            .start();
        System.out.println("Started server at " + server.uri());
    }

    @Path("/stream/orders")
    public static class FindOrders {
        @GET
        @Path("/all")
        @Produces("application/json")
        public Response get() {
            String url = "jdbc:trino://localhost/schema/catalog?user=foo&password=bar";

            StreamingOutput stream = new StreamingOutput() {
                @Override
                public void write(OutputStream os) throws IOException, WebApplicationException {
                    try (
                        Connection connection = DriverManager.getConnection(url);
                        Statement statement = connection.createStatement();
                        ResultSet rs = statement.executeQuery("SELECT orderkey, orderstatus FROM orders");
                    ) {
                        OutputStreamWriter writer = new OutputStreamWriter(os);
                        writer.write("[\n");

                        int count = 0;
                        while (rs.next()) {
                            if (count > 0) {
                                writer.write(",\n");
                            }
                            String jsonR = new StringBuilder()
                                .append("{\"orderkey\":" + rs.getInt("orderkey"))
                                .append(",\"orderstatus \":\"" + rs.getString("orderstatus") + "\"}").toString();
                            writer.write(jsonR);
                            count++;
                        }

                        writer.write("\n]");
                        writer.flush();
                    }
                    catch(SQLException e) {
                        e.printStackTrace();
                    }
                }
            };
            return Response.ok(stream).build();
        }
    }
}
