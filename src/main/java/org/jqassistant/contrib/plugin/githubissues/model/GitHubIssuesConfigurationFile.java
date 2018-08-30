package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("GitHub-Issues-Configuration-File")
public interface GitHubIssuesConfigurationFile extends GitHub, FileDescriptor {

    @Relation("SPECIFIES_REPOSITORY")
    List<GitHubRepository> getRepositories();
}
