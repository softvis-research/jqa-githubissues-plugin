package json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class JSONMilestone {

    private int id;

    private String title;

    private String description;

    private String state;

    private int number;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("due_on")
    private String dueOn;

    private JSONUser creator;
}
