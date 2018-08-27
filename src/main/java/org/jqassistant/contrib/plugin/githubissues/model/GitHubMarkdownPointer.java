package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

public interface GitHubMarkdownPointer {

    @Relation("REFERENCES_ISSUE")
    Set<GitHubIssue> getGitHubIssues();

    @Relation("REFERENCES_COMMIT")
    Set<GitHubCommit> getGitHubCommits();

    @Relation("REFERENCES_USER")
    Set<GitHubUser> getGitHubUsers();
}