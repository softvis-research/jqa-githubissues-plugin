package org.jqassistant.contrib.plugin.githubissues.toolbox;

import com.buschmais.jqassistant.core.store.api.Store;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.json.*;
import org.jqassistant.contrib.plugin.githubissues.model.*;
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
public abstract class StoreTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreTool.class);

    /**
     * Check for {@link GitHubRepository}.
     *
     * @param store               The jQAssistant store instance to create a new node.
     * @param xmlGitHubRepository The GitHub repository information.
     * @return The retrieved or newly created descriptor instance.
     */
    public static GitHubRepository findOrCreateGitHubRepository(Store store, XMLGitHubRepository xmlGitHubRepository) {

        GitHubRepository repository = DescriptorCache.getInstance().get(xmlGitHubRepository);

        if (repository == null) {
            LOGGER.debug("Creating new repository: " + xmlGitHubRepository);

            repository = store.create(GitHubRepository.class);
            repository.setRepositoryId(xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName());
            repository.setName(xmlGitHubRepository.getName());
            repository.setUser(xmlGitHubRepository.getUser());

            DescriptorCache.getInstance().put(repository);
        }

        return repository;
    }

    /**
     * Check for {@link GitHubIssue}.
     * </p>
     * This method works a little bit different as it only has the ID of the Issue. If it can't find
     * the Issue in the {@link DescriptorCache} it will load the information from the GitHub-API using
     * the {@link RestTool}. After that it falls back to the common case:
     * {@link #findOrCreateGitHubIssue(Store, JSONIssue, XMLGitHubRepository)}.
     *
     * @param store               The jQAssistant store instance to create a new node.
     * @param repoUser            The owner of the repository.
     * @param repoName            The name of the repository.
     * @param issueNumber         The number of the issue.
     * @param xmlGitHubRepository The plugin configuration for the current repository.
     * @return The retrieved or newly created descriptor instance.
     * @throws IOException If the parsing of the issue JSON failed.
     */
    static GitHubIssue findOrCreateGitHubIssue(
            Store store,
            String repoUser,
            String repoName,
            String issueNumber,
            XMLGitHubRepository xmlGitHubRepository,
            RestTool restTool) throws IOException {

        GitHubIssue gitHubIssue = DescriptorCache.getInstance().getIssue(repoUser, repoName, issueNumber);

        if (gitHubIssue == null) {
            LOGGER.debug("Creating new issue: " + repoUser + "/" + repoName + "#" + issueNumber);
            String response = restTool.requestIssueByRepositoryAndNumber(
                    repoUser,
                    repoName,
                    issueNumber);

            JSONIssue jsonIssue = JSONParser.getInstance().parseIssue(response);
            gitHubIssue = findOrCreateGitHubIssue(store, jsonIssue, xmlGitHubRepository);
        }

        return gitHubIssue;
    }

    /**
     * Check for {@link GitHubIssue}.
     *
     * @param store               The jQAssistant store instance to create a new node.
     * @param jsonIssue           The GitHub issue information.
     * @param xmlGitHubRepository The GitHub repository information, needed to identify the issue.
     * @return The retrieved or newly created descriptor instance.
     */
    public static GitHubIssue findOrCreateGitHubIssue(
            Store store,
            JSONIssue jsonIssue,
            XMLGitHubRepository xmlGitHubRepository) {

        GitHubIssue gitHubIssue = DescriptorCache.getInstance().get(jsonIssue, xmlGitHubRepository);

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

            DescriptorCache.getInstance().put(gitHubIssue);
        }

        return gitHubIssue;
    }

    /**
     * Check for {@link GitHubUser}.
     *
     * @param store    The jQAssistant store instance to create a new node.
     * @param jsonUser The GitHub user information.
     * @return The retrieved or newly created descriptor instance.
     */
    public static GitHubUser findOrCreateGitHubUser(Store store, JSONUser jsonUser) {

        GitHubUser user = DescriptorCache.getInstance().get(jsonUser);

        if (user == null) {
            LOGGER.debug("Creating new user: " + jsonUser);
            user = store.create(GitHubUser.class);
            user.setLogin(jsonUser.getLogin());

            DescriptorCache.getInstance().put(user);
        }

        return user;
    }

    /**
     * Check for {@link GitHubLabel}.
     *
     * @param store     The jQAssistant store instance to create a new node.
     * @param jsonLabel The GitHub label information.
     * @return The retrieved or newly created descriptor instance.
     */
    public static GitHubLabel findOrCreateGitHubLabel(Store store, JSONLabel jsonLabel) {

        GitHubLabel label = DescriptorCache.getInstance().get(jsonLabel);

        if (label == null) {
            LOGGER.debug("Creating new label: " + jsonLabel);
            label = store.create(GitHubLabel.class);
            label.setName(jsonLabel.getName());
            label.setDescription(jsonLabel.getDescription());

            DescriptorCache.getInstance().put(label);
        }

        return label;
    }

    /**
     * Check for {@link GitHubMilestone}.
     *
     * @param store               The jQAssistant store instance to create a new node.
     * @param jsonMilestone       The GitHub milestone information.
     * @param xmlGitHubRepository The GitHub repository information, needed to identify the milestone.
     * @return The retrieved or newly created descriptor instance.
     */
    public static GitHubMilestone findOrCreateGitHubMilestone(Store store, JSONMilestone jsonMilestone, XMLGitHubRepository xmlGitHubRepository) {

        GitHubMilestone milestone = DescriptorCache.getInstance().get(jsonMilestone, xmlGitHubRepository);

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

            milestone.setCreatedBy(findOrCreateGitHubUser(store, jsonMilestone.getCreator()));

            DescriptorCache.getInstance().put(milestone);
        }

        return milestone;
    }

    /**
     * Check for {@link GitHubCommit}.
     *
     * @param store     The jQAssistant store instance to create a new node.
     * @param repoUser  The owner of the repository.
     * @param repoName  The name of the repository.
     * @param commitSha The hash of the commit.
     * @return The retrieved or newly created descriptor instance.
     */
    public static GitHubCommit findOrCreateGitHubCommit(Store store, String repoUser, String repoName, String commitSha) {

        GitHubCommit commit = DescriptorCache.getInstance().getCommit(repoUser, repoName, commitSha);

        if (commit == null) {

            LOGGER.debug("Creating new commit: " + commitSha);
            commit = store.create(GitHubCommit.class);
            commit.setId(repoUser + "/" + repoName + "#" + commitSha);
            commit.setSha(commitSha);

            DescriptorCache.getInstance().put(commit);
        }

        return commit;
    }
}
