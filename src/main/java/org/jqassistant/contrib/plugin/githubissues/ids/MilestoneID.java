package org.jqassistant.contrib.plugin.githubissues.ids;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class MilestoneID {

    private String repoUser;
    private String repoName;
    private int milestoneNumber;
}
