package org.jqassistant.contrib.plugin.githubissues.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * The {@link JSONParser} class contains methods for every json response from the GitHub API that is
 * used in the GitHub-Issues plugin.
 * <p>
 * Furthermore, there is one function {@link #parseMarkdownRequest(JSONMarkdownRequest)} which does reverse parsing.
 * It is needed to post markdown requests to resolve references in the issue and comment bodies.
 */
public class JSONParser {

    private static JSONParser instance;
    private ObjectMapper objectMapper;

    private JSONParser() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * <p>
     * Json Parser is a singleton to avoid creating a {@link ObjectMapper} instance multiple times.
     * </p>
     * Therefore, it needs a getInstance() method to retrieve the singleton instance.
     *
     * @return The singleton instance.
     */
    public static JSONParser getInstance() {

        if (instance == null) {
            instance = new JSONParser();
        }
        return instance;
    }

    /**
     * Parses a JSON-String to an instance of a class in {@link org.jqassistant.contrib.plugin.githubissues.json}.
     *
     * @param json The JSON-string.
     * @return The requested POJO.
     * @throws IOException If parsing failed.
     */
    public JSONIssue parsePullRequest(String json) throws IOException {

        return objectMapper.readValue(json, JSONIssue.class);
    }

    /**
     * Parses a JSON-String to an instance of a class in {@link org.jqassistant.contrib.plugin.githubissues.json}.
     *
     * @param json The JSON-string.
     * @return The requested POJO.
     * @throws IOException If parsing failed.
     */
    public List<JSONIssue> parseIssues(String json) throws IOException {

        return objectMapper.readValue(json, new TypeReference<List<JSONIssue>>() {
        });
    }

    /**
     * Parses a JSON-String to an instance of a class in {@link org.jqassistant.contrib.plugin.githubissues.json}.
     *
     * @param json The JSON-string.
     * @return The requested POJO.
     * @throws IOException If parsing failed.
     */
    public List<JSONComment> parseComments(String json) throws IOException {

        return objectMapper.readValue(json, new TypeReference<List<JSONComment>>() {
        });
    }

    /**
     * Parses a JSON-String to an instance of a class in {@link org.jqassistant.contrib.plugin.githubissues.json}.
     *
     * @param json The JSON-string.
     * @return The requested POJO.
     * @throws IOException If parsing failed.
     */
    public List<JSONMilestone> parseMilestones(String json) throws IOException {

        return objectMapper.readValue(json, new TypeReference<List<JSONMilestone>>() {
        });
    }

    /**
     * Parses a JSON-String to an instance of a class in {@link org.jqassistant.contrib.plugin.githubissues.json}.
     *
     * @param json The JSON-string.
     * @return The requested POJO.
     * @throws IOException If parsing failed.
     */
    public JSONIssue parseIssue(String json) throws IOException {

        return objectMapper.readValue(json, JSONIssue.class);
    }

    /**
     * Parses a {@link JSONMarkdownRequest} to a JSON-string..
     *
     * @param jsonMarkdownRequest The object to be parsed.
     * @return A JSON-string representing the markdown request.
     * @throws JsonProcessingException If JSON processing fails.
     */
    public String parseMarkdownRequest(JSONMarkdownRequest jsonMarkdownRequest) throws JsonProcessingException {

        return objectMapper.writeValueAsString(jsonMarkdownRequest);
    }
}
