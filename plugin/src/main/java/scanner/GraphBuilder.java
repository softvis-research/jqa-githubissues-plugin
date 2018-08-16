package scanner;

import com.buschmais.jqassistant.core.store.api.Store;
import jdom.XMLGitHubRepository;
import json.*;
import lombok.AllArgsConstructor;
import toolbox.MarkdownParser;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import toolbox.RestTool;
import toolbox.StoreTool;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;

@AllArgsConstructor
class GraphBuilder {

    private final Logger LOGGER = LoggerFactory.getLogger(GraphBuilder.class);

    private Store store;
    private List<XMLGitHubRepository> xmlGitHubRepositories;
    private GitHub gitHub;

    void startTraversal() throws IOException {

        for (XMLGitHubRepository xmlGitHubRepository : xmlGitHubRepositories) {

            LOGGER.info("GitHub-Issues plugin searches in repository \""
                    + xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName() + "\".");


            GitHubRepository gitHubRepository = StoreTool.findOrCreateGitHubRepository(store, xmlGitHubRepository);

            repositoryLevel(store, gitHubRepository, xmlGitHubRepository);

            gitHub.getContains().add(gitHubRepository);
        }
    }

    private void repositoryLevel(Store store,
                                 GitHubRepository gitHubRepository,
                                 XMLGitHubRepository xmlGitHubRepository) throws IOException {

        // Load and iterate over milestones
        String response = RestTool.getInstance().requestMilestonesByRepository(gitHubRepository, xmlGitHubRepository);

        List<JSONMilestone> jsonMilestones = JSONParser.getInstance().parseMilestones(response);

        for (JSONMilestone jsonMilestone : jsonMilestones) {

            LOGGER.info("Importing milestone: \"" + jsonMilestone.getTitle() + "\"");

            GitHubMilestone gitHubMilestone = StoreTool.findOrCreateGitHubMilestone(store, jsonMilestone, xmlGitHubRepository);

            gitHubRepository.getMilestones().add(gitHubMilestone);
        }

        // Load and iterate over issues
        response = RestTool.
                getInstance().
                requestIssuesByRepository(gitHubRepository, xmlGitHubRepository);

        List<JSONIssue> jsonIssues = JSONParser.getInstance().parseIssues(response);


        for (JSONIssue jsonIssue : jsonIssues) {
            String id = xmlGitHubRepository.getUser() +
                    "/" + xmlGitHubRepository.getName() +
                    "#" +jsonIssue.getNumber();

            LOGGER.info("Importing issue: " + id + ", \"" + jsonIssue.getTitle() + "\"");

            GitHubIssue gitHubIssue = issueLevel(store, jsonIssue, xmlGitHubRepository);

            gitHubRepository.getContains().add(gitHubIssue);
        }
    }

    private GitHubIssue issueLevel(Store store,
                            JSONIssue jsonIssue,
                            XMLGitHubRepository xmlGitHubRepository) throws IOException {

        GitHubIssue gitHubIssue = StoreTool.findOrCreateGitHubIssue(store, jsonIssue, xmlGitHubRepository);

        // Find existing user or let the store create a new one
        gitHubIssue.setCreatedBy(StoreTool.findOrCreateGitHubUser(store, jsonIssue.getUser()));

        // Find existing milestone or let the store create a new one.
        // Issues don't need to have an associated milestone.
        if (jsonIssue.getMilestone() != null) {
            gitHubIssue.setMilestone(StoreTool.findOrCreateGitHubMilestone(store, jsonIssue.getMilestone(), xmlGitHubRepository));
        }

        /*
         Check if this issue is an pull-request.

         If it is one the JSONPullRequest contains (only!) an URL pointing at more information about the pull-request.
         Therefore, we need to make a call to the URL to get properties like the merge timestamp and the related
         commit-sha.
        */
        if (jsonIssue.getPullRequest() != null) {

            String response = RestTool.getInstance().requestAbsoluteUrl(
                    jsonIssue.getPullRequest().getUrl(),
                    xmlGitHubRepository);

            JSONIssue jsonPullRequest = JSONParser.getInstance().parsePullRequest(response);

            GitHubPullRequest gitHubPullRequest = (GitHubPullRequest) gitHubIssue;

            if (jsonPullRequest.getMergedAt() != null) {
                gitHubPullRequest.setMergedAt(ZonedDateTime.parse(jsonPullRequest.getMergedAt()));
            }

            GitHubCommit gitHubCommit = StoreTool.findOrCreateGitHubCommit(
                    store,
                    xmlGitHubRepository.getUser(),
                    xmlGitHubRepository.getName(),
                    jsonPullRequest.getMergeCommitSha());

            gitHubPullRequest.setLastCommit(gitHubCommit);
        }

        for (JSONUser jsonAssignee : jsonIssue.getAssignees()) {

            gitHubIssue.getAssignees().add(StoreTool.findOrCreateGitHubUser(store, jsonAssignee));
        }

        for (JSONLabel jsonLabel : jsonIssue.getLabels()) {

            gitHubIssue.getLabeles().add(StoreTool.findOrCreateGitHubLabel(store, jsonLabel));
        }

        MarkdownParser.getReferencesInMarkdown(store, gitHubIssue.getBody(), gitHubIssue, xmlGitHubRepository);

        commentLevel(store, gitHubIssue, xmlGitHubRepository);

        return gitHubIssue;
    }

    private void commentLevel(Store store,
                              GitHubIssue gitHubIssue,
                              XMLGitHubRepository xmlGitHubRepository) throws IOException {

        String response = RestTool.getInstance().requestCommentsByIssue(gitHubIssue, xmlGitHubRepository);

        List<JSONComment> jsonComments = JSONParser.getInstance().parseComments(response);

        GitHubComment last = null;
        for (JSONComment jsonComment : jsonComments) {

            GitHubComment comment = store.create(GitHubComment.class);
            comment.setBody(jsonComment.getBody());
            comment.setCreatedAt(ZonedDateTime.parse(jsonComment.getCreatedAt()));
            comment.setUpdatedAt(ZonedDateTime.parse(jsonComment.getUpdatedAt()));

            comment.setUser(StoreTool.findOrCreateGitHubUser(store, jsonComment.getUser()));

            MarkdownParser.getReferencesInMarkdown(store, comment.getBody(), comment, xmlGitHubRepository);

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