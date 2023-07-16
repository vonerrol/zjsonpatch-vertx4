/*
 * Copyright 2016 flipkart.com zjsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flipkart.zjsonpatch;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Unit test
 */
public class JsonDiffTest {
    private static JsonArray jsonArray;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/sample.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonArray = new JsonArray(testData);
    }

    @Test
    public void testSampleJsonDiff() {
        for (int i = 0; i < jsonArray.size(); i++) {
            Object first = jsonArray.getJsonObject(i).getValue("first");
            Object second = jsonArray.getJsonObject(i).getValue("second");
            JsonArray actualPatch = JsonDiff.asJson(first, second);
            Object secondPrime = JsonPatch.apply(actualPatch, first);
            Assert.assertEquals("JSON Patch not symmetrical [index=" + i + ", first=" + first + "]", second, secondPrime);
        }
    }

    @Test
    public void testGeneratedJsonDiff() {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            JsonArray first = TestDataGenerator.generate(random.nextInt(10));
            JsonArray second = TestDataGenerator.generate(random.nextInt(10));
            JsonArray actualPatch = JsonDiff.asJson(first, second);
            Object secondPrime = JsonPatch.apply(actualPatch, first);
            Assert.assertEquals(second, secondPrime);
        }
    }

    @Test
    public void testRenderedRemoveOperationOmitsValueByDefault() {
        JsonObject source = new JsonObject();
        JsonObject target = new JsonObject();
        source.put("field", "value");

        JsonArray diff = JsonDiff.asJson(source, target);

        Assert.assertEquals(Operation.REMOVE.rfcName(), diff.getJsonObject(0).getString("op"));
        Assert.assertEquals("/field", diff.getJsonObject(0).getString("path"));
        Assert.assertNull(diff.getJsonObject(0).getValue("value"));
    }

    @Test
    public void testRenderedRemoveOperationRetainsValueIfOmitDiffFlagNotSet() {
        JsonObject source = new JsonObject();
        JsonObject target = new JsonObject();
        source.put("field", "value");

        EnumSet<DiffFlags> flags = DiffFlags.defaults().clone();
        Assert.assertTrue("Expected OMIT_VALUE_ON_REMOVE by default", flags.remove(DiffFlags.OMIT_VALUE_ON_REMOVE));
        JsonArray diff = JsonDiff.asJson(source, target, flags);

        Assert.assertEquals(Operation.REMOVE.rfcName(), diff.getJsonObject(0).getString("op"));
        Assert.assertEquals("/field", diff.getJsonObject(0).getString("path"));
        Assert.assertEquals("value", diff.getJsonObject(0).getString("value"));
    }

    @Test
    public void testRenderedOperationsExceptMoveAndCopy() throws Exception {
        JsonObject source = new JsonObject("{\"age\": 10}");
        JsonObject target = new JsonObject("{\"height\": 10}");

        EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone(); //only have ADD, REMOVE, REPLACE, Don't normalize operations into MOVE & COPY

        JsonArray diff = JsonDiff.asJson(source, target, flags);

        for (JsonObject d : (List<JsonObject>) diff.getList()) {
            Assert.assertNotEquals(Operation.MOVE.rfcName(), d.getString("op"));
            Assert.assertNotEquals(Operation.COPY.rfcName(), d.getString("op"));
        }

        Object targetPrime = JsonPatch.apply(diff, source);
        Assert.assertEquals(target, targetPrime);
    }

    @Test
    public void testPath() throws Exception {
        JsonObject source = new JsonObject("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world\"}]}}");
        JsonArray patch = new JsonArray("[{\"op\":\"copy\",\"from\":\"/profiles/def/0\", \"path\":\"/profiles/def/0\"},{\"op\":\"replace\",\"path\":\"/profiles/def/0/hello\",\"value\":\"world2\"}]");

        JsonObject target = (JsonObject) JsonPatch.apply(patch, source);
        JsonObject expected = new JsonObject("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world2\"},{\"hello\":\"world\"}]}}");
        Assert.assertEquals(target, expected);
    }

    @Test
    public void testJsonDiffReturnsEmptyNodeExceptionWhenBothSourceAndTargetNodeIsNull() {
        JsonArray diff = JsonDiff.asJson(null, null);
        assertEquals(0, diff.size());
    }

    @Test
    public void testJsonDiffShowsDiffWhenSourceNodeIsNull() {
        String target = "{ \"K1\": {\"K2\": \"V1\"} }";
        JsonArray diff = JsonDiff.asJson(null, new JsonObject(target));
        assertEquals(1, diff.size());

        System.out.println(diff);
        assertEquals(Operation.ADD.rfcName(), diff.getJsonObject(0).getString("op"));
        assertEquals(JsonPointer.ROOT.toString(), diff.getJsonObject(0).getString("path"));
        assertEquals("V1", diff.getJsonObject(0).getJsonObject("value").getJsonObject("K1").getString("K2"));
    }

    @Test
    public void testJsonDiffShowsDiffWhenTargetNodeIsNullWithFlags() {
        String source = "{ \"K1\": \"V1\" }";
        JsonObject sourceNode = new JsonObject(source);
        JsonArray diff = JsonDiff.asJson(sourceNode, null, EnumSet.of(DiffFlags.ADD_ORIGINAL_VALUE_ON_REPLACE));

        assertEquals(1, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.getJsonObject(0).getString("op"));
        assertEquals(JsonPointer.ROOT.toString(), diff.getJsonObject(0).getString("path"));
        assertEquals("V1", diff.getJsonObject(0).getJsonObject("value").getString("K1"));
    }
}
