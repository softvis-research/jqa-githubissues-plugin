package toolbox;

import com.buschmais.jqassistant.core.store.api.Store;
import jdom.XMLGitHubRepository;
import json.*;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public abstract class StoreTool {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreTool.class);

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

    static GitHubIssue findOrCreateGitHubIssue(
            Store store,
            String repoUser,
            String repoName,
            String issueNumber,
            XMLGitHubRepository xmlGitHubRepository) throws IOException {

        GitHubIssue gitHubIssue = DescriptorCache.getInstance().getIssue(repoUser, repoName, issueNumber);

        if (gitHubIssue == null) {
            LOGGER.info("Creating new issue: " + repoUser + "/" + repoName + "#" + issueNumber);
            String response = RestTool.getInstance().requestIssueByRepositoryAndNumber(
                    repoUser,
                    repoName,
                    issueNumber,
                    xmlGitHubRepository);

            JSONIssue jsonIssue = JSONParser.getInstance().parseIssue(response);
            gitHubIssue = findOrCreateGitHubIssue(store, jsonIssue, xmlGitHubRepository);
        }

        return gitHubIssue;
    }

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
            gitHubIssue.setCreatedAt(jsonIssue.getCreatedAt());
            gitHubIssue.setUpdatedAt(jsonIssue.getUpdatedAt());
            gitHubIssue.setLocked(jsonIssue.isLocked());
            gitHubIssue.setNumber(jsonIssue.getNumber());
            gitHubIssue.setState(jsonIssue.getState());
            gitHubIssue.setTitle(jsonIssue.getTitle());

            DescriptorCache.getInstance().put(gitHubIssue);
        }

        return gitHubIssue;
    }

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

    public static GitHubMilestone findOrCreateGitHubMilestone(Store store, JSONMilestone jsonMilestone, XMLGitHubRepository xmlGitHubRepository) {

        GitHubMilestone milestone = DescriptorCache.getInstance().get(jsonMilestone, xmlGitHubRepository);

        if (milestone == null) {
            LOGGER.debug("Creating new milestone: " + jsonMilestone);

            milestone = store.create(GitHubMilestone.class);
            milestone.setMilestoneId(xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName() + "#" + jsonMilestone.getNumber());
            milestone.setTitle(jsonMilestone.getTitle());
            milestone.setDescription(jsonMilestone.getDescription());
            milestone.setCreatedAt(jsonMilestone.getCreatedAt());
            milestone.setUpdatedAt(jsonMilestone.getUpdatedAt());
            milestone.setDueOn(jsonMilestone.getDueOn());
            milestone.setState(jsonMilestone.getState());
            milestone.setNumber(jsonMilestone.getNumber());

            milestone.setCreatedBy(findOrCreateGitHubUser(store, jsonMilestone.getCreator()));

            DescriptorCache.getInstance().put(milestone);
        }

        return milestone;
    }

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
