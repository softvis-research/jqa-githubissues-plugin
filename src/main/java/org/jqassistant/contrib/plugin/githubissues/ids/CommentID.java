package org.jqassistant.contrib.plugin.githubissues.ids;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class CommentID {

    private String repoUser;
    private String repoName;
    private long commentId;
}
