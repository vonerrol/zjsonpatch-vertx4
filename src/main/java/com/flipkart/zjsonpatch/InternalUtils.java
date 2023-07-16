package com.flipkart.zjsonpatch;

import io.vertx.core.json.JsonArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class InternalUtils {

    static List<Object> toList(JsonArray input) {
        int size = input.size();
        List<Object> toReturn = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            toReturn.add(input.getValue(i));
        }
        return toReturn;
    }
}
