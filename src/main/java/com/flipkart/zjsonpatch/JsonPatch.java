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

import java.util.EnumSet;
import java.util.Iterator;

/**
 * User: gopi.vishwakarma
 * Date: 31/07/14
 */
public final class JsonPatch {

    private JsonPatch() {
    }

    private static String getPatchStringAttr(JsonObject jsonObject, String attr) {
        Object child = getPatchAttr(jsonObject, attr);

        if (!JsonType.of(child).isString())
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (non-text '" + attr + "' field)");

        return (String) child;
    }

    private static Object getPatchAttr(JsonObject jsonObject, String attr) {
        Object child = jsonObject.getValue(attr);
        if (child == null && !jsonObject.containsKey(attr))
            throw new InvalidJsonPatchException("Invalid JSON Patch payload (missing '" + attr + "' field)");

        return child;
    }

    private static Object getPatchAttrWithDefault(JsonObject jsonObject, String attr, Object defaultValue) {
        Object child = jsonObject.getValue(attr);
        if (child == null)
            return defaultValue;
        else
            return child;
    }

    private static void process(JsonArray patch, JsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags)
            throws InvalidJsonPatchException {

        Iterator<Object> operations = patch.iterator();
        while (operations.hasNext()) {
            Object jsonNode = operations.next();
            if (!JsonType.of(jsonNode).isObject()) throw new InvalidJsonPatchException("Invalid JSON Patch payload (not an object)");
            JsonObject jsonObject = (JsonObject) jsonNode;
            Operation operation = Operation.fromRfcName(getPatchStringAttr(jsonObject, Constants.OP));
            JsonPointer path = JsonPointer.parse(getPatchStringAttr(jsonObject, Constants.PATH));

            try {
                switch (operation) {
                    case REMOVE: {
                        processor.remove(path);
                        break;
                    }

                    case ADD: {
                        Object value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonObject, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonObject, Constants.VALUE, null);
                        processor.add(path, VertxJsonUtil.deepCopy(value));
                        break;
                    }

                    case REPLACE: {
                        Object value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonObject, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonObject, Constants.VALUE, null);
                        processor.replace(path, VertxJsonUtil.deepCopy(value));
                        break;
                    }

                    case MOVE: {
                        JsonPointer fromPath = JsonPointer.parse(getPatchStringAttr(jsonObject, Constants.FROM));
                        processor.move(fromPath, path);
                        break;
                    }

                    case COPY: {
                        JsonPointer fromPath = JsonPointer.parse(getPatchStringAttr(jsonObject, Constants.FROM));
                        processor.copy(fromPath, path);
                        break;
                    }

                    case TEST: {
                        Object value;
                        if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                            value = getPatchAttr(jsonObject, Constants.VALUE);
                        else
                            value = getPatchAttrWithDefault(jsonObject, Constants.VALUE, null);
                        processor.test(path, VertxJsonUtil.deepCopy(value));
                        break;
                    }
                }
            }
            catch (JsonPointerEvaluationException e) {
                throw new JsonPatchApplicationException(e.getMessage(), operation, e.getPath());
            }
        }
    }

    public static void validate(JsonArray patch, EnumSet<CompatibilityFlags> flags) throws InvalidJsonPatchException {
        process(patch, NoopProcessor.INSTANCE, flags);
    }

    public static void validate(JsonArray patch) throws InvalidJsonPatchException {
        validate(patch, CompatibilityFlags.defaults());
    }

    public static Object apply(JsonArray patch, Object source, EnumSet<CompatibilityFlags> flags) throws JsonPatchApplicationException {
        CopyingApplyProcessor processor = new CopyingApplyProcessor(source, flags);
        process(patch, processor, flags);
        return processor.result();
    }

    public static Object apply(JsonArray patch, Object source) throws JsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonArray patch, Object source) {
        applyInPlace(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(JsonArray patch, Object source, EnumSet<CompatibilityFlags> flags) {
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(source, flags);
        process(patch, processor, flags);
    }
}
