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

@ScannerPlugin.Requires(FileDescriptor.class)
public class GitHubIssueScannerPlugin extends AbstractScannerPlugin<FileResource, GitHubIssuesConfigurationFile> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubIssueScannerPlugin.class);

    private static final String JQASSISTANT_PLUGIN_GITHUBISSUES_FILENAME = "jqassistant.plugin.githubissues.filename";

    private String gitHubIssuesFileName = "githubissues.xml";
    private String apiUrl = "https://api.github.com/";

    @Override
    protected void configure() {
        super.configure();

        if (getProperties().containsKey(JQASSISTANT_PLUGIN_GITHUBISSUES_FILENAME)) {
            gitHubIssuesFileName = (String) getProperties().get(JQASSISTANT_PLUGIN_GITHUBISSUES_FILENAME);
        }
        LOGGER.info(String.format("GitHub-Issues plugin looks for files named %s.", gitHubIssuesFileName));
    }

    @Override
    public boolean accepts(FileResource item, String path, Scope scope) {
        boolean accepted = path.toLowerCase().endsWith(gitHubIssuesFileName);

        if (accepted) {
            LOGGER.debug(String.format("GitHub-Issues plugin accepted file %s.", path));
        }
        return accepted;
    }

    @Override
    public GitHubIssuesConfigurationFile scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {

        LOGGER.debug(String.format("GitHub-Issues plugin scans file %s.", path));

        // Create a cache based on the jQAssistant store:
        CacheEndpoint cacheEndpoint = new CacheEndpoint(getScannerContext().getStore());

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

        FileDescriptor fileDescriptor = scanner.getContext().getCurrentDescriptor();
        final GitHubIssuesConfigurationFile gitHubIssuesConfigurationFile = scanner
                .getContext()
                .getStore()
                .addDescriptorType(fileDescriptor, GitHubIssuesConfigurationFile.class);

        GraphBuilder graphBuilder = new GraphBuilder(scanner.getContext().getStore(), this.apiUrl, cacheEndpoint);

        graphBuilder.startTraversal(gitHubIssuesConfigurationFile, xmlRepositoryList);

        return gitHubIssuesConfigurationFile;
    }
}
