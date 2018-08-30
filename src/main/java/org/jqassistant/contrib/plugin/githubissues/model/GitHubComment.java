package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.time.ZonedDateTime;

@Label("Comment")
public interface GitHubComment extends GitHub, GitHubMarkdownPointer, GitHubTimestamps {

    @Property("commentId")
    String getCommentId();
    void setCommentId(String CommentId);

    @Property("body")
    String getBody();
    void setBody(String body);

    @Relation("CREATED_BY")
    GitHubUser getUser();
    void setUser(GitHubUser user);

    @Relation("FOLLOWED_BY")
    GitHubComment getComment();
    void setComment(GitHubComment comment);
}
