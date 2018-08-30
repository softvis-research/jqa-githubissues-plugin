package org.jqassistant.contrib.plugin.githubissues.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.json.*;
import org.jqassistant.contrib.plugin.githubissues.model.*;
import org.jqassistant.contrib.plugin.githubissues.toolbox.MarkdownParser;
import org.jqassistant.contrib.plugin.githubissues.toolbox.RestTool;
import org.jqassistant.contrib.plugin.githubissues.toolbox.cache.CacheEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * The GraphBuilder gets build only once per execution of the GitHub-Issues plugin
 * {@link GitHubIssueScannerPlugin#scan(FileResource, String, Scope, Scanner)} method.
 * <p>
 * It takes a list of specified repositories and starts to analyze them by following url paths.
 * <p>
 * The different tree depths are represented by the corresponding methods.
 */
class GraphBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphBuilder.class);

    private String apiUrl;
    private CacheEndpoint cacheEndpoint;
    private MarkdownParser markdownParser;

    GraphBuilder(String apiUrl, CacheEndpoint cacheEndpoint) {

        this.apiUrl = apiUrl;
        this.cacheEndpoint = cacheEndpoint;

        markdownParser = new MarkdownParser(cacheEndpoint);
    }

    /**
     * The entry point and the only method accessible from outside.
     * <p>
     * It takes a list of repository specifications and the root descriptor build by the
     * {@link GitHubIssueScannerPlugin}.
     *
     * @param gitHubIssuesConfigurationFile The root descriptor which is also the root of the tree.
     * @param xmlGitHubRepositories         The list of repositories specified in the configuration file.
     * @throws IOException If parsing fails.
     */
    void startTraversal(GitHubIssuesConfigurationFile gitHubIssuesConfigurationFile,
                        List<XMLGitHubRepository> xmlGitHubRepositories) throws IOException {

        for (XMLGitHubRepository xmlGitHubRepository : xmlGitHubRepositories) {

            LOGGER.info("GitHub-Issues plugin searches in repository \""
                + xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName() + "\".");


            GitHubRepository gitHubRepository = cacheEndpoint.findOrCreateGitHubRepository(xmlGitHubRepository);

            repositoryLevel(gitHubRepository, xmlGitHubRepository);

            gitHubIssuesConfigurationFile.getRepositories().add(gitHubRepository);
        }
    }

    /**
     * The method gets invoked once for every repository in a configuration file.
     *
     * @param gitHubRepository    The descriptor for the current repository.
     * @param xmlGitHubRepository The xml configuration for the current repository.
     * @throws IOException If parsing fails.
     */
    private void repositoryLevel(GitHubRepository gitHubRepository,
                                 XMLGitHubRepository xmlGitHubRepository) throws IOException {

        RestTool restTool = new RestTool(apiUrl, xmlGitHubRepository);

        // Load and iterate over milestones
        String response = restTool.requestMilestonesByRepository(gitHubRepository);

        List<JSONMilestone> jsonMilestones = JSONParser.getInstance().parseMilestones(response);

        for (JSONMilestone jsonMilestone : jsonMilestones) {

            LOGGER.info("Importing milestone: \"" + jsonMilestone.getTitle() + "\"");

            GitHubMilestone gitHubMilestone = cacheEndpoint.findOrCreateGitHubMilestone(jsonMilestone, xmlGitHubRepository);

            gitHubRepository.getMilestones().add(gitHubMilestone);
        }

        // Load and iterate over issues
        response = restTool.requestIssuesByRepository(gitHubRepository);

        List<JSONIssue> jsonIssues = JSONParser.getInstance().parseIssues(response);


        for (JSONIssue jsonIssue : jsonIssues) {
            String id = xmlGitHubRepository.getUser() +
                "/" + xmlGitHubRepository.getName() +
                "#" + jsonIssue.getNumber();

            LOGGER.info("Importing issue: " + id + ", \"" + jsonIssue.getTitle() + "\"");

            GitHubIssue gitHubIssue = issueLevel(jsonIssue, xmlGitHubRepository, restTool);

            gitHubRepository.getContains().add(gitHubIssue);
        }
    }


    /**
     * This method gets invoked once for every issue in a repository.
     *
     * @param jsonIssue           The issue as JSON POJO.
     * @param xmlGitHubRepository The xml configuration for the current repository.
     * @param restTool            The RestTool which will be used to request further details.
     * @throws IOException If parsing fails.
     */
    private GitHubIssue issueLevel(JSONIssue jsonIssue,
                                   XMLGitHubRepository xmlGitHubRepository,
                                   RestTool restTool) throws IOException {

        GitHubIssue gitHubIssue = cacheEndpoint.findOrCreateGitHubIssue(jsonIssue, xmlGitHubRepository);

        // Find existing user or let the store create a new one
        gitHubIssue.setCreatedBy(cacheEndpoint.findOrCreateGitHubUser(jsonIssue.getUser()));

        // Find existing milestone or let the store create a new one.
        // Issues don't need to have an associated milestone.
        if (jsonIssue.getMilestone() != null) {
            gitHubIssue.setMilestone(cacheEndpoint.findOrCreateGitHubMilestone(jsonIssue.getMilestone(), xmlGitHubRepository));
        }

        /*
         Check if this issue is an pull-request.

         If it is one the JSONPullRequest contains (only!) an URL pointing at more information about the pull-request.
         Therefore, we need to make a call to the URL to get properties like the merge timestamp and the related
         commit-sha.
        */
        if (jsonIssue.getPullRequest() != null) {

            String response = restTool.requestPullRequestByAbsoluteUrl(
                jsonIssue.getPullRequest().getUrl());

            JSONIssue jsonPullRequest = JSONParser.getInstance().parsePullRequest(response);

            GitHubPullRequest gitHubPullRequest = (GitHubPullRequest) gitHubIssue;

            if (jsonPullRequest.getMergedAt() != null) {
                gitHubPullRequest.setMergedAt(ZonedDateTime.parse(jsonPullRequest.getMergedAt()));
            }

            GitHubCommit gitHubCommit = cacheEndpoint.findOrCreateGitHubCommit(
                xmlGitHubRepository.getUser(),
                xmlGitHubRepository.getName(),
                jsonPullRequest.getMergeCommitSha());

            gitHubPullRequest.setLastCommit(gitHubCommit);
        }

        for (JSONUser jsonAssignee : jsonIssue.getAssignees()) {

            gitHubIssue.getAssignees().add(cacheEndpoint.findOrCreateGitHubUser(jsonAssignee));
        }

        for (JSONLabel jsonLabel : jsonIssue.getLabels()) {

            gitHubIssue.getLabeles().add(cacheEndpoint.findOrCreateGitHubLabel(jsonLabel));
        }

        markdownParser.getReferencesInMarkdown(gitHubIssue.getBody(), gitHubIssue, xmlGitHubRepository, restTool);

        commentLevel(gitHubIssue, xmlGitHubRepository, restTool);

        return gitHubIssue;
    }

    /**
     * Every issue can contain one or more comments.
     *
     * @param gitHubIssue         The issue as descriptor.
     * @param xmlGitHubRepository The xml configuration for the current repository.
     * @param restTool            The RestTool which will be used to request further details.
     * @throws IOException If parsing fails.
     */
    private void commentLevel(GitHubIssue gitHubIssue,
                              XMLGitHubRepository xmlGitHubRepository,
                              RestTool restTool) throws IOException {

        String response = restTool.requestCommentsByIssue(gitHubIssue);

        List<JSONComment> jsonComments = JSONParser.getInstance().parseComments(response);

        GitHubComment last = null;
        for (JSONComment jsonComment : jsonComments) {

            GitHubComment comment = cacheEndpoint.findOrCreateGitHubComment(jsonComment, xmlGitHubRepository);

            markdownParser.getReferencesInMarkdown(comment.getBody(), comment, xmlGitHubRepository, restTool);

            // Create a list of comments, each pointing at the next one.
            if (last == null) {
                gitHubIssue.getCommented().add(comment);
            } else {
                last.setComment(comment);
            }
            last = comment;
        }
    }
}
