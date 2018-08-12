package model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Issue")
public interface GitHubIssue extends GitHub, GitHubMarkdownPointer {

    @Property("issueId")
    String getIssueId();
    void setIssueId(String issueId);

    @Property("title")
    String getTitle();
    void setTitle(String text);

    @Property("body")
    String getBody();
    void setBody(String body);

    @Property("state")
    String getState();
    void setState(String state);

    @Property("number")
    int getNumber();
    void setNumber(int number);

    @Property("comments")
    int getComments();
    void setComments(int comments);

    @Property("createdAt")
    String getCreatedAt();
    void setCreatedAt(String createdAt);

    @Property("updatedAt")
    String getUpdatedAt();
    void setUpdatedAt(String updatedAt);

    @Property("locked")
    boolean isLocked();
    void setLocked(boolean locked);

    @Relation("HAS_LABEL")
    List<GitHubLabel> getLabeles();

    @Relation("HAS_ASSIGNEE")
    List<GitHubUser> getAssignees();

    @Relation("HAS_COMMENT")
    List<GitHubComment> getCommented();

    @Relation("CREATED_BY")
    GitHubUser getCreatedBy();
    void setCreatedBy(GitHubUser user);

    @Relation("IS_PART_OF")
    GitHubMilestone getMilestone();
    void setMilestone(GitHubMilestone milestone);
}