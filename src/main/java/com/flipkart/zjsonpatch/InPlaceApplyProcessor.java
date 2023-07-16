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

class InPlaceApplyProcessor implements JsonPatchProcessor {

    private Object target;
    private EnumSet<CompatibilityFlags> flags;

    InPlaceApplyProcessor(Object target) {
        this(target, CompatibilityFlags.defaults());
    }

    InPlaceApplyProcessor(Object target, EnumSet<CompatibilityFlags> flags) {
        this.target = target;
        this.flags = flags;
    }

    public Object result() {
        return target;
    }

    @Override
    public void move(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
        Object valueNode = fromPath.evaluate(target);
        remove(fromPath);
        set(toPath, valueNode, Operation.MOVE);
    }

    @Override
    public void copy(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
        Object valueNode = fromPath.evaluate(target);
        Object valueToCopy = valueNode != null ? VertxJsonUtil.deepCopy(valueNode) : null;
        set(toPath, valueToCopy, Operation.COPY);
    }

    private static String show(Object value) {
        if (value == null)
            return "null";
        else if (JsonType.of(value).isArray())
            return "array";
        else if (JsonType.of(value).isObject())
            return "object";
        else
            return "value " + value.toString();     // Caveat: numeric may differ from source (e.g. trailing zeros)
    }

    @Override
    public void test(JsonPointer path, Object value) throws JsonPointerEvaluationException {
        Object valueNode = path.evaluate(target);
        if (valueNode == null && value == null)
            return;
        else if (valueNode == null || value == null)
            throw new JsonPatchApplicationException(
                    "Expected " + show(value) + " but found " + show(valueNode), Operation.TEST, path);
        else if (!valueNode.equals(value))
            throw new JsonPatchApplicationException(
                    "Expected " + show(value) + " but found " + show(valueNode), Operation.TEST, path);
    }

    @Override
    public void add(JsonPointer path, Object value) throws JsonPointerEvaluationException {
        set(path, value, Operation.ADD);
    }

    @Override
    public void replace(JsonPointer path, Object value) throws JsonPointerEvaluationException {
        if (path.isRoot()) {
            target = value;
            return;
        }

        Object parentNode = path.getParent().evaluate(target);
        JsonPointer.RefToken token = path.last();
        if (JsonType.of(parentNode).isObject()) {
            JsonObject parentObject = (JsonObject) parentNode;
            if (!flags.contains(CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE) &&
                    !parentObject.containsKey(token.getField()))
                throw new JsonPatchApplicationException(
                        "Missing field \"" + token.getField() + "\"", Operation.REPLACE, path.getParent());
            parentObject.put(token.getField(), value);
        } else if (JsonType.of(parentNode).isArray()) {
            JsonArray parentArray = (JsonArray) parentNode;
            if (token.getIndex() >= parentArray.size())
                throw new JsonPatchApplicationException(
                        "Array index " + token.getIndex() + " out of bounds", Operation.REPLACE, path.getParent());
            parentArray.set(token.getIndex(), value);
        } else {
            throw new JsonPatchApplicationException(
                    "Can't reference past scalar value", Operation.REPLACE, path.getParent());
        }
    }

    @Override
    public void remove(JsonPointer path) throws JsonPointerEvaluationException {
        if (path.isRoot())
            throw new JsonPatchApplicationException("Cannot remove document root", Operation.REMOVE, path);

        Object parentNode = path.getParent().evaluate(target);
        JsonPointer.RefToken token = path.last();
        if (JsonType.of(parentNode).isObject()) {
            JsonObject parentObject = (JsonObject) parentNode;
            if (flags.contains(CompatibilityFlags.FORBID_REMOVE_MISSING_OBJECT) && !parentObject.containsKey(token.getField()))
                throw new JsonPatchApplicationException(
                        "Missing field " + token.getField(), Operation.REMOVE, path.getParent());
            parentObject.remove(token.getField());
        }
        else if (JsonType.of(parentNode).isArray()) {
            JsonArray parentArray = (JsonArray) parentNode;
            if (token.getIndex() >= parentArray.size()) {
                if (!flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT))
                    throw new JsonPatchApplicationException(
                            "Array index " + token.getIndex() + " out of bounds", Operation.REMOVE, path.getParent());
            } else {
                parentArray.remove(token.getIndex());
            }
        } else {
            throw new JsonPatchApplicationException(
                    "Cannot reference past scalar value", Operation.REMOVE, path.getParent());
        }
    }



    private void set(JsonPointer path, Object value, Operation forOp) throws JsonPointerEvaluationException {
        if (path.isRoot())
            target = value;
        else {
            Object parentNode = path.getParent().evaluate(target);
            if (JsonType.of(parentNode).isArray())
                addToArray(path, value, parentNode);
            else if (JsonType.of(parentNode).isObject())
                addToObject(path, parentNode, value);
            else
                throw new JsonPatchApplicationException("Cannot reference past scalar value", forOp, path.getParent());
        }
    }

    private void addToObject(JsonPointer path, Object node, Object value) {
        final JsonObject target = (JsonObject) node;
        String key = path.last().getField();
        target.put(key, value);
    }

    private void addToArray(JsonPointer path, Object value, Object parentNode) {
        final JsonArray target = (JsonArray) parentNode;
        int idx = path.last().getIndex();

        if (idx == JsonPointer.LAST_INDEX) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            if (idx > target.size())
                throw new JsonPatchApplicationException(
                        "Array index " + idx + " out of bounds", Operation.ADD, path.getParent());
            target.add(idx, value);
        }
    }
}
