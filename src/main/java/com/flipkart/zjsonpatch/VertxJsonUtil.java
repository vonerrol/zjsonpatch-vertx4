package com.flipkart.zjsonpatch;

import io.vertx.core.json.impl.JsonUtil;

class VertxJsonUtil {

    public static Object deepCopy(Object o) {
        return JsonUtil.deepCopy(o, JsonUtil.DEFAULT_CLONER);
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        if (o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }
}
