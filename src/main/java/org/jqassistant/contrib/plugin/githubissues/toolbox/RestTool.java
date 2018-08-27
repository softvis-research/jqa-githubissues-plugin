package org.jqassistant.contrib.plugin.githubissues.toolbox;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.json.JSONMarkdownRequest;
import org.jqassistant.contrib.plugin.githubissues.json.JSONParser;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubIssue;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubRepository;

import javax.ws.rs.core.MediaType;

public class RestTool {

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
     * Requests one specific issue.
     *
     * @param repoUser    The owner of the repository.
     * @param repoName    The name of the repository.
     * @param issueNumber The number of the issue.
     * @return The response as JSON-String.
     * @see <a href="https://developer.github.com/v3/issues/#get-a-single-issue">REST-API</a>
     */
    public String requestIssueByRepositoryAndNumber(String repoUser,
                                             String repoName,
                                             String issueNumber) {

        WebResource issueWebResource = client.resource(
                apiUrl + "repos/" + repoUser + "/" +
                        repoName + "/issues/" + issueNumber);

        return issueWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);

    }

    /**
     * Requests all issues related to a specific repository.
     *
     * @param gitHubRepository The repository descriptor for which the issues shall be retrieved.
     * @return The response as JSON-String.
     * @see <a href="https://developer.github.com/v3/issues/#list-issues-for-a-repository">REST-API</a>
     */
    public String requestIssuesByRepository(GitHubRepository gitHubRepository) {

        WebResource issuesWebResource = client.resource(
                apiUrl + "repos/" + gitHubRepository.getUser() + "/" +
                        gitHubRepository.getName() + "/issues?state=all");

        return issuesWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    /**
     * Requests all comments related to a specific issue.
     *
     * @param gitHubIssueDescriptor The issue descriptor for which the comments shall be retrieved.
     * @return The response as JSON-String.
     * @see <a href="https://developer.github.com/v3/issues/comments/#list-comments-on-an-issue">REST-API</a>
     */
    public String requestCommentsByIssue(GitHubIssue gitHubIssueDescriptor) {

        WebResource commentsWebResource = client.resource(
                apiUrl + "repos/" + xmlGitHubRepository.getUser() + "/" +
                        xmlGitHubRepository.getName() + "/issues/" + gitHubIssueDescriptor.getNumber() + "/comments");

        return commentsWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    /**
     * Requests all Milestones related to a specific repository.
     *
     * @param gitHubRepository The repository descriptor for which the milestones shall be
     *                         retrieved.
     * @return The response as JSON-String.
     * @see <a href="https://developer.github.com/v3/issues/milestones/#list-milestones-for-a-repository">REST-API</a>
     */
    public String requestMilestonesByRepository(GitHubRepository gitHubRepository) {

        WebResource milestonesWebResource = client.resource(
                apiUrl + "repos/" + gitHubRepository.getUser() + "/" +
                        gitHubRepository.getName() + "/milestones?state=all");

        return milestonesWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    /**
     * Requests JSON from an absolute URL. This is used for issues that are also pull requests.
     * They contain a <b>pull_request</b> property which is an url pointing at more information
     * about this pull request.
     *
     * @param url An absolute url as String.
     * @return The response as JSON-String.
     * @see <a href="https://developer.github.com/v3/pulls/#get-a-single-pull-request">REST-API</a>
     */
    public String requestPullRequestByAbsoluteUrl(String url) {

        WebResource webResource = client.resource(url);

        return webResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
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
     * @return The response as HTML-String.
     * @throws JsonProcessingException If the org.jqassistant.contrib.plugin.githubissues.json creation for the request payload fails.
     * @see <a href="https://developer.github.com/v3/markdown/#render-an-arbitrary-markdown-document">REST-API</a>
     */
    String requestMarkdownToHtml(String markdown) throws JsonProcessingException {

        WebResource webResource = client.resource(apiUrl + "markdown");

        JSONMarkdownRequest jsonMarkdownRequest = new JSONMarkdownRequest(markdown, xmlGitHubRepository);

        String json = JSONParser.getInstance().parseMarkdownRequest(jsonMarkdownRequest);

        return webResource.accept(MediaType.APPLICATION_JSON_TYPE).post(String.class, json);
    }
}
