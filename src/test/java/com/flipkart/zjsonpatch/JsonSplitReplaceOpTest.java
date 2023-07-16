package com.flipkart.zjsonpatch;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * @author isopropylcyanide
 */
public class JsonSplitReplaceOpTest {

    @Test
    public void testJsonDiffSplitsReplaceIntoAddAndRemoveOperationWhenFlagIsAdded() {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        JsonObject sourceNode = new JsonObject(source);
        JsonObject targetNode = new JsonObject(target);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(3, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.getJsonObject(0).getString("op"));
        assertEquals("/ids/1", diff.getJsonObject(0).getString("path"));
        assertEquals("F3", diff.getJsonObject(0).getString("value"));

        assertEquals(Operation.ADD.rfcName(), diff.getJsonObject(1).getString("op"));
        assertEquals("/ids/1", diff.getJsonObject(1).getString("path"));
        assertEquals("F6", diff.getJsonObject(1).getString("value"));

        assertEquals(Operation.ADD.rfcName(), diff.getJsonObject(2).getString("op"));
        assertEquals("/ids/2", diff.getJsonObject(2).getString("path"));
        assertEquals("F4", diff.getJsonObject(2).getString("value"));
    }

    @Test
    public void testJsonDiffDoesNotSplitReplaceIntoAddAndRemoveOperationWhenFlagIsNotAdded() {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        JsonObject sourceNode = new JsonObject(source);
        JsonObject targetNode = new JsonObject(target);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode);
        System.out.println(diff);
        assertEquals(2, diff.size());
        assertEquals(Operation.REPLACE.rfcName(), diff.getJsonObject(0).getString("op"));
        assertEquals("/ids/1", diff.getJsonObject(0).getString("path"));
        assertEquals("F6", diff.getJsonObject(0).getString("value"));

        assertEquals(Operation.ADD.rfcName(), diff.getJsonObject(1).getString("op"));
        assertEquals("/ids/2", diff.getJsonObject(1).getString("path"));
        assertEquals("F4", diff.getJsonObject(1).getString("value"));
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyRemove() {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F3\"] }";

        JsonObject sourceNode = new JsonObject(source);
        JsonObject targetNode = new JsonObject(target);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.getJsonObject(0).getString("op"));
        assertEquals("/ids/0", diff.getJsonObject(0).getString("path"));
        assertEquals("F1", diff.getJsonObject(0).getString("value"));
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyAdd() {
        String source = "{ \"ids\": [ \"F1\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\"] }";

        JsonObject sourceNode = new JsonObject(source);
        JsonObject targetNode = new JsonObject(target);

        JsonArray diff = JsonDiff.asJson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.ADD.rfcName(), diff.getJsonObject(0).getString("op"));
        assertEquals("/ids/1", diff.getJsonObject(0).getString("path"));
        assertEquals("F6", diff.getJsonObject(0).getString("value"));
    }
}
