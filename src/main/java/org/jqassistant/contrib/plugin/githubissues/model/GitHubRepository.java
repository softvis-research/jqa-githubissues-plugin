package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Repository")
public interface GitHubRepository extends GitHub, Descriptor, NamedDescriptor {

    @Property("repositoryId")
    String getRepositoryId();
    void setRepositoryId(String repositoryId);

    @Property("user")
    String getUser();
    void setUser(String user);

    @Property("name")
    String getName();
    void setName(String name);

    @Relation("HAS_ISSUE")
    List<GitHubIssue> getContains();

    @Relation("HAS_MILESTONE")
    List<GitHubMilestone> getMilestones();
}
