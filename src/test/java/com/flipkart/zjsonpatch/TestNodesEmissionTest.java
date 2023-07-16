package com.flipkart.zjsonpatch;

import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.*;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

public class TestNodesEmissionTest {

    private static EnumSet<DiffFlags> flags;

    static {
        flags = DiffFlags.defaults();
        flags.add(DiffFlags.EMIT_TEST_OPERATIONS);
    }

    @Test
    public void testNodeEmittedBeforeReplaceOperation() throws IOException {
        JsonObject source = new JsonObject("{\"key\":\"original\"}");
        JsonObject target = new JsonObject("{\"key\":\"replaced\"}");

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        JsonObject testNode = new JsonObject("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeCopyOperation() throws IOException {
        JsonObject source = new JsonObject("{\"key\":\"original\"}");
        JsonObject target = new JsonObject("{\"key\":\"original\", \"copied\":\"original\"}");

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        JsonObject testNode = new JsonObject("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeMoveOperation() throws IOException {
        JsonObject source = new JsonObject("{\"key\":\"original\"}");
        JsonObject target = new JsonObject("{\"moved\":\"original\"}");

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        JsonObject testNode = new JsonObject("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveOperation() throws IOException {
        JsonObject source = new JsonObject("{\"key\":\"original\"}");
        JsonObject target = new JsonObject("{}");

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        JsonObject testNode = new JsonObject("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromMiddleOfArray() throws IOException {
        JsonObject source = new JsonObject("{\"key\":[1,2,3]}");
        JsonObject target = new JsonObject("{\"key\":[1,3]}");

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        JsonObject testNode = new JsonObject("{\"op\":\"test\",\"path\":\"/key/1\",\"value\":2}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromTailOfArray() throws IOException {
        JsonObject source = new JsonObject("{\"key\":[1,2,3]}");
        JsonObject target = new JsonObject("{\"key\":[1,2]}");

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        JsonObject testNode = new JsonObject("{\"op\":\"test\",\"path\":\"/key/2\",\"value\":3}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }
}
