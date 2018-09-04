package org.jqassistant.contrib.plugin.githubissues.toolbox.cache;

import com.buschmais.jqassistant.core.store.api.Store;
import org.jqassistant.contrib.plugin.githubissues.ids.*;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.json.*;
import org.jqassistant.contrib.plugin.githubissues.model.*;
import org.jqassistant.contrib.plugin.githubissues.toolbox.RestTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * This class was written to handle nodes in the resulting graph that get referenced more than one
 * time. These nodes get cached in the {@link DescriptorCache}.
 * </p>
 * All of the methods in this class work similar:
 * </p>
 * They check if a certain descriptor instance exists. If it does
 * exist, they return the instance. Otherwise they create a new one and save it in the
 * {@link Store} and in the {@link DescriptorCache}.
 */
public class CacheEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheEndpoint.class);

    private Store store;
    private DescriptorCache descriptorCache;

    public CacheEndpoint(Store store) {

        this.store = store;
        descriptorCache = new DescriptorCache();
    }

    /**
     * Check for {@link GitHubRepository}.
     *
     * @param xmlGitHubRepository The GitHub repository information.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubRepository findOrCreateGitHubRepository(XMLGitHubRepository xmlGitHubRepository) {

        RepositoryID repositoryID = RepositoryID.builder()
            .user(xmlGitHubRepository.getUser())
            .name(xmlGitHubRepository.getName())
            .build();

        GitHubRepository repository = descriptorCache.get(repositoryID);

        if (repository == null) {
            LOGGER.debug("Creating new repository: " + xmlGitHubRepository);

            repository = store.create(GitHubRepository.class);
            repository.setRepositoryId(xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName());
            repository.setName(xmlGitHubRepository.getName());
            repository.setUser(xmlGitHubRepository.getUser());

            descriptorCache.put(repository, repositoryID);
        }

        return repository;
    }

    /**
     * Check for {@link GitHubIssue}.
     * </p>
     * This method works a little bit different as it only has the ID of the Issue. If it can't find
     * the Issue in the {@link DescriptorCache} it will load the information from the GitHub-API using
     * the {@link RestTool}. After that it falls back to the common case:
     * {@link #findOrCreateGitHubIssue(JSONIssue, XMLGitHubRepository)}.
     *
     * @param repoUser            The owner of the repository.
     * @param repoName            The name of the repository.
     * @param issueNumber         The number of the issue.
     * @param xmlGitHubRepository The plugin configuration for the current repository.
     * @return The retrieved or newly created descriptor instance.
     * @throws IOException If the parsing of the issue JSON failed.
     */
    public GitHubIssue findOrCreateGitHubIssue(
        String repoUser,
        String repoName,
        int issueNumber,
        XMLGitHubRepository xmlGitHubRepository,
        RestTool restTool) throws IOException {

        GitHubIssue gitHubIssue = descriptorCache.get(
            IssueID.builder()
                .repoUser(repoUser)
                .repoName(repoName)
                .issueNumber(issueNumber)
                .build());

        if (gitHubIssue == null) {
            LOGGER.debug("Creating new issue: " + repoUser + "/" + repoName + "#" + issueNumber);
            JSONIssue jsonIssue = restTool.requestIssueByRepositoryAndNumber(
                repoUser,
                repoName,
                issueNumber);

            gitHubIssue = findOrCreateGitHubIssue(jsonIssue, xmlGitHubRepository);
        }

        return gitHubIssue;
    }

    /**
     * Check for {@link GitHubIssue}.
     *
     * @param jsonIssue           The GitHub issue information.
     * @param xmlGitHubRepository The GitHub repository information, needed to identify the issue.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubIssue findOrCreateGitHubIssue(JSONIssue jsonIssue, XMLGitHubRepository xmlGitHubRepository) {

        IssueID issueID = IssueID.builder()
            .repoUser(xmlGitHubRepository.getUser())
            .repoName(xmlGitHubRepository.getName())
            .issueNumber(jsonIssue.getNumber())
            .build();

        GitHubIssue gitHubIssue = descriptorCache.get(issueID);

        if (gitHubIssue == null) {
            LOGGER.debug("Creating new issue: " + jsonIssue);

            if (jsonIssue.getPullRequest() == null) {
                gitHubIssue = store.create(GitHubIssue.class);
            } else {
                gitHubIssue = store.create(GitHubPullRequest.class);
            }

            // Fill descriptor with information
            gitHubIssue.setIssueId(xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName() + "#" + jsonIssue.getNumber());
            gitHubIssue.setBody(jsonIssue.getBody());
            gitHubIssue.setComments(jsonIssue.getComments());
            gitHubIssue.setCreatedAt(ZonedDateTime.parse(jsonIssue.getCreatedAt()));
            gitHubIssue.setUpdatedAt(ZonedDateTime.parse(jsonIssue.getUpdatedAt()));
            gitHubIssue.setLocked(jsonIssue.isLocked());
            gitHubIssue.setNumber(jsonIssue.getNumber());
            gitHubIssue.setState(jsonIssue.getState());
            gitHubIssue.setTitle(jsonIssue.getTitle());

            descriptorCache.put(gitHubIssue, issueID);
        }

        return gitHubIssue;
    }

    /**
     * Check for {@link GitHubUser}.
     *
     * @param jsonUser The GitHub user information.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubUser findOrCreateGitHubUser(JSONUser jsonUser) {

        GitHubUser user = descriptorCache.get(
            UserID.builder()
                .login(jsonUser.getLogin())
                .build());

        if (user == null) {
            LOGGER.debug("Creating new user: " + jsonUser);
            user = store.create(GitHubUser.class);
            user.setLogin(jsonUser.getLogin());

            descriptorCache.put(user);
        }

        return user;
    }

    /**
     * Check for {@link GitHubLabel}.
     *
     * @param jsonLabel The GitHub label information.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubLabel findOrCreateGitHubLabel(JSONLabel jsonLabel) {

        GitHubLabel label = descriptorCache.get(
            LabelID.builder().name(jsonLabel.getName())
                .build());

        if (label == null) {
            LOGGER.debug("Creating new label: " + jsonLabel);
            label = store.create(GitHubLabel.class);
            label.setName(jsonLabel.getName());
            label.setDescription(jsonLabel.getDescription());

            descriptorCache.put(label);
        }

        return label;
    }

    /**
     * Check for {@link GitHubMilestone}.
     *
     * @param jsonMilestone       The GitHub milestone information.
     * @param xmlGitHubRepository The GitHub repository information, needed to identify the milestone.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubMilestone findOrCreateGitHubMilestone(JSONMilestone jsonMilestone, XMLGitHubRepository xmlGitHubRepository) {

        MilestoneID milestoneID = MilestoneID.builder()
            .repoUser(xmlGitHubRepository.getUser())
            .repoName(xmlGitHubRepository.getName())
            .milestoneNumber(jsonMilestone.getNumber())
            .build();

        GitHubMilestone milestone = descriptorCache.get(milestoneID);

        if (milestone == null) {
            LOGGER.debug("Creating new milestone: " + jsonMilestone);

            milestone = store.create(GitHubMilestone.class);
            milestone.setMilestoneId(xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName() + "#" + jsonMilestone.getNumber());
            milestone.setTitle(jsonMilestone.getTitle());
            milestone.setDescription(jsonMilestone.getDescription());
            milestone.setCreatedAt(ZonedDateTime.parse(jsonMilestone.getCreatedAt()));
            milestone.setUpdatedAt(ZonedDateTime.parse(jsonMilestone.getUpdatedAt()));

            if (jsonMilestone.getDueOn() != null) {
                milestone.setDueOn(ZonedDateTime.parse(jsonMilestone.getDueOn()));
            }

            milestone.setState(jsonMilestone.getState());
            milestone.setNumber(jsonMilestone.getNumber());

            milestone.setCreatedBy(findOrCreateGitHubUser(jsonMilestone.getCreator()));

            descriptorCache.put(milestone, milestoneID);
        }

        return milestone;
    }

    /**
     * Check for {@link GitHubCommit}.
     *
     * @param repoUser  The owner of the repository.
     * @param repoName  The name of the repository.
     * @param commitSha The hash of the commit.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubCommit findOrCreateGitHubCommit(String repoUser, String repoName, String commitSha) {

        CommitID commitID = CommitID.builder()
            .repoUser(repoUser)
            .repoName(repoName)
            .commitSha(commitSha)
            .build();

        GitHubCommit commit = descriptorCache.get(commitID);

        if (commit == null) {

            LOGGER.debug("Creating new commit: " + commitSha);
            commit = store.create(GitHubCommit.class);
            commit.setId(repoUser + "/" + repoName + "#" + commitSha);
            commit.setSha(commitSha);

            descriptorCache.put(commit, commitID);
        }

        return commit;
    }


    /**
     * Check for {@link GitHubComment}.
     *
     * @param jsonComment         The GitHub comment information.
     * @param xmlGitHubRepository The GitHub repository information, needed to identify the milestone.
     * @return The retrieved or newly created descriptor instance.
     */
    public GitHubComment findOrCreateGitHubComment(JSONComment jsonComment, XMLGitHubRepository xmlGitHubRepository) {

        CommentID commentID = CommentID.builder()
            .repoUser(xmlGitHubRepository.getUser())
            .repoName(xmlGitHubRepository.getName())
            .commentId(jsonComment.getId())
            .build();

        GitHubComment comment = descriptorCache.get(commentID);

        if (comment == null) {
            LOGGER.debug("Creating new comment: " + jsonComment);

            comment = store.create(GitHubComment.class);
            comment.setCommentId(jsonComment.getId());
            comment.setBody(jsonComment.getBody());
            comment.setCreatedAt(ZonedDateTime.parse(jsonComment.getCreatedAt()));
            comment.setUpdatedAt(ZonedDateTime.parse(jsonComment.getUpdatedAt()));

            comment.setUser(findOrCreateGitHubUser(jsonComment.getUser()));

            descriptorCache.put(comment, commentID);
        }

        return comment;
    }
}
