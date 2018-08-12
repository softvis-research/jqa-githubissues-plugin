package json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class JSONParser {

    private static JSONParser instance;
    private ObjectMapper objectMapper;

    private JSONParser() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static JSONParser getInstance() {

        if (instance == null) {
            instance = new JSONParser();
        }
        return instance;
    }

    public JSONIssue parsePullRequest(String json) throws IOException {

        return objectMapper.readValue(json, JSONIssue.class);
    }

    public List<JSONIssue> parseIssues(String json) throws IOException {

        return objectMapper.readValue(json, new TypeReference<List<JSONIssue>>() {
        });
    }

    public List<JSONComment> parseComments(String json) throws IOException {

        return objectMapper.readValue(json, new TypeReference<List<JSONComment>>() {
        });
    }

    public List<JSONMilestone> parseMilestones(String json) throws IOException {

        return objectMapper.readValue(json, new TypeReference<List<JSONMilestone>>() {
        });
    }

    public JSONIssue parseIssue(String json) throws IOException {

        return objectMapper.readValue(json, JSONIssue.class);
    }

    public String parseMarkdownRequest(JSONMarkdownRequest jsonMarkdownRequest) throws JsonProcessingException {

        return objectMapper.writeValueAsString(jsonMarkdownRequest);
    }
}
