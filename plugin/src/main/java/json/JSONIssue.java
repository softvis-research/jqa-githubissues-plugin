package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class JSONIssue {

    private JSONUser user;

    private String title;

    private String body;

    private String state;

    private int number;

    private int comments;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    private List<JSONLabel> labels;

    private boolean locked;

    private List<JSONUser> assignees;

    private JSONMilestone milestone;

    // The following properties are pull-request related:

    @JsonProperty("pull_request")
    private JSONPullRequest pullRequest;

    @JsonProperty("merged_at")
    private String mergedAt;

    @JsonProperty("merge_commit_sha")
    private String mergeCommitSha;
}
