package dev.auth.lib.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TestUtils {

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJsonFromFile(String file) throws IOException {
        File expectedFile = ResourceUtils.getFile("classpath:" + file);
        return new String(Files.readAllBytes(expectedFile.toPath()));
    }

    public static String getStringFromFile(String file) throws IOException {
        File expectedFile = ResourceUtils.getFile("classpath:" + file);
        return new String(Files.readAllBytes(expectedFile.toPath()));
    }
}
