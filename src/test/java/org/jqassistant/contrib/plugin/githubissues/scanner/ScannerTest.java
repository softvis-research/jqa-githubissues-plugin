package org.jqassistant.contrib.plugin.githubissues.scanner;

import com.buschmais.jqassistant.core.scanner.api.DefaultScope;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.test.AbstractPluginIT;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.jqassistant.contrib.plugin.githubissues.model.GitHub;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.jqassistant.contrib.plugin.githubissues.scanner.stubbing.StubbingTool;
import org.jqassistant.contrib.plugin.githubissues.toolbox.RestTool;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ScannerTest extends AbstractPluginIT {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);


    @Test
    public void scanGitHubIssues() throws IOException {

        StubbingTool.stubGitHubAPI();

        store.beginTransaction();

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource("githubissues.xml")).getFile());

        Descriptor descriptor = getScanner().scan(file, "/githubissues.xml", DefaultScope.NONE);

        assertThat(descriptor, CoreMatchers.instanceOf(GitHub.class));

        GitHub gitHub = (GitHub) descriptor;
        assertEquals(1, gitHub.getContains().size());

        assertEquals("github-user", gitHub.getContains().get(0).getUser());
        assertEquals(1, gitHub.getContains().get(0).getMilestones().size());
        assertEquals(1, gitHub.getContains().get(0).getContains().size());

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
