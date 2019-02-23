package io.muserver.docs;

import org.jtwig.functions.FunctionRequest;
import org.jtwig.render.RenderRequest;

class MockJtwigRequest extends FunctionRequest {
    private final String[] args;

    public MockJtwigRequest(String... args) {
        super(new RenderRequest(null, null), null, null, null);
        this.args = args;
    }

    @Override
    public Object get(int index) {
        return args[index];
    }
}
