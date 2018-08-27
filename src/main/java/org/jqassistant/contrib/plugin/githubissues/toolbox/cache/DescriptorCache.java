package org.jqassistant.contrib.plugin.githubissues.toolbox.cache;


import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.json.JSONIssue;
import org.jqassistant.contrib.plugin.githubissues.json.JSONLabel;
import org.jqassistant.contrib.plugin.githubissues.json.JSONMilestone;
import org.jqassistant.contrib.plugin.githubissues.json.JSONUser;
import org.jqassistant.contrib.plugin.githubissues.model.*;

import java.util.HashMap;

class DescriptorCache {

    private HashMap<String, GitHubRepository> repositories;
    private HashMap<String, GitHubCommit> commits;
    private HashMap<String, GitHubIssue> issues;
    private HashMap<String, GitHubUser> users;
    private HashMap<String, GitHubLabel> labels;
    private HashMap<String, GitHubMilestone> milestones;

    DescriptorCache() {

        repositories = new HashMap<>();
        commits = new HashMap<>();
        issues = new HashMap<>();
        users = new HashMap<>();
        labels = new HashMap<>();
        milestones = new HashMap<>();
    }

    GitHubMilestone get(JSONMilestone milestone, XMLGitHubRepository xmlGitHubRepository) {

        return milestones.get(xmlGitHubRepository.getUser() +
                "/" + xmlGitHubRepository.getName() +
                "#" + milestone.getNumber());
    }

    GitHubUser get(JSONUser user) {

        return users.get(user.getLogin());
    }

    GitHubLabel get(JSONLabel label) {

        return labels.get(label.getName());
    }

    GitHubRepository get(XMLGitHubRepository xmlGitHubRepository) {

        return repositories.get(xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName());
    }

    /**
     * Matching commits is more complex than matching other entities:
     * <p>
     * Even if the GitHub-REST API is used to resolve markdown references the following issue can occur:
     * <p>
     * "kontext-e/jqassistant-plugins@a00cd018208b04caa08a32f970067cf8ec837eb8" and
     * "kontext-e/jqassistant-plugins@a00cd01" point at the same commit. Still, the GitHub API parses
     * them to different hrefs:
     * ../commit/a00cd018208b04caa08a32f970067cf8ec837eb8 and ../commit/a00cd01.
     * <p>
     * Therefore, we need to check if one of the commit hashes is a prefix of the other.
     *
     * @param repoUser  The repository user.
     * @param repoName  The repository name.
     * @param commitSha The commit hash.
     * @return The cached GitHubCommit or Null if it does not exist yet.
     */
    GitHubCommit getCommit(String repoUser, String repoName, String commitSha) {

        String id = repoUser + "/" + repoName + "#" + commitSha;

        for (String key : commits.keySet()) {
            if (key.startsWith(id) || id.startsWith(key)) {
                return commits.get(key);
            }
        }
        return null;
    }

    GitHubIssue get(JSONIssue issue, XMLGitHubRepository xmlGitHubRepository) {

        return issues.get(xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName() + "#" + issue.getNumber());
    }

    GitHubIssue getIssue(String repoUser, String repoName, String issueNumber) {

        return issues.get(repoUser + "/" + repoName + "#" + issueNumber);
    }

    void put(GitHubUser user) {

        if (!users.containsKey(user.getLogin())) {
            users.put(user.getLogin(), user);
        }
    }

    void put(GitHubMilestone milestone) {

        if (!milestones.containsKey(milestone.getMilestoneId())) {
            milestones.put(milestone.getMilestoneId(), milestone);
        }
    }

    void put(GitHubLabel label) {

        if (!labels.containsKey(label.getName())) {
            labels.put(label.getName(), label);
        }
    }

    void put(GitHubRepository repository) {

        if (!repositories.containsKey(repository.getRepositoryId())) {
            repositories.put(repository.getRepositoryId(), repository);
        }
    }

    void put(GitHubCommit commit) {

        if (!commits.containsKey(commit.getId())) {
            commits.put(commit.getId(), commit);
        }
    }

    void put(GitHubIssue issue) {

        if (!issues.containsKey(issue.getIssueId())) {
            issues.put(issue.getIssueId(), issue);
        }
    }
}
