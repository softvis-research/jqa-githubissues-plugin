package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("Commit")
public interface GitHubCommit extends GitHub {

    @Property("id")
    String getId();
    void setId(String id);

    @Property("sha")
    String getSha();
    void setSha(String sha);
}