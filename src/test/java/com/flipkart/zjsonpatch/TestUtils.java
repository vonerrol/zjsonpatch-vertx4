package com.flipkart.zjsonpatch;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {

    private TestUtils() {
    }

    public static String loadFromResources(String path) throws IOException {
        InputStream resourceAsStream = PatchTestCase.class.getResourceAsStream(path);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }
}
