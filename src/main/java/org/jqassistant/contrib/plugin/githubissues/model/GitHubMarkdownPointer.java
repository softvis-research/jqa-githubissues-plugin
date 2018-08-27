package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

public interface GitHubMarkdownPointer {

    @Relation("POINTS_AT")
    Set<GitHubIssue> getGitHubIssues();

    @Relation("POINTS_AT")
    Set<GitHubCommit> getGitHubCommits();

    @Relation("POINTS_AT")
    Set<GitHubUser> getGitHubUsers();
}