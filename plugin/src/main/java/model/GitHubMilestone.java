package model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Milestone")
public interface GitHubMilestone extends GitHub {

    @Property("milestoneId")
    String getMilestoneId();
    void setMilestoneId(String milestoneId);

    @Property("title")
    String getTitle();
    void setTitle(String text);

    @Property("description")
    String getDescription();
    void setDescription(String description);

    @Property("state")
    String getState();
    void setState(String state);

    @Property("number")
    int getNumber();
    void setNumber(int number);

    @Property("createdAt")
    String getCreatedAt();
    void setCreatedAt(String createdAt);

    @Property("updatedAt")
    String getUpdatedAt();
    void setUpdatedAt(String updatedAt);

    @Property("dueOn")
    String getDueOn();
    void setDueOn(String dueOn);

    @Relation("CREATED_BY")
    GitHubUser getCreatedBy();
    void setCreatedBy(GitHubUser user);
}
