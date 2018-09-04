package org.jqassistant.contrib.plugin.githubissues.toolbox.cache;


import org.jqassistant.contrib.plugin.githubissues.ids.*;
import org.jqassistant.contrib.plugin.githubissues.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * This class caches descriptor instances which have already been created.
 * <p>
 * For more information see {@link CacheEndpoint} which is the public accessible interface for this cache.
 */
class DescriptorCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptorCache.class);

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

    void put(GitHubUser user) {

        UserID key = UserID.builder()
            .login(user.getLogin())
            .build();

        if (!users.containsKey(key)) {
            users.put(key, user);
        }
    }

    void put(GitHubMilestone milestone, String repoUser, String repoName) {

        MilestoneID key = MilestoneID.builder()
            .repoUser(repoUser)
            .repoName(repoName)
            .milestoneNumber(milestone.getNumber())
            .build();

        if (!milestones.containsKey(key)) {
            milestones.put(key, milestone);
        }
    }

    void put(GitHubLabel label) {

        LabelID key = LabelID.builder()
            .name(label.getName())
            .build();

        if (!labels.containsKey(key)) {
            labels.put(key, label);
        }
    }

    void put(GitHubRepository repository) {

        RepositoryID key = RepositoryID.builder()
            .user(repository.getUser())
            .name(repository.getName())
            .build();

        if (!repositories.containsKey(key)) {
            repositories.put(key, repository);
        }
    }

    void put(GitHubCommit commit, String repoUser, String repoName) {

        CommitID key = CommitID.builder()
            .repoUser(repoUser)
            .repoName(repoName)
            .commitSha(commit.getSha())
            .build();

        if (!commits.containsKey(key)) {
            commits.put(key, commit);
        }
    }

    void put(GitHubIssue issue, String repoUser, String repoName) {

        IssueID key = IssueID.builder()
            .repoUser(repoUser)
            .repoName(repoName)
            .issueNumber(issue.getNumber())
            .build();

        if (!issues.containsKey(key)) {
            issues.put(key, issue);
        }
    }

    void put(GitHubComment comment, String repoUser, String repoName) {

        CommentID key = CommentID.builder()
            .repoUser(repoUser)
            .repoName(repoName)
            .commentId(comment.getId())
            .build();

        if (!comments.containsKey(key)) {
            comments.put(key, comment);
        }
    }
}
