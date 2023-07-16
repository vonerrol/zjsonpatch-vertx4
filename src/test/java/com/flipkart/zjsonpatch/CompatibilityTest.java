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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;

import static com.flipkart.zjsonpatch.CompatibilityFlags.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CompatibilityTest {

    JsonArray addNodeWithMissingValue;
    JsonArray replaceNodeWithMissingValue;
    JsonArray removeNoneExistingArrayElement;
    JsonArray replaceNode;
    JsonArray removeNode;

    @Before
    public void setUp() throws Exception {
        addNodeWithMissingValue = new JsonArray("[{\"op\":\"add\",\"path\":\"/a\"}]");
        replaceNodeWithMissingValue = new JsonArray("[{\"op\":\"replace\",\"path\":\"/a\"}]");
        removeNoneExistingArrayElement = new JsonArray("[{\"op\": \"remove\",\"path\": \"/b/0\"}]");
        replaceNode = new JsonArray("[{\"op\":\"replace\",\"path\":\"/a\",\"value\":true}]");
        removeNode = new JsonArray("[{\"op\":\"remove\",\"path\":\"/b\"}]");
    }

    @Test
    public void withFlagAddShouldTreatMissingValuesAsNulls() throws IOException {
        JsonObject expected = new JsonObject("{\"a\":null}");
        JsonObject result = (JsonObject) JsonPatch.apply(addNodeWithMissingValue, new JsonObject(), EnumSet.of(MISSING_VALUES_AS_NULLS));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagAddNodeWithMissingValueShouldValidateCorrectly() {
        JsonPatch.validate(addNodeWithMissingValue, EnumSet.of(MISSING_VALUES_AS_NULLS));
    }

    @Test
    public void withFlagReplaceShouldTreatMissingValuesAsNull() throws IOException {
        JsonObject source = new JsonObject("{\"a\":\"test\"}");
        JsonObject expected = new JsonObject("{\"a\":null}");
        JsonObject result = (JsonObject) JsonPatch.apply(replaceNodeWithMissingValue, source, EnumSet.of(MISSING_VALUES_AS_NULLS));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagReplaceNodeWithMissingValueShouldValidateCorrectly() {
        JsonPatch.validate(addNodeWithMissingValue, EnumSet.of(MISSING_VALUES_AS_NULLS));
    }

    @Test
    public void withFlagIgnoreRemoveNoneExistingArrayElement() throws IOException {
        JsonObject source = new JsonObject("{\"b\": []}");
        JsonObject expected = new JsonObject("{\"b\": []}");
        JsonObject result = (JsonObject) JsonPatch.apply(removeNoneExistingArrayElement, source, EnumSet.of(REMOVE_NONE_EXISTING_ARRAY_ELEMENT));
        assertThat(result, equalTo(expected));
    }

    @Test
    public void withFlagReplaceShouldAddValueWhenMissingInTarget() throws Exception {
        JsonObject expected = new JsonObject("{\"a\": true}");
        JsonObject result = (JsonObject) JsonPatch.apply(replaceNode, new JsonObject(), EnumSet.of(ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));
        assertThat(result, equalTo(expected));
    }

    @Test(expected = JsonPatchApplicationException.class)
    public void withFlagRemoveMissingValueShouldThrow() throws Exception {
        JsonObject source = new JsonObject("{\"a\": true}");
        JsonPatch.apply(removeNode, source, EnumSet.of(FORBID_REMOVE_MISSING_OBJECT));
    }

    @Test
    public void withFlagRemoveShouldRemove() throws Exception {
        JsonObject source = new JsonObject("{\"b\": true}");
        JsonObject expected = new JsonObject("{}");
        JsonObject result = (JsonObject) JsonPatch.apply(removeNode, source, EnumSet.of(FORBID_REMOVE_MISSING_OBJECT));
        assertThat(result, equalTo(expected));
    }
}
