package com.flipkart.zjsonpatch;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

enum JsonType {
    OBJECT,
    ARRAY,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL;

    public static JsonType of(Object o) {
        if (o == null) {
            return NULL;
        }
        if (o instanceof String) {
            return STRING;
        }
        if (o instanceof Number) {
            return NUMBER;
        }
        if (o instanceof Boolean) {
            return BOOLEAN;
        }
        if (o instanceof JsonArray) {
            return ARRAY;
        }
        if (o instanceof JsonObject) {
            return OBJECT;
        }
        throw new IllegalArgumentException("Unknown type: " + o.getClass());
    }

    public boolean isNull() {
        return this == NULL;
    }

    public boolean isString() {
        return this == STRING;
    }

    public boolean isNumber() {
        return this == NUMBER;
    }

    public boolean isBoolean() {
        return this == BOOLEAN;
    }

    public boolean isArray() {
        return this == ARRAY;
    }

    public boolean isObject() {
        return this == OBJECT;
    }
}
