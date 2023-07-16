package com.flipkart.zjsonpatch;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RFC6901Tests {
    @Test
    void testRFC6901Compliance() throws IOException {
        JsonObject data = new JsonObject(TestUtils.loadFromResources("/rfc6901/data.json"));
        JsonObject testData = data.getJsonObject("testData");

        JsonObject emptyJson = new JsonObject();
        JsonArray patch = JsonDiff.asJson(emptyJson, testData);
        Object result = JsonPatch.apply(patch, emptyJson);
        assertEquals(testData, result);
    }
}
