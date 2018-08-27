package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

import java.util.List;

@Label("GitHub")
public interface GitHub extends Descriptor {

    @Property("SPECIFIES")
    List<GitHubRepository> getContains();
}
