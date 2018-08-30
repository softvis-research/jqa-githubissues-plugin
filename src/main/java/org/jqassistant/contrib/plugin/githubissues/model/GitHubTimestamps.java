package org.jqassistant.contrib.plugin.githubissues.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

import java.time.ZonedDateTime;

public interface GitHubTimestamps {

    @Property("createdAt")
    ZonedDateTime getCreatedAt();
    void setCreatedAt(ZonedDateTime createdAt);

    @Property("updatedAt")
    ZonedDateTime getUpdatedAt();
    void setUpdatedAt(ZonedDateTime updatedAt);
}
