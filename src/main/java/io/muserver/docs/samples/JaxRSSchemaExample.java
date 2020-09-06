package io.muserver.docs.samples;

import io.muserver.MuServer;
import io.muserver.MuServerBuilder;
import io.muserver.openapi.SchemaObject;
import io.muserver.openapi.SchemaObjectBuilder;
import io.muserver.rest.RestHandlerBuilder;
import io.muserver.rest.SchemaObjectCustomizer;
import io.muserver.rest.SchemaObjectCustomizerContext;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static io.muserver.rest.CORSConfigBuilder.corsConfig;
import static java.util.Arrays.asList;

public class JaxRSSchemaExample {

    /**
     * A data class that is able to serialise to a JSONObject
     */
    static class Product {
        public final String name;
        public final double price;

        Product(String name, double price) {
            this.name = name;
            this.price = price;
        }

        public JSONObject toJSON() {
            return new JSONObject()
                .put("name", name)
                .put("price", price);
        }
    }

    /**
     * A JAX-RS message body writer that converts a Product object to JSON
     */
    @Produces("application/json")
    private static class ProductWriter implements MessageBodyWriter<Product> {
        public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
            return Product.class.isAssignableFrom(type);
        }

        public void writeTo(Product product, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
            product.toJSON().write(new OutputStreamWriter(entityStream));
        }
    }

    /**
     * The resource class that returns a product. Because it implements SchemaObjectCustomizer
     * the customize method is called whenever the OpenAPI document is requested.
     */
    @Path("/api/products")
    static class ProductResource implements SchemaObjectCustomizer {
        @GET
        @Produces("application/json")
        public Product randomProduct() {
            return new Product("Mu Product", 99.99);
        }

        @Override
        public SchemaObjectBuilder customize(SchemaObjectBuilder builder, SchemaObjectCustomizerContext context) {
            if (context.resource() == this && context.type().equals(Product.class)) {
                Map<String, SchemaObject> props = new HashMap<>();
                props.put("name", SchemaObjectBuilder.schemaObjectFrom(String.class).build());
                props.put("price", SchemaObjectBuilder.schemaObjectFrom(double.class).build());
                builder.withProperties(props)
                    .withRequired(asList("name", "price"))
                    // If a map is given as an example object, then the properties are listed separately.
                    // Alternatively, a string can be given as the example.
                    .withExample(new Product("Example Product", 9.99).toJSON().toMap());
            }
            return builder;
        }
    }

    public static void main(String[] args) {

        MuServer server = MuServerBuilder.muServer()
            .withHttpsPort(13571)
            .addHandler(
                RestHandlerBuilder.restHandler(new ProductResource())
                    .withOpenApiHtmlUrl("/api.html")
                    .withOpenApiJsonUrl("/openapi.json")
                    .addCustomWriter(new ProductWriter())
                    .withCORS(corsConfig().withAllowedOrigins("https://petstore.swagger.io"))
            )
            .start();

        System.out.println("API HTML: " + server.uri().resolve("/api.html"));
        System.out.println("API JSON: " + server.uri().resolve("/openapi.json"));

    }

}