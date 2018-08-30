package org.jqassistant.contrib.plugin.githubissues.ids;

import lombok.EqualsAndHashCode;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubRepository;

@EqualsAndHashCode
public class RepositoryId {

    private String user;
    private String name;

    public RepositoryId(XMLGitHubRepository xmlGitHubRepository) {

        this.user = xmlGitHubRepository.getUser();
        this.name = xmlGitHubRepository.getName();
    }

    public RepositoryId(GitHubRepository gitHubRepository) {

        this.user = gitHubRepository.getUser();
        this.name = gitHubRepository.getName();
    }
}
