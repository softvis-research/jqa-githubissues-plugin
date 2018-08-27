package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("User")
public interface GitHubUser extends GitHub {

    @Property("login")
    String getLogin();
    void setLogin(String login);

}