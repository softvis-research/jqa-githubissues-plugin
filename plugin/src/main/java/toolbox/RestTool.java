package toolbox;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import jdom.XMLGitHubRepository;
import json.JSONMarkdownRequest;
import json.JSONParser;
import model.GitHubIssue;
import model.GitHubRepository;

import javax.ws.rs.core.MediaType;

public class RestTool {

    private static final String GITHUB_API = "https://api.github.com/";

    private static RestTool instance;
    private Client client;

    private RestTool() {

        client = Client.create();
    }

    public static RestTool getInstance() {

        if (instance == null) {
            instance = new RestTool();
        }

        return instance;
    }

    String requestIssueByRepositoryAndNumber(String repoUser,
                                             String repoName,
                                             String issueNumber,
                                             XMLGitHubRepository xmlGitHubRepository) {

        setAuthFilter(xmlGitHubRepository);

        WebResource issueWebResource = client.resource(
                GITHUB_API + "repos/" + repoUser + "/" +
                        repoName + "/issues/" + issueNumber);

        return issueWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);

    }

    public String requestIssuesByRepository(GitHubRepository gitHubRepository,
                                     XMLGitHubRepository xmlGitHubRepository) {

        setAuthFilter(xmlGitHubRepository);

        WebResource issuesWebResource = client.resource(
                GITHUB_API + "repos/" + gitHubRepository.getUser() + "/" +
                        gitHubRepository.getName() + "/issues?state=all");

        return issuesWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    public String requestCommentsByIssue(GitHubIssue gitHubIssueDescriptor,
                                  XMLGitHubRepository xmlGitHubRepository) {

        setAuthFilter(xmlGitHubRepository);

        WebResource commentsWebResource = client.resource(
                GITHUB_API + "repos/" + xmlGitHubRepository.getUser() + "/" +
                        xmlGitHubRepository.getName() + "/issues/" + gitHubIssueDescriptor.getNumber() + "/comments");

        return commentsWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    public String requestMilestonesByRepository(GitHubRepository gitHubRepository, XMLGitHubRepository xmlGitHubRepository) {

        setAuthFilter(xmlGitHubRepository);

        WebResource milestonesWebResource = client.resource(
                GITHUB_API + "repos/" + gitHubRepository.getUser() + "/" +
                        gitHubRepository.getName() + "/milestones?state=all");

        return milestonesWebResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    public String requestAbsoluteUrl(String url, XMLGitHubRepository xmlGitHubRepository) {

        setAuthFilter(xmlGitHubRepository);

        WebResource webResource = client.resource(url);

        return webResource.accept(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    String requestMarkdownToHtml(String markdown, XMLGitHubRepository xmlGitHubRepository) throws JsonProcessingException {

        setAuthFilter(xmlGitHubRepository);

        WebResource webResource = client.resource(GITHUB_API + "markdown");

        JSONMarkdownRequest jsonMarkdownRequest = new JSONMarkdownRequest(markdown, xmlGitHubRepository);

        String json = JSONParser.getInstance().parseMarkdownRequest(jsonMarkdownRequest);

        return webResource.accept(MediaType.APPLICATION_JSON_TYPE).post(String.class, json);
    }

    private void setAuthFilter(XMLGitHubRepository xmlGitHubRepository) {

        client.removeAllFilters();
        client.addFilter(new HTTPBasicAuthFilter(
                xmlGitHubRepository.getCredentials().getUser(),
                xmlGitHubRepository.getCredentials().getPassword()));
    }
}
