package org.jqassistant.contrib.plugin.githubissues.jdom;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class XMLGitHubRepository {

    private String user;
    private String name;
    private XMLCredentials credentials;
}
