package org.jqassistant.contrib.plugin.githubissues.scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import org.jdom2.JDOMException;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLGitHubRepository;
import org.jqassistant.contrib.plugin.githubissues.jdom.XMLParser;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubIssuesConfigurationFile;
import org.jqassistant.contrib.plugin.githubissues.toolbox.cache.CacheEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * The {@link GitHubIssueScannerPlugin} class is the centerpiece of the GitHub-Issues plugin.
 * <p>
 * It specifies which files shall be processed by the plugin (see {@link #accepts(FileResource, String, Scope)}).
 * Furthermore, it starts the scan process with its {@link #scan(FileResource, String, Scope, Scanner)} method.
 */
@ScannerPlugin.Requires(FileDescriptor.class)
public class GitHubIssueScannerPlugin extends AbstractScannerPlugin<FileResource, GitHubIssuesConfigurationFile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubIssueScannerPlugin.class);

    private static final String JQASSISTANT_PLUGIN_GITHUBISSUES_FILENAME = "jqassistant.plugin.githubissues.filename";

    private String gitHubIssuesFileName = "githubissues.xml";
    private String apiUrl = "https://api.github.com/";

    /**
     * This method checks if the user wants to override the default configuration file name
     * by setting a property.
     */
    @Override
    protected void configure() {
        super.configure();

        if (getProperties().containsKey(JQASSISTANT_PLUGIN_GITHUBISSUES_FILENAME)) {
            gitHubIssuesFileName = (String) getProperties().get(JQASSISTANT_PLUGIN_GITHUBISSUES_FILENAME);
        }
        LOGGER.info(String.format("GitHub-Issues plugin looks for files named %s.", gitHubIssuesFileName));
    }

    /**
     * This methods ensures that only the configuration file for the plugin will be processed.
     *
     * @param item  The file that shall be checked.
     * @param path  The path of the file.
     * @param scope The current jQAssistant scope.
     * @return True if the file matches a certain suffix, otherwise False.
     */
    @Override
    public boolean accepts(FileResource item, String path, Scope scope) {
        boolean accepted = path.toLowerCase().endsWith(gitHubIssuesFileName);

        if (accepted) {
            LOGGER.debug(String.format("GitHub-Issues plugin accepted file %s.", path));
        }
        return accepted;
    }

    /**
     * This function is called for every file which got accepted by the plugin.
     * <p>
     * First, it parses the XML configuration file.
     * Afterwards, it uses the repository information to start making calls to the GitHub REST API.
     *
     * @param item    The current accepted file which must be a valid configuration file.
     * @param path    The path of the configuration file.
     * @param scope   The current jQAssistant scope.
     * @param scanner The jQAssistant scanner which will be used to extract the main descriptor and the jQAssistant
     *                store.
     * @return The main descriptor which can specify multiple repositories.
     * @throws IOException If the application can't open a file stream for the configuration file.
     */
    @Override
    public GitHubIssuesConfigurationFile scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {

        LOGGER.debug(String.format("GitHub-Issues plugin scans file %s.", path));

        // Create a cache based on the jQAssistant store:
        CacheEndpoint cacheEndpoint = new CacheEndpoint(getScannerContext().getStore());

        // Read the configuration file:
        List<XMLGitHubRepository> xmlRepositoryList;
        String apiUrl = null;
        try {
            xmlRepositoryList = XMLParser.parseConfiguration(item.createStream());
            apiUrl = XMLParser.parseApiUrl(item.createStream());
        } catch (JDOMException e) {
            LOGGER.error(path + " could not be parsed. Error:", e);
            return null;
        } finally {

            // If an API URL is specified in the configuration file:
            if (apiUrl != null) {
                this.apiUrl = apiUrl;
            }
        }

        // Create the root descriptor
        FileDescriptor fileDescriptor = scanner.getContext().getCurrentDescriptor();
        final GitHubIssuesConfigurationFile gitHubIssuesConfigurationFile = scanner
            .getContext()
            .getStore()
            .addDescriptorType(fileDescriptor, GitHubIssuesConfigurationFile.class);

        // Start traversing the repositories
        GraphBuilder graphBuilder = new GraphBuilder(this.apiUrl, cacheEndpoint);

        graphBuilder.startTraversal(gitHubIssuesConfigurationFile, xmlRepositoryList);

        return gitHubIssuesConfigurationFile;
    }
}
