package org.jqassistant.contrib.plugin.githubissues.scanner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.jqassistant.contrib.plugin.githubissues.model.GitHubIssuesConfigurationFile;
import org.jqassistant.contrib.plugin.githubissues.scanner.stubbing.StubbingTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScannerTestIT extends AbstractPluginIT {

    WireMockServer wireMockServer;

    @BeforeEach
    public void setup () {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
    }

    @AfterEach
    public void teardown () {
        wireMockServer.stop();
    }

    @Test
    public void scanGitHubIssues() throws IOException {

        StubbingTool.stubGitHubAPI();

        store.beginTransaction();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("githubissues.xml")).getFile());

        Descriptor descriptor = getScanner().scan(file, "/githubissues.xml", DefaultScope.NONE);

        assertThat(descriptor).isInstanceOf(GitHubIssuesConfigurationFile.class);

        GitHubIssuesConfigurationFile gitHubIssuesConfigurationFile = (GitHubIssuesConfigurationFile) descriptor;
        assertEquals(1, gitHubIssuesConfigurationFile.getRepositories().size());

        assertEquals("github-user", gitHubIssuesConfigurationFile.getRepositories().get(0).getUser());
        assertEquals(1, gitHubIssuesConfigurationFile.getRepositories().get(0).getMilestones().size());
        assertEquals(1, gitHubIssuesConfigurationFile.getRepositories().get(0).getContains().size());

        TestResult testResult = query(
                "MATCH\n" +
                "    (r:Repository)-[:HAS_ISSUE]->(i:Issue {state:\"open\"})\n" +
                "RETURN\n" +
                "    r.repositoryId, i.title, i.body");

        assertEquals(1, testResult.getColumn("i.title").size());
        assertEquals("Found a bug", testResult.getColumn("i.title").get(0));

        store.commitTransaction();
    }
}
