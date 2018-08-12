package json;

import jdom.XMLGitHubRepository;
import lombok.*;

@Getter
@ToString
public class JSONMarkdownRequest {

    private String text;
    private String mode;
    private String context;

    public JSONMarkdownRequest(String text, XMLGitHubRepository xmlGitHubRepository) {

        this.text = text;
        this.mode = "gfm";
        this.context = xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName();
    }
}
