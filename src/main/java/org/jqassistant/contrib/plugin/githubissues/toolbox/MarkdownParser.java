package org.jqassistant.contrib.plugin.githubissues.toolbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.jersey.api.client.UniformInterfaceException;
import lombok.AllArgsConstructor;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.json.JSONUser;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubMarkdownPointer;
import org.jqassistant.contrib.plugin.githubissues.toolbox.cache.CacheEndpoint;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@link MarkdownParser} is an additional feature which resolves references in the text body of issues and
 * comments.
 * <p>
 * For more information have a look at the documentation of
 * {@link #getReferencesInMarkdown(String, GitHubMarkdownPointer, XMLGitHubRepository, RestTool)}.
 */
@AllArgsConstructor
public class MarkdownParser {

    private static final String[] REPLACES = {"https://github.com/", "commit/", "issues/"};

    private static final Logger LOGGER = LoggerFactory.getLogger(MarkdownParser.class);

    private CacheEndpoint cacheEndpoint;

    /**
     * This function parses bodies of descriptors that contain markdown information, e.g. issues
     * or comments.
     * </p>
     * The following references get resolved:
     * <ul>
     * <li>Commits,</li>
     * <li>Issues</li>
     * <li>and Users.</li>
     * </ul>
     * IMPORTANT: The depth for resolving references is 1!
     *
     * @param markdown              The markdown that shall be parsed.
     * @param gitHubMarkdownPointer The descriptor containing this markdown.
     * @param xmlGitHubRepository   The repository context from the plugin configuration.
     * @throws IOException If the parsing of an issue request fails.
     */
    public void getReferencesInMarkdown(
        String markdown,
        GitHubMarkdownPointer gitHubMarkdownPointer,
        XMLGitHubRepository xmlGitHubRepository,
        RestTool restTool) throws IOException {

        try {
            // https://developer.github.com/v3/guides/best-practices-for-integrators/#dealing-with-abuse-rate-limits
            Thread.sleep(1000);

            LOGGER.debug("MARKDOWN:\n" + markdown + "\n");

            String html = restTool.requestMarkdownToHtml(markdown);

            LOGGER.debug("HTML:\n" + html + "\n");

            Document htmlDocument = Jsoup.parse(html);

            for (Element issueElement : htmlDocument.select("a.issue-link")) {

                List<String> hrefCut = removeDispensableHrefPartsAndCutIt(issueElement.attr("data-url"));

                gitHubMarkdownPointer.getGitHubIssues().add(
                    cacheEndpoint.findOrCreateGitHubIssue(
                        hrefCut.get(0),
                        hrefCut.get(1),
                        Integer.parseInt(hrefCut.get(2)),
                        xmlGitHubRepository,
                        restTool));
            }

            for (Element commitElement : htmlDocument.select("a.commit-link")) {

                List<String> hrefCut = removeDispensableHrefPartsAndCutIt(commitElement.attr("href"));

                gitHubMarkdownPointer.getGitHubCommits().add(
                    cacheEndpoint.findOrCreateGitHubCommit(
                        hrefCut.get(0),
                        hrefCut.get(1),
                        hrefCut.get(2)));
            }

            for (Element userElement : htmlDocument.select("a.user-mention")) {

                List<String> hrefCut = removeDispensableHrefPartsAndCutIt(userElement.attr("href"));

                gitHubMarkdownPointer.getGitHubUsers().add(
                    cacheEndpoint.findOrCreateGitHubUser(new JSONUser(hrefCut.get(0))));
            }


        } catch (JsonProcessingException e) {
            LOGGER.error("Converting markdown to html threw an error:", e);
        } catch (UniformInterfaceException e) {

            LOGGER.error("Converting markdown to html threw an UniformInterfaceException: \"" + e.getMessage() + "\"");
            LOGGER.error("Entity:\n\n" + e.getResponse().getEntity(String.class));
            LOGGER.error("Header Retry-After: " + e.getResponse().getHeaders().get("Retry-After") + " s");
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException: ", e);
        } catch (RequestFailedException e) {
            LOGGER.warn("Markdown request failed:", e);
        }
    }

    /**
     * The HTML-anchors returned by the GitHub API contain URLs which can be used to extract IDs.
     * To make the extraction more clear dispensable parts will be removed and the URL will be cut
     * in sub parts.
     *
     * @param href The URL which shall be cleaned and cut.
     * @return A list of importing sub parts.
     */
    private static List<String> removeDispensableHrefPartsAndCutIt(String href) {

        List<String> result = new ArrayList<>();

        for (String replace : REPLACES) {
            href = href.replaceAll(replace, "");
        }

        // A user annotation consists of only one element: The user name.
        if (!href.contains("/")) {
            result.add(href);
        } else {
            // Issue and commit references are tuple (repoUser, repoName, commitSha | issueNumber).

            // Get repository user:
            result.add(href.substring(0, href.indexOf("/")));
            href = href.substring(href.indexOf("/") + 1);

            // Get repository name:
            result.add(href.substring(0, href.indexOf("/")));
            href = href.substring(href.indexOf("/") + 1);

            // Get commit sha or issue number:
            result.add(href);
        }

        return result;
    }
}
