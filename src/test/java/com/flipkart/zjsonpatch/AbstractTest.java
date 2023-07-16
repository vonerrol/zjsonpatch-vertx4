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
import org.apache.commons.io.output.StringBuilderWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public abstract class AbstractTest {

    @Parameter
    public PatchTestCase p;

    protected boolean matchOnErrors() {
        return true;
    }

    @Test
    public void test() throws Exception {
        if (p.isOperation()) {
            testOperation();
        } else {
            testError();
        }
    }

    private void testOperation() throws Exception {
        JsonObject node = p.getNode();

        Object doc = node.getValue("node");
        Object expected = node.getValue("expected");
        JsonArray patch = node.getJsonArray("op");
        String message = node.containsKey("message") ? node.getString("message") : "";

        Object result = JsonPatch.apply(patch, doc);
        String failMessage = "The following test failed: \n" +
                "message: " + message + '\n' +
                "at: " + p.getSourceFile();
        assertEquals(failMessage, expected, result);
    }

    private Class<?> exceptionType(String type) throws ClassNotFoundException {
        return Class.forName(type.contains(".") ? type : "com.flipkart.zjsonpatch." + type);
    }

    private String errorMessage(String header) {
        return errorMessage(header, null);
    }
    private String errorMessage(String header, Exception e) {
        StringBuilder res =
                new StringBuilder()
                        .append(header)
                        .append("\nFull test case (in file ")
                        .append(p.getSourceFile())
                        .append("):\n")
                        .append(p.getNode().encodePrettily());
        if (e != null) {
            res.append("\nFull error: ");
            e.printStackTrace(new PrintWriter(new StringBuilderWriter(res)));
        }
        return res.toString();
    }

    private void testError() throws ClassNotFoundException {
        JsonObject node = p.getNode();
        Object first = node.getValue("node");
        JsonArray patch = node.getJsonArray("op");
        String message = node.getString("message");
        Class<?> type =
                node.containsKey("type") ? exceptionType(node.getString("type")) : JsonPatchApplicationException.class;

        try {
            JsonPatch.apply(patch, first);

            fail(errorMessage("Failure expected: " + message));
        } catch (Exception e) {
            if (matchOnErrors()) {
                StringWriter fullError = new StringWriter();
                e.printStackTrace(new PrintWriter(fullError));

                assertThat(
                        errorMessage("Operation failed but with wrong exception type", e),
                        e,
                        instanceOf(type));
                if (message != null) {
                    assertThat(
                            errorMessage("Operation failed but with wrong message", e),
                            e.toString(),
                            containsString(message));    // equalTo would be better, but fail existing tests
                }
            }
        }
    }
}
