package org.jqassistant.contrib.plugin.githubissues.toolbox;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.json.*;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubIssue;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class provides methods to retrieve information from the REST API of GitHub.
 * <p>
 * Every method is well documented and contains a link to the REST API documentation.
 * <p>
 * For every configured repository one instance of this class will be created.
 */
public class RestTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTool.class);

    private String apiUrl;
    private XMLGitHubRepository xmlGitHubRepository;
    private Client client;

    public RestTool(String apiUrl, XMLGitHubRepository xmlGitHubRepository) {

        this.apiUrl = apiUrl;
        client = Client.create();

        client.removeAllFilters();
        client.addFilter(new HTTPBasicAuthFilter(
            xmlGitHubRepository.getCredentials().getUser(),
            xmlGitHubRepository.getCredentials().getPassword()));

        this.xmlGitHubRepository = xmlGitHubRepository;
    }

    /**
     * Every request that returns a failure needs to be catched and logged.
     *
     * @param webResource The request web resource.
     * @param response    The response that shall be checked.
     * @return The entity of the response if no error occurs, null otherwise.
     * @throws RequestFailedException If a request error occurs.
     */
    private String raiseErrorsIfNeeded(WebResource webResource, ClientResponse response) throws RequestFailedException {

        if (response.getStatus() == 200) {
            return response.getEntity(String.class);
        }

        StringBuilder headers = new StringBuilder();

        // Build HEADERS string:
        for (Map.Entry<String, List<String>> entry : response.getHeaders().entrySet()) {

            headers.append("  ");
            headers.append(entry.getKey());
            headers.append(System.lineSeparator());

            for (String v : entry.getValue()) {
                headers.append("  - ");
                headers.append(v);
                headers.append(System.lineSeparator());
            }

            headers.append(System.lineSeparator());
        }

        throw new RequestFailedException("REST request failed:\n\n" +
            "Status: " + response.getStatus() + "\n" +
            "Headers: \n\n" +
            headers.toString() + "\n\n" +
            "Entity: \n" +
            response.getEntity(String.class) + "\n" +
            "Request: \n" +
            "> URL: \"" + webResource.getURI().toString() + "\"\n");
    }

    /**
     * Adds a POST entity to the logging message. See {@link #raiseErrorsIfNeeded(WebResource, ClientResponse)} for
     * more information.
     *
     * @param webResource The request web resource.
     * @param response    The response that shall be checked.
     * @param entity      A POST request payload.
     * @return The entity of the response if no error occurs, null otherwise.
     * @throws RequestFailedException If a request error occurs.
     */
    private String raiseErrorsIfNeeded(WebResource webResource, ClientResponse response, String entity) throws RequestFailedException {

        try {
            return raiseErrorsIfNeeded(webResource, response);
        } catch (RequestFailedException e) {
            throw new RequestFailedException(e.getMessage() +
                "> Entity:" +
                "\n----------------------\n" +
                entity +
                "\n----------------------\n");
        }
    }


    /**
     * Requests one specific issue.
     *
     * @param repoUser    The owner of the repository.
     * @param repoName    The name of the repository.
     * @param issueNumber The number of the issue.
     * @return The response as JSON-POJO or null if the request failed.
     * @throws IOException            If parsing the response fails.
     * @throws RequestFailedException If the request fails.
     * @see <a href="https://developer.github.com/v3/issues/#get-a-single-issue">REST-API</a>
     */
    public JSONIssue requestIssueByRepositoryAndNumber(String repoUser,
                                                       String repoName,
                                                       int issueNumber) throws IOException, RequestFailedException {

        WebResource issueWebResource = client.resource(
            apiUrl + "repos/" + repoUser + "/" +
                repoName + "/issues/" + issueNumber);

        String response = raiseErrorsIfNeeded(issueWebResource, issueWebResource
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .get(ClientResponse.class));

        return JSONParser.getInstance().parseIssue(response);
    }

    /**
     * Requests all issues related to a specific repository.
     *
     * @param gitHubRepository The repository descriptor for which the issues shall be retrieved.
     * @return The response as JSON-POJOs.
     * @throws IOException If parsing the response fails.
     * @see <a href="https://developer.github.com/v3/issues/#list-issues-for-a-repository">REST-API</a>
     */
    public List<JSONIssue> requestIssuesByRepository(GitHubRepository gitHubRepository) throws IOException {

        WebResource issuesWebResource = client.resource(
            apiUrl + "repos/" + gitHubRepository.getUser() + "/" +
                gitHubRepository.getName() + "/issues?state=all&per_page=100");

        List<String> jsonArrays = retrieveAllPages(issuesWebResource);
        List<JSONIssue> jsonIssues = new ArrayList<>();

        for (String jsonArray : jsonArrays) {
            jsonIssues.addAll(JSONParser.getInstance().parseIssues(jsonArray));
        }

        return jsonIssues;
    }

    /**
     * Resolves the pagination function from the GitHub-REST API.
     * <p>
     * First, the original webResource gets resolved. After that, it looks for "Link" headers and follows them if they
     * are set.
     * <p>
     * The result is an array of JSON arrays.
     *
     * @param webResource The initial web resource.
     * @return An array of JSON arrays.
     */
    private List<String> retrieveAllPages(WebResource webResource) {


        List<String> jsonStrings = new ArrayList<>();

        ClientResponse clientResponse;
        String nextPaginationUrl = null;

        do {

            // Override the next URL everytime after the first:
            if (nextPaginationUrl != null) {
                webResource = client.resource(nextPaginationUrl);

                LOGGER.info("Requesting next page: \"" + nextPaginationUrl + "\"");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LOGGER.error("Interrupted Exception: ", e);
                }
            }

            clientResponse = webResource.get(ClientResponse.class);

            String entity;
            try {

                entity = raiseErrorsIfNeeded(webResource, clientResponse);
            } catch (RequestFailedException e) {

                // If an error occurs:
                LOGGER.warn("Pagination REST failure:", e);
                return jsonStrings;
            }

            nextPaginationUrl = getNextPaginationUrl(clientResponse);
            jsonStrings.add(entity);

        } while (nextPaginationUrl != null);

        return jsonStrings;
    }

    /**
     * Extracts the "next" URL from a "Link" header.
     * </p>
     * e.g.
     * <p>
     * Status: 200 OK
     * Link: <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=2>; rel="next",
     * <https://api.github.com/search/code?q=addClass+user%3Amozilla&page=34>; rel="last"
     *
     * @param clientResponse The ClientResponse which is used to extract the URL.
     * @return The URL as String or null if no URL is returned by the server.
     */
    private String getNextPaginationUrl(ClientResponse clientResponse) {

        for (Map.Entry<String, List<String>> header : clientResponse.getHeaders().entrySet()) {

            /*
            The better solution clientResponse.getLinks() doesn't work.

            Leads to:

            java.lang.IllegalArgumentException: java.text.ParseException: Expected separator ';' instead of ','
                at com.sun.jersey.core.header.LinkHeader.valueOf(LinkHeader.java:106)
                at com.sun.jersey.core.header.LinkHeaders.<init>(LinkHeaders.java:62)
                at com.sun.jersey.api.client.WebResourceLinkHeaders.<init>(WebResourceLinkHeaders.java:55)
                at com.sun.jersey.api.client.ClientResponse.getLinks(ClientResponse.java:813)

            Therefore a custom parser must be written:
             */
            if (header.getKey().equals("Link")) {
                for (String s : header.getValue()) {
                    if (s.contains("rel=\"next\"")) {

                        String temp = s;

                        // Cut the rest of the link:
                        temp = temp.substring(0, temp.indexOf("rel=\"next\""));

                        return temp.substring(temp.lastIndexOf('<') + 1, temp.lastIndexOf('>'));
                    }
                }
            }
        }

        return null;
    }

    /**
     * Requests all comments related to a specific issue.
     *
     * @param gitHubIssueDescriptor The issue descriptor for which the comments shall be retrieved.
     * @return The response as JSON-POJOs.
     * @throws IOException If parsing the response fails.
     * @see <a href="https://developer.github.com/v3/issues/comments/#list-comments-on-an-issue">REST-API</a>
     */
    public List<JSONComment> requestCommentsByIssue(GitHubIssue gitHubIssueDescriptor) throws IOException {

        WebResource commentsWebResource = client.resource(
            apiUrl + "repos/" + xmlGitHubRepository.getUser() + "/" +
                xmlGitHubRepository.getName() + "/issues/" + gitHubIssueDescriptor.getNumber() + "/comments?per_page=100");

        List<String> jsonArrays = retrieveAllPages(commentsWebResource);
        List<JSONComment> jsonComments = new ArrayList<>();

        for (String jsonArray : jsonArrays) {
            jsonComments.addAll(JSONParser.getInstance().parseComments(jsonArray));
        }

        return jsonComments;
    }

    /**
     * Requests all Milestones related to a specific repository.
     *
     * @param gitHubRepository The repository descriptor for which the milestones shall be
     *                         retrieved.
     * @return The response as JSON-POJOs.
     * @throws IOException If parsing the response fails.
     * @see <a href="https://developer.github.com/v3/issues/milestones/#list-milestones-for-a-repository">REST-API</a>
     */
    public List<JSONMilestone> requestMilestonesByRepository(GitHubRepository gitHubRepository) throws IOException {

        WebResource milestonesWebResource = client.resource(
            apiUrl + "repos/" + gitHubRepository.getUser() + "/" +
                gitHubRepository.getName() + "/milestones?state=all&per_page=100");

        List<String> jsonArrays = retrieveAllPages(milestonesWebResource);
        List<JSONMilestone> jsonMilestones = new ArrayList<>();

        for (String jsonArray : jsonArrays) {
            jsonMilestones.addAll(JSONParser.getInstance().parseMilestones(jsonArray));
        }

        return jsonMilestones;
    }

    /**
     * Requests JSON from an absolute URL. This is used for issues that are also pull requests.
     * They contain a <b>pull_request</b> property which is an url pointing at more information
     * about this pull request.
     * <p>
     *
     * @param url An absolute url as String.
     * @return The response as JSON-POJO or null if the request failed.
     * @throws RequestFailedException If the request fails.
     * @throws IOException            If parsing the response fails.
     * @see <a href="https://developer.github.com/v3/pulls/#get-a-single-pull-request">REST-API</a>
     */
    public JSONIssue requestPullRequestByAbsoluteUrl(String url) throws IOException, RequestFailedException {

        WebResource webResource = client.resource(url);

        String entity = raiseErrorsIfNeeded(webResource,
            webResource.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class));

        return JSONParser.getInstance().parsePullRequest(entity);
    }

    /**
     * GitHub provides an endpoint to convert markdown to HTML.
     *
     * <p>This is especially useful to retrieve references from an issue or a comment body to
     * <ul>
     * <li>commits,</li>
     * <li>users,</li>
     * <li>or issues.</li>
     * </ul>
     *
     * @param markdown The markdown that shall be converted.
     * @return The response as HTML-String or an empty String if the request failed.
     * @throws JsonProcessingException If the org.jqassistant.contrib.plugin.githubissues.json creation for the request payload fails.
     * @see <a href="https://developer.github.com/v3/markdown/#render-an-arbitrary-markdown-document">REST-API</a>
     */
    String requestMarkdownToHtml(String markdown) throws JsonProcessingException, RequestFailedException {

        WebResource webResource = client.resource(apiUrl + "markdown");

        JSONMarkdownRequest jsonMarkdownRequest = new JSONMarkdownRequest(markdown, xmlGitHubRepository);

        String json = JSONParser.getInstance().parseMarkdownRequest(jsonMarkdownRequest);

        return raiseErrorsIfNeeded(
            webResource,
            webResource.accept(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class, json),
            json);
    }
}
