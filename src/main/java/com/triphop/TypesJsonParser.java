package com.triphop;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.core.JsonToken.*;

public class TypesJsonParser {
    private JsonFactory factory;
    private final Stack<PathItem> path;

    public static TypesJsonParser of(JsonFactory factory) {
        return new TypesJsonParser(factory);
    }

    public TypesJsonParser(JsonFactory factory) {
        this.factory = factory;
        path = new Stack<>();
    }

    private static final String TYPE = "type";


    public void parse(File file, Map<String, String> allTypesAndCurrentValues) throws IOException {

        try (JsonParser parser = factory.createParser(file)) {
            //skip root object
            parser.nextToken();
            JsonToken token = parser.nextToken();
            while (parser.hasToken(token) && token != null) {
                final String currentName = parser.getCurrentName();
                if (isJsonArrayObject(token, currentName)) {
                    path.peek().incrementIndex();
                } else if (token == START_ARRAY) {
                    if (isCurrentTokenArray(currentName)) {
                        path.peek().incrementIndex();
                    }
                    pushPathItem(currentName, START_ARRAY);
                } else if (token == START_OBJECT) {
                    pushPathItem(currentName, START_OBJECT);
                } else if (isEndOfNamedObject(token, currentName) || token == END_ARRAY) {
                    path.pop();
                } else if (token == FIELD_NAME && TYPE.equalsIgnoreCase(currentName)) {
                    cacheType(allTypesAndCurrentValues, parser);
                }
                token = parser.nextToken();
            }
        }
    }

    private boolean isEndOfNamedObject(JsonToken token, String currentName) {
        return currentName != null && token == END_OBJECT;
    }

    private boolean isJsonArrayObject(JsonToken token, String currentName) {
        return (token == START_OBJECT || token.isScalarValue()) && isCurrentTokenArray(currentName);
    }

    private boolean isCurrentTokenArray(String currentName) {
        return currentName == null && !path.isEmpty() && START_ARRAY.equals(path.peek().type);
    }

    private void cacheType(Map<String, String> allTypesAndCurrentValues, JsonParser parser) throws IOException {
        final JsonToken value = parser.nextToken();
        if (value.isScalarValue())
            allTypesAndCurrentValues.put(stringifyPath(path), parser.getValueAsString());
    }

    private void pushPathItem(String currentName, JsonToken startArray) {
        PathItem pi = PathItem.of(currentName, startArray);
        path.push(pi);
    }

    private String stringifyPath(Stack<PathItem> path) {
        final Stream<String> type = Stream.of(TYPE);
        return Stream.concat(path.stream().map(p -> {
            if (p.type == START_ARRAY)
                return String.format("%s[%d]", p.name, p.index);
            else
                return p.name;
        }), type).collect(Collectors.joining("."));

    }

    static class PathItem {
        private String name;
        private JsonToken type;
        private Integer index;

        private PathItem(String name, JsonToken type) {
            this.name = name;
            this.type = type;
        }

        private PathItem(String name, JsonToken type, Integer index) {
            this(name, type);
            this.index = index;
        }

        public static PathItem of(String name, JsonToken type) {
            return new PathItem(name, type);
        }

        public static PathItem of(String name, JsonToken type, Integer index) {
            return new PathItem(name, type);
        }

        @Override
        public String toString() {
            return name + "[" + index + "]";
        }

        public void incrementIndex() {
            if (this.index == null) {
                this.index = 0;
            } else {
                ++this.index;
            }
        }
    }

}
