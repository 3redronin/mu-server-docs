package io.muserver.docs;

import io.muserver.MuResponse;
import io.muserver.Mutils;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.DefaultEnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;

import java.io.File;
import java.io.IOException;

public class ViewHandler {
    private final boolean isLocal;
    private final EnvironmentConfiguration config;
    private final File viewBase;

    public ViewHandler(boolean isLocal) {
        this.isLocal = isLocal;
        if (isLocal) {
            try {
                viewBase = new File("src/main/resources/views").getCanonicalFile();
            } catch (IOException e) {
                throw new RuntimeException("Error loading dir", e);
            }
            if (!viewBase.isDirectory()) {
                throw new RuntimeException("Could not find " + Mutils.fullPath(viewBase));
            }

            config = EnvironmentConfigurationBuilder.configuration()
                .parser().withoutTemplateCache()
                .and()
                .build();
        } else {
            config = new DefaultEnvironmentConfiguration();
            viewBase = null;
        }
    }

    protected JtwigModel model() {
        return JtwigModel.newModel()
            .with("version", "0.19.3");
    }

    protected void render(MuResponse response, String viewName, JtwigModel model) {

        response.contentType("text/html");

        JtwigTemplate template;
        if (isLocal) {
            template = JtwigTemplate.fileTemplate(new File(viewBase, viewName + ".html"), config);
        } else {
            template = JtwigTemplate.classpathTemplate("/views/" + viewName + ".html", config);
        }

        template.render(model, response.outputStream());

    }

}
