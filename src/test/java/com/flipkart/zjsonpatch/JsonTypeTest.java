package com.flipkart.zjsonpatch;

import static org.junit.jupiter.api.Assertions.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class JsonTypeTest {

    @Test
    public void ofNullReturnsNullType() {
        assertEquals(JsonType.NULL, JsonType.of(null));
    }

    @Test
    public void ofNumberReturnsNumberType() {
        assertEquals(JsonType.NUMBER, JsonType.of(0));
    }

    @Test
    public void ofStringReturnsStringType() {
        assertEquals(JsonType.STRING, JsonType.of(""));
    }

    @Test
    public void ofBooleanReturnsBooleanType() {
        assertEquals(JsonType.BOOLEAN, JsonType.of(true));
    }

    @Test
    public void ofJsonArrayReturnsArrayType() {
        assertEquals(JsonType.ARRAY, JsonType.of(new JsonArray()));
    }

    @Test
    public void ofJsonObjectReturnsObjectType() {
        assertEquals(JsonType.OBJECT, JsonType.of(new JsonObject()));
    }

    @Test
    public void ofUnknownTypeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> JsonType.of(new Object()));
    }
}