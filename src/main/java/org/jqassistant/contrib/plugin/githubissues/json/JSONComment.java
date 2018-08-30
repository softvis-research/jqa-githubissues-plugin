package org.jqassistant.contrib.plugin.githubissues.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class JSONComment {

    private int id;

    private String body;

    private JSONUser user;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}
