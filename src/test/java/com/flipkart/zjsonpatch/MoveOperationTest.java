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
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author ctranxuan (streamdata.io).
 */
public class MoveOperationTest extends AbstractTest {

    @Parameterized.Parameters
    public static Collection<PatchTestCase> data() throws IOException {
        return PatchTestCase.load("move");
    }

    @Test
    public void testMoveValueGeneratedHasNoValue() {
        JsonObject jsonNode1 = new JsonObject("{ \"foo\": { \"bar\": \"baz\", \"waldo\": \"fred\" }, \"qux\": { \"corge\": \"grault\" } }");
        JsonObject jsonNode2 = new JsonObject("{ \"foo\": { \"bar\": \"baz\" }, \"qux\": { \"corge\": \"grault\", \"thud\": \"fred\" } }");
        JsonArray patch = new JsonArray("[{\"op\":\"move\",\"from\":\"/foo/waldo\",\"path\":\"/qux/thud\"}]");

        JsonArray diff = JsonDiff.asJson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }

    @Test
    public void testMoveArrayGeneratedHasNoValue() {
        JsonObject jsonNode1 = new JsonObject("{ \"foo\": [ \"all\", \"grass\", \"cows\", \"eat\" ] }");
        JsonObject jsonNode2 = new JsonObject("{ \"foo\": [ \"all\", \"cows\", \"eat\", \"grass\" ] }");
        JsonArray patch = new JsonArray("[{\"op\":\"move\",\"from\":\"/foo/1\",\"path\":\"/foo/3\"}]");

        JsonArray diff = JsonDiff.asJson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }
}
