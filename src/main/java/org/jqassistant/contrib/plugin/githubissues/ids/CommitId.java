package org.jqassistant.contrib.plugin.githubissues.ids;


import org.jqassistant.contrib.plugin.githubissues.model.GitHubCommit;

public class CommitId {

    private String repoUser;
    private String repoName;
    private String commitSha;

    public CommitId(String repoUser, String repoName, String commitSha) {

        this.repoUser = repoUser;
        this.repoName = repoName;
        this.commitSha = commitSha;
    }

    public CommitId(GitHubCommit gitHubCommit) {


    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof CommitId)) {
            return false;
        }

        CommitId otherCommitId = (CommitId) other;

        boolean sameHash = otherCommitId.commitSha.startsWith(this.commitSha)
            || this.commitSha.startsWith(otherCommitId.commitSha);

        return repoName.equals(otherCommitId.repoName)
            && repoUser.equals(otherCommitId.repoUser)
            && sameHash;
    }
}
