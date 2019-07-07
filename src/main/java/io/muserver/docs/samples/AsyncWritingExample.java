package io.muserver.docs.samples;

import io.muserver.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.muserver.MuServerBuilder.httpServer;

public class AsyncWritingExample {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .addHandler(Method.GET, "/guangzhou.jpg", (request, response, pathParams) -> {

                response.contentType(ContentTypes.IMAGE_JPEG);

                AsyncHandle asyncHandle = request.handleAsync();
                Path path = Paths.get("src/test/resources/guangzhou.jpg");

                AsynchronousFileChannel channel = AsynchronousFileChannel.open(path);
                FileChannelToResponsePump pump = new FileChannelToResponsePump(asyncHandle, channel);
                pump.pumpIt();

            })
            .start();

        System.out.println("Download the file at " + server.uri().resolve("/guangzhou.jpg"));

    }

    private static class FileChannelToResponsePump implements CompletionHandler<Integer, Object> {

        private final AsynchronousFileChannel channel;
        private final AsyncHandle asyncHandle;
        private final ByteBuffer buffer = ByteBuffer.allocate(8192);
        private long bytesSent = 0;

        FileChannelToResponsePump(AsyncHandle asyncHandle, AsynchronousFileChannel channel) {
            this.asyncHandle = asyncHandle;
            this.channel = channel;
        }

        public void pumpIt() {
            // Read from the file into the buffer, starting at position 0, and call 'this' instance's
            // completed method when read.
            channel.read(buffer, 0, null, this);
        }

        @Override
        public void completed(Integer bytesRead, Object attachment) {
            buffer.flip();
            if (bytesRead != -1) {

                // Write the jpg bytes from the buffer to the response channel
                asyncHandle.write(buffer, new DoneCallback() {

                    @Override
                    public void onComplete(Throwable errorIfFailed) throws Exception {
                        if (errorIfFailed == null) {
                            // Clear the buffer, and then start reading the next segment of data
                            buffer.clear();
                            bytesSent += bytesRead;
                            channel.read(buffer, bytesSent, null, FileChannelToResponsePump.this);
                        } else {
                            // client probably disconnected so stop reading and close the request
                            asyncHandle.complete();
                        }
                    }

                });
            } else {
                // File reading is complete. End the response and close the file channel.
                asyncHandle.complete();
                try {
                    channel.close();
                } catch (IOException ignored) { }
            }
        }

        @Override
        public void failed(Throwable exc, Object attachment) {
            // File reading failed. Log an error and send a 500 to the client.
            asyncHandle.complete(exc);
        }
    }
}
