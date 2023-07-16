package com.flipkart.zjsonpatch;

import io.vertx.core.json.JsonArray;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationTest {

    @ParameterizedTest
    @MethodSource("argsForValidationTest")
    public void testValidation(String patch) {
        assertThrows(
            InvalidJsonPatchException.class,
            () -> JsonPatch.validate(new JsonArray(patch))
        );
    }

    public static Stream<Arguments> argsForValidationTest() throws IOException {
        JsonArray patches = new JsonArray(TestUtils.loadFromResources("/testdata/invalid-patches.json"));

        List<Arguments> args = new ArrayList<>();

        for (Object patch: patches) {
            args.add(Arguments.of(patch.toString()));
        }

        return args.stream();
    }
}
