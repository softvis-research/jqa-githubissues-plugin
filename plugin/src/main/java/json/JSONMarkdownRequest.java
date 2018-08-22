package json;

import jdom.XMLGitHubRepository;
import lombok.*;

@Getter
public class JSONMarkdownRequest {

    private String text;
    private String mode;
    private String context;

    /**
     * Created a markdown-parsing request payload from a markdown text and context information.
     *
     * @param text                The markdown text that shall be parsed to HTML.
     * @param xmlGitHubRepository Context information needed by the GitHub markdown parser.
     */
    public JSONMarkdownRequest(String text, XMLGitHubRepository xmlGitHubRepository) {

        this.text = text;
        this.mode = "gfm";
        this.context = xmlGitHubRepository.getUser() + "/" + xmlGitHubRepository.getName();
    }
}
