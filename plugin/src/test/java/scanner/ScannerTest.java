package scanner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import model.GitHub;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import scanner.stubbing.StubbingTool;
import toolbox.RestTool;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertThat;

public class ScannerTest extends AbstractPluginIT {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);


    @Test
    public void scanGitHubIssues() throws IOException {

        StubbingTool.stubGitHubAPI();

        RestTool.GITHUB_API = "http://127.0.0.1:8089/";

        store.beginTransaction();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("githubissues.xml")).getFile());

        Descriptor descriptor = getScanner().scan(file, "/githubissues.xml", DefaultScope.NONE);

        assertThat(descriptor, CoreMatchers.instanceOf(GitHub.class));

        store.commitTransaction();

    }
}
