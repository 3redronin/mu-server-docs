package io.muserver.docs.samples;

import io.muserver.*;
import io.muserver.handlers.ResourceHandlerBuilder;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static io.muserver.MuServerBuilder.httpServer;
import static java.nio.charset.StandardCharsets.UTF_8;

public class AsyncRequestBodyExample {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler(Method.POST, "/upload", new AsyncRequestBodyHandler())
            .addHandler(ResourceHandlerBuilder.classpathHandler("/samples"))
            .start();

        System.out.println("Upload a file at " + server.uri().resolve("/async-upload.html"));

    }

    private static class AsyncRequestBodyHandler implements RouteHandler {
        @Override
        public void handle(MuRequest request, MuResponse response, Map<String, String> pathParams) {

            AsyncHandle asyncHandle = request.handleAsync();

            long expectedSize = Long.parseLong(request.headers().get("content-length", "-1"));

            response.contentType("text/plain;charset=utf-8");
            AtomicLong total = new AtomicLong();

            asyncHandle.setReadListener(new RequestBodyListener() {

                public void onDataReceived(ByteBuffer buffer) {
                    int dataSize = buffer.remaining();
                    String message = "Received " + dataSize + " bytes - total so far is "
                        + total.addAndGet(dataSize) + " of " + expectedSize + " bytes\n";
                    // Note: do not call any blocking response writing methods from a data-received
                    // callback. Instead use the async writer on the handle.
                    asyncHandle.write(Mutils.toByteBuffer(message));
                    System.out.print(message);
                }

                public void onComplete() {
                    String message = "\n\nUpload complete. Received " + total.get() + " bytes.";
                    System.out.println(message);
                    asyncHandle.write(ByteBuffer.wrap(message.getBytes(UTF_8)));
                    asyncHandle.complete();
                }

                public void onError(Throwable t) {
                    System.out.println("\n\nError while reading: " + t);
                    asyncHandle.complete();
                }
            });

        }
    }
}
