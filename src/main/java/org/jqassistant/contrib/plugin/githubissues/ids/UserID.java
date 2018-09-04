package org.jqassistant.contrib.plugin.githubissues.ids;

import lombok.Builder;
import lombok.EqualsAndHashCode;

@Builder
@EqualsAndHashCode
public class UserID {

    private String login;
}
