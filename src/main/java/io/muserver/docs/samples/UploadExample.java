package io.muserver.docs.samples;

import io.muserver.Method;
import io.muserver.MuServer;
import io.muserver.Mutils;
import io.muserver.UploadedFile;
import io.muserver.handlers.ResourceHandlerBuilder;

import java.io.File;

import static io.muserver.MuServerBuilder.httpServer;

public class UploadExample {
    public static void main(String[] args) {
        MuServer server = httpServer()
            .withMaxRequestSize(1_000_000_000 /* 1GB */)
            .addHandler(Method.POST, "/upload", (request, response, pathParams) -> {

                UploadedFile file = request.uploadedFile("your-file");
                File dest = new File("target/upload-demo/" + file.filename());
                file.saveTo(dest);
                response.write(file.filename() + " (" + file.size() + " bytes) of type " +
                    file.contentType() + " saved to " + Mutils.fullPath(dest));

            })
            .addHandler(ResourceHandlerBuilder.fileOrClasspath("src/main/resources/samples", "/samples"))
            .start();
        System.out.println("Upload example started at " + server.uri().resolve("/upload.html"));
    }
}
