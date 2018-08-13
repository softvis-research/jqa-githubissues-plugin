package model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("PullRequest")
public interface GitHubPullRequest extends GitHubIssue {

    @Property("mergedAt")
    String getMergedAt();
    void setMergedAt(String mergedAt);

    @Relation("HAS_LAST_COMMIT")
    GitHubCommit getLastCommit();
    void setLastCommit(GitHubCommit lastCommit);
}
