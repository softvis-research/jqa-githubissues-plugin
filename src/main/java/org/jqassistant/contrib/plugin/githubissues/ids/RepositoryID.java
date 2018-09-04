package org.jqassistant.contrib.plugin.githubissues.ids;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Builder
public class RepositoryID {

    private String user;
    private String name;
}
