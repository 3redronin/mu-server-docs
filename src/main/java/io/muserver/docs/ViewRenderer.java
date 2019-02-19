package io.muserver.docs;

import io.muserver.MuResponse;
import org.jtwig.JtwigModel;

public interface ViewRenderer {
    void render(MuResponse response, String relativePath, JtwigModel model);

    JtwigModel model();
}
