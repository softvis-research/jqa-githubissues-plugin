package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("Label")
public interface GitHubLabel extends GitHub {

    @Property("name")
    String getName();
    void setName(String name);

    @Property("description")
    String getDescription();
    void setDescription(String description);
}
