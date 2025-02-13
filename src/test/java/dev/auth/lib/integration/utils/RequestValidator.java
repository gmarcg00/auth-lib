package dev.auth.lib.integration.utils;

import lombok.AllArgsConstructor;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@AllArgsConstructor
public class RequestValidator {

    private MockMvc mvc;

    public void validateGetRequest(String url, ResultMatcher matcher, String fileExpected) throws Exception {
        File expectedFile = ResourceUtils.getFile("classpath:" + fileExpected);
        String expectedJson = new String(Files.readAllBytes(expectedFile.toPath()));
        String actualJson = this.mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.STRICT);
    }

    public void validateGetRequestHtml(String url, ResultMatcher matcher, String fileExpected) throws Exception {
        File expectedFile = ResourceUtils.getFile("classpath:" + fileExpected);
        String expectedJson = new String(Files.readAllBytes(expectedFile.toPath()));
        String actualJson = this.mvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);

        assertEquals(expectedJson, actualJson);
    }

    public void validatePostRequest(String url, ResultMatcher matcher, String fileInput, String fileExpected) throws Exception {
        String requestBody = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileInput).toPath()));
        String expectedJson = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileExpected).toPath()));
        String actualJson = this.mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);
        if (!expectedJson.trim().isEmpty()) {
            JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
        }
    }

    public void validatePutRequest(String url, ResultMatcher matcher, String fileInput, String fileExpected) throws Exception {
        String requestBody = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileInput).toPath()));
        String expectedJson = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileExpected).toPath()));
        String actualJson = this.mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    public void validatePatchRequest(String url, ResultMatcher matcher, String fileInput, String fileExpected) throws Exception {
        String requestBody = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileInput).toPath()));
        String expectedJson = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileExpected).toPath()));
        String actualJson = this.mvc.perform(patch(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    public void validateDeleteRequest(String url, ResultMatcher matcher) throws Exception {
        this.mvc.perform(delete(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher);
    }

    public void validateCustomPostRequest(String url, ResultMatcher matcher, String fileInput, String fileExpected, String[] field) throws Exception {
        String requestBody = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileInput).toPath()));
        String expectedJson = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileExpected).toPath()));
        String actualJson = this.mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);
        Customization[] customs = Arrays.stream(field).map(f -> new Customization(f, (o1, o2) -> true)).toList().toArray(new Customization[0]);
        JSONAssert.assertEquals(expectedJson, actualJson, new CustomComparator(JSONCompareMode.NON_EXTENSIBLE, customs));
    }

    public void validateCustomPutRequest(String url, ResultMatcher matcher, String fileInput, String fileExpected, String[] field) throws Exception{
        String requestBody = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileInput).toPath()));
        String expectedJson = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileExpected).toPath()));
        String actualJson = this.mvc.perform(put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);
        Customization[] customs = Arrays.stream(field).map(f -> new Customization(f, (o1, o2) -> true)).toList().toArray(new Customization[0]);
        JSONAssert.assertEquals(expectedJson, actualJson, new CustomComparator(JSONCompareMode.NON_EXTENSIBLE, customs));
    }

    public void validatePostRequestWithoutInputFile(String url, ResultMatcher matcher, String fileExpected) throws Exception {
        String expectedJson = new String(Files.readAllBytes(ResourceUtils.getFile("classpath:" + fileExpected).toPath()));
        String actualJson = this.mvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(matcher)
                .andReturn().getResponse().getContentAsString();
        System.out.println("OUTPUT: " + actualJson);
        JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
    }
}
