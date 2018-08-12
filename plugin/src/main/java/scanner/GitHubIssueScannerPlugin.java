package scanner;

import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.Scope;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.AbstractScannerPlugin;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FileResource;
import jdom.XMLGitHubRepository;
import jdom.XMLParser;
import model.GitHub;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@ScannerPlugin.Requires(FileDescriptor.class)
public class GitHubIssueScannerPlugin extends AbstractScannerPlugin<FileResource, GitHub> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubIssueScannerPlugin.class);

    private static final String JQASSISTANT_PLUGIN_GITHUBISSUES_FILENAME = "jqassistant.plugin.githubissues.filename";

    private static String gitHubIssuesFileName = "githubissues.xml";

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
    public GitHub scan(FileResource item, String path, Scope scope, Scanner scanner) throws IOException {

        LOGGER.debug(String.format("GitHub-Issues plugin scans file %s.", path));

        List<XMLGitHubRepository> xmlRepositoryList;
        try {
            xmlRepositoryList = XMLParser.parseConfiguration(item.createStream());
        } catch (JDOMException e) {
            LOGGER.error(path + " could not be parsed. Error:", e);
            return null;
        }

        FileDescriptor fileDescriptor = scanner.getContext().getCurrentDescriptor();
        final GitHub gitHubIssuesDescriptor = scanner
                .getContext()
                .getStore()
                .addDescriptorType(fileDescriptor, GitHub.class);

        GraphBuilder graphBuilder  = new GraphBuilder(
                scanner.getContext().getStore(),
                xmlRepositoryList,
                gitHubIssuesDescriptor);

        graphBuilder.startTraversal();

        return gitHubIssuesDescriptor;
    }
}
