package org.jqassistant.contrib.plugin.githubissues.ids;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class IssueID {

    private String repoUser;
    private String repoName;
    private int issueNumber;
}
