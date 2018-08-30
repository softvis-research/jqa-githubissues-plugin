package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.time.ZonedDateTime;

@Label("Milestone")
public interface GitHubMilestone extends GitHub, GitHubTimestamps {

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

    @Property("dueOn")
    ZonedDateTime getDueOn();
    void setDueOn(ZonedDateTime dueOn);

    @Relation("CREATED_BY")
    GitHubUser getCreatedBy();
    void setCreatedBy(GitHubUser user);
}
