package io.muserver.docs;

import java.util.HashMap;

public class MapBuilder extends HashMap<String,Object> {
    public MapBuilder with(String key, Object value) {
        put(key, value);
        return this;
    }
}
