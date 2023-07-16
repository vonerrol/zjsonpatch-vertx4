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
import org.junit.Test;

import java.io.IOException;
import java.util.EnumSet;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * User: holograph
 * Date: 03/08/16
 */
public class ApiTest {

    @Test
    public void applyInPlaceMutatesSource() throws Exception {
        JsonArray patch = new JsonArray("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        JsonObject source = new JsonObject();
        JsonObject beforeApplication = source.copy();
        JsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyDoesNotMutateSource() throws Exception {
        JsonArray patch = new JsonArray("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        JsonObject source = new JsonObject();
        JsonPatch.applyInPlace(patch, source);
        assertThat(source.getValue("b"), is("b-value"));
    }

    @Test
    public void applyDoesNotMutateSource2() throws Exception {
        JsonArray patch = new JsonArray("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        JsonObject source = new JsonObject();
        JsonObject beforeApplication = source.copy();
        JsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyInPlaceMutatesSourceWithCompatibilityFlags() throws Exception {
        JsonArray patch = new JsonArray("[{ \"op\": \"add\", \"path\": \"/b\" }]");
        JsonObject source = new JsonObject();
        JsonPatch.applyInPlace(patch, source, EnumSet.of(CompatibilityFlags.MISSING_VALUES_AS_NULLS));
        assertTrue(source.containsKey("b"));
        assertTrue(source.getValue("b") == null);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingAnInvalidArrayShouldThrowAnException() throws IOException {
        JsonArray invalid = new JsonArray("[1, 2, 3, 4, 5]");
        JsonObject to = new JsonObject("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void applyingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
        JsonArray invalid = new JsonArray("[{\"op\": \"what\"}]");
        JsonObject to = new JsonObject("{\"a\":1}");
        JsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingAnInvalidArrayShouldThrowAnException() throws IOException {
        JsonArray invalid = new JsonArray("[1, 2, 3, 4, 5]");
        JsonPatch.validate(invalid);
    }

    @Test(expected = InvalidJsonPatchException.class)
    public void validatingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
        JsonArray invalid = new JsonArray("[{\"op\": \"what\"}]");
        JsonPatch.validate(invalid);
    }
}

