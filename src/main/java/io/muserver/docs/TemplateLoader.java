package io.muserver.docs;

import org.jtwig.JtwigTemplate;

interface TemplateLoader {
    JtwigTemplate load(String relativePath);
}
