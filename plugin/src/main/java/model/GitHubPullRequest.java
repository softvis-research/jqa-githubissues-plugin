package model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.time.ZonedDateTime;

@Label("PullRequest")
public interface GitHubPullRequest extends GitHubIssue {

    @Property("mergedAt")
    ZonedDateTime getMergedAt();
    void setMergedAt(ZonedDateTime mergedAt);

    @Relation("HAS_LAST_COMMIT")
    GitHubCommit getLastCommit();
    void setLastCommit(GitHubCommit lastCommit);
}
