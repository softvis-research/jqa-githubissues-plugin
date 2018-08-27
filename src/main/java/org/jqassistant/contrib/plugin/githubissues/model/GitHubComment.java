package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.time.ZonedDateTime;

@Label("Comment")
public interface GitHubComment extends GitHub, GitHubMarkdownPointer {

    @Property("body")
    String getBody();
    void setBody(String body);

    @Property("createdAt")
    ZonedDateTime getCreatedAt();
    void setCreatedAt(ZonedDateTime createdAt);

    @Property("updatedAt")
    ZonedDateTime getUpdatedAt();
    void setUpdatedAt(ZonedDateTime updatedAt);

    @Relation("CREATED_BY")
    GitHubUser getUser();
    void setUser(GitHubUser user);

    @Relation("FOLLOWED_BY")
    GitHubComment getComment();
    void setComment(GitHubComment comment);

}