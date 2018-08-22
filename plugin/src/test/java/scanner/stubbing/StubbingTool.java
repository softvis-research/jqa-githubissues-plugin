package scanner.stubbing;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;

public abstract class StubbingTool {

    public static void stubGitHubAPI() throws IOException {

        stubMilestones();
        stubIssues();
    }

    private static void stubMilestones() throws IOException {

        stubFor(get("/repos/github-user/github-repository/milestones?state=all")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(JSONReader.readJsonInResources("milestones.json"))));
    }

    private static void stubIssues() throws IOException {

        stubFor(get("/repos/github-user/github-repository/issues?state=all")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(JSONReader.readJsonInResources("issues.json"))));
    }
}
