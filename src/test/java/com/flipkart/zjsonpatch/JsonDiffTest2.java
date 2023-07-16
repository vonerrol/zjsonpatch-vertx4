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
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author ctranxuan (streamdata.io).
 */
public class JsonDiffTest2 {
    private static JsonArray jsonNode;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/diff.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonNode = new JsonArray(testData);
    }

    @Test
    public void testPatchAppliedCleanly() {
        for (int i = 0; i < jsonNode.size(); i++) {
            Object first = jsonNode.getJsonObject(i).getValue("first");
            Object second = jsonNode.getJsonObject(i).getValue("second");
            JsonArray patch = jsonNode.getJsonObject(i).getJsonArray("patch");
            String message = jsonNode.getJsonObject(i).getString("message");

            Object secondPrime = JsonPatch.apply(patch, first);

            Assert.assertThat(message, secondPrime, equalTo(second));
        }

    }
}
