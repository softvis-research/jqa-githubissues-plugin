package org.jqassistant.contrib.plugin.githubissues.toolbox.cache;


import org.jqassistant.contrib.plugin.githubissues.ids.*;
import org.jqassistant.contrib.plugin.githubissues.model.*;

import java.util.HashMap;

/**
 * This class caches descriptor instances which have already been created.
 * <p>
 * For more information see {@link CacheEndpoint} which is the public accessible interface for this cache.
 */
class DescriptorCache {

    private HashMap<RepositoryID, GitHubRepository> repositories;
    private HashMap<CommitID, GitHubCommit> commits;
    private HashMap<IssueID, GitHubIssue> issues;
    private HashMap<CommentID, GitHubComment> comments;
    private HashMap<UserID, GitHubUser> users;
    private HashMap<LabelID, GitHubLabel> labels;
    private HashMap<MilestoneID, GitHubMilestone> milestones;

    DescriptorCache() {

        repositories = new HashMap<>();
        commits = new HashMap<>();
        issues = new HashMap<>();
        users = new HashMap<>();
        labels = new HashMap<>();
        milestones = new HashMap<>();
        comments = new HashMap<>();
    }

    GitHubMilestone get(MilestoneID milestoneID) {

        return milestones.get(milestoneID);
    }

    GitHubUser get(UserID userID) {

        return users.get(userID);
    }

    GitHubLabel get(LabelID labelID) {

        return labels.get(labelID);
    }

    GitHubRepository get(RepositoryID repositoryID) {

        return repositories.get(repositoryID);
    }

    GitHubCommit get(CommitID commitID) {

        return commits.get(commitID);
    }

    GitHubIssue get(IssueID issueID) {

        return issues.get(issueID);
    }

    GitHubComment get(CommentID commentID) {

        return comments.get(commentID);
    }

    /*
    PUT-Functions for which the descriptor contains its ID information:
     */

    void put(GitHubUser user) {

        UserID userID = UserID.builder()
            .login(user.getLogin())
            .build();

        if (!users.containsKey(userID)) {
            users.put(userID, user);
        }
    }

    void put(GitHubLabel label) {

        LabelID labelId = LabelID.builder()
            .name(label.getName())
            .build();

        if (!labels.containsKey(labelId)) {
            labels.put(labelId, label);
        }
    }

    /*
    PUT-Functions with descriptors that need context information to identify them:
     */

    void put(GitHubMilestone milestone, MilestoneID milestoneID) {

        if (!milestones.containsKey(milestoneID)) {
            milestones.put(milestoneID, milestone);
        }
    }

    void put(GitHubRepository repository, RepositoryID repositoryID) {

        if (!repositories.containsKey(repositoryID)) {
            repositories.put(repositoryID, repository);
        }
    }

    void put(GitHubCommit commit, CommitID commitID) {

        if (!commits.containsKey(commitID)) {
            commits.put(commitID, commit);
        }
    }

    void put(GitHubIssue issue, IssueID issueID) {

        if (!issues.containsKey(issueID)) {
            issues.put(issueID, issue);
        }
    }

    void put(GitHubComment comment, CommentID commentID) {

        if (!comments.containsKey(commentID)) {
            comments.put(commentID, comment);
        }
    }
}
