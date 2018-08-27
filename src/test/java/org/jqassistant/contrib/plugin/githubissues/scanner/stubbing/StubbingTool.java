package org.jqassistant.contrib.plugin.githubissues.scanner.stubbing;

import java.io.IOException;

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
        stub("/repos/github-user/github-repository/milestones?state=all", "rest-mocks/milestones.json");
    }

    private static void stubIssues() throws IOException {
        stub("/repos/github-user/github-repository/issues?state=all", "rest-mocks/issues.json");
    }

    private static void stubPullRequest() throws IOException {
        stub("/repos/octocat/Hello-World/pulls/1347", "rest-mocks/pullrequest.json");
    }

    private static void stubComments() throws IOException {
        stub("/repos/github-user/github-repository/issues/1347/comments", "rest-mocks/comments.json");
    }

    private static void stubMarkdown() throws IOException {

        stubFor(post("/markdown")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/org.jqassistant.contrib.plugin.githubissues.json")
                        .withBody(FileReader.readFileInResources("rest-mocks/markdown.html"))));
    }

    private static void stubSingleIssue() throws IOException {
        stub("/repos/octocat/Hello-World/issues/1347", "rest-mocks/issue.json");
    }

    private static void stub(String url, String fileName) throws IOException {

        stubFor(get(url)
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/org.jqassistant.contrib.plugin.githubissues.json")
                        .withBody(FileReader.readFileInResources(fileName))));
    }
}
