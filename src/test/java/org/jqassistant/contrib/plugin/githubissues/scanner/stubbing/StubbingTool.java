package org.jqassistant.contrib.plugin.githubissues.scanner.stubbing;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public abstract class StubbingTool {

    public static void stubGitHubAPI() throws IOException {

        stubMilestones();
        stubIssues();
        stubPullRequest();
        stubComments();
        stubMarkdown();
        stubSingleIssue();
    }

    private static void stubMilestones() throws IOException {
        stub("/repos/github-user/github-repository/milestones?state=all&per_page=100",
            "rest-mocks/milestones.json");
    }

    private static void stubIssues() throws IOException {
        stub("/repos/github-user/github-repository/issues?state=all&per_page=100",
            "rest-mocks/issues.json");
    }

    private static void stubPullRequest() throws IOException {
        stub("/repos/octocat/Hello-World/pulls/1347", "rest-mocks/pullrequest.json");
    }

    private static void stubComments() throws IOException {
        stub("/repos/github-user/github-repository/issues/1347/comments?per_page=100",
            "rest-mocks/comments.json");
    }

    private static void stubMarkdown() throws IOException {

        InputStream in = getInputStreamFromFile("rest-mocks/markdown.html");

        stubFor(post("/markdown")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type",
                            "application/org.jqassistant.contrib.plugin.githubissues.json")
                        .withBody(IOUtils.toString(in))));
    }

    private static void stubSingleIssue() throws IOException {
        stub("/repos/octocat/Hello-World/issues/1347", "rest-mocks/issue.json");
    }

    private static void stub(String url, String fileName) throws IOException {

        InputStream in = getInputStreamFromFile(fileName);

        stubFor(get(url)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(IOUtils.toString(in))));
    }

    private static InputStream getInputStreamFromFile(String fileName) throws FileNotFoundException {

        ClassLoader classLoader = StubbingTool.class.getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());

        return new FileInputStream(file);
    }
}
