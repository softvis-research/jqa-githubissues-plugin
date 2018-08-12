package model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Comment")
public interface GitHubComment extends GitHub, GitHubMarkdownPointer {

    @Property("body")
    String getBody();
    void setBody(String body);

    @Property("createdAt")
    String getCreatedAt();
    void setCreatedAt(String createdAt);

    @Property("updatedAt")
    String getUpdatedAt();
    void setUpdatedAt(String updatedAt);

    @Relation("CREATED_BY")
    GitHubUser getUser();
    void setUser(GitHubUser user);

    @Relation("FOLLOWED_BY")
    GitHubComment getComment();
    void setComment(GitHubComment comment);

}