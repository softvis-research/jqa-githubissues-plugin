package org.jqassistant.contrib.plugin.githubissues.ids;


import lombok.Builder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Builder
public class CommitID {

    private String repoUser;
    private String repoName;
    private String commitSha;

    /**
     * Matching commits is more complex than matching other entities:
     * <p>
     * Even if the GitHub-REST API is used to resolve markdown references the following issue can occur:
     * <p>
     * "kontext-e/jqassistant-plugins@a00cd018208b04caa08a32f970067cf8ec837eb8" and
     * "kontext-e/jqassistant-plugins@a00cd01" point at the same commit. Still, the GitHub API parses
     * them to different hrefs:
     * ../commit/a00cd018208b04caa08a32f970067cf8ec837eb8 and ../commit/a00cd01.
     * <p>
     * Therefore, we need to check if one of the commit hashes is a prefix of the other.
     *
     * @return True if the objects are equals, False else.
     */
    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (!(other instanceof CommitID)) {
            return false;
        }

        CommitID otherCommitId = (CommitID) other;

        boolean sameHash = otherCommitId.commitSha.startsWith(this.commitSha)
            || this.commitSha.startsWith(otherCommitId.commitSha);

        return repoName.equals(otherCommitId.repoName)
            && repoUser.equals(otherCommitId.repoUser)
            && sameHash;
    }

    /**
     * This hash function is not optimal but it is needed to solve the same problem the {@link #equals(Object)} function
     * aims at.
     *
     * @return A hash build from the repository user and the repository name.
     */
    @Override
    public int hashCode() {

        return new HashCodeBuilder(17, 31)
                .append(repoUser)
                .append(repoName)
                .toHashCode();
    }
}
