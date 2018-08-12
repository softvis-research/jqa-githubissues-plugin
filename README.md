# jQAssistant GitHub-Issues Plugin

[![GitHub license](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/b-pos465/jqa-githubissues-plugin/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/b-pos465/jqa-githubissues-plugin.svg?branch=master)](https://travis-ci.com/b-pos465/jqa-githubissues-plugin)

This is a GitHub issue scanner for [jQAssistant](https://jqassistant.org/). It enables jQAssistant to scan and analyze GitHub issues.

## Getting Started

Create a _githubissues.xml_:

```xml
<github-repositories>
    <github-repository>
        <user>github-user</user>
        <name>github-repository-name</name>

        <credentials>
            <user>authentication-user</user>
            <password>authentication-password</password>
        </credentials>
    </github-repository>

    <github-repository>
        ...
    </github-repository>
</github-repositories>
```

## Labels

The __GitHub-Issues__ plugin uses the following labels in the resulting graph:

| Label | Description                                                  | ID |
| ----- | ------------------------------------------------------------ |----|
|GitHub |Parent label for all nodes related to the GitHub-Issues plugin.| -|
|Repository|Represents a GitHub Repository.| "repo-user/repo-name"|
|Issue|Represents a GitHub Issue.| "repo-user/repo-name#issue-number" |
|Milestone|Represents a GitHub Milestone which is a collection of Issues. | "repo-user/repo-name#milestone-id" |
|Comment|Represents a Comment under a GitHub Issue.| - |
|PullRequest|Every PullRequest is an Issue, but not every Issue is a PullRequest.| "repo-user/repo-name#issue-number" |
|User|Represents a GitHub User.| "user-name" |
|Commit|Represents a GitHub Commit.| "repo-user/repo-name#commit-sha" |

Here you can find possible relations between those labels:

```mysql
(Repository)  -[:HAS_ISSUE]       ->    (Issue)
(Repository)  -[:HAS_MILESTONE]   ->    (Milestone)
(Issue)       -[:CREATED_BY]      ->    (User)
(Issue)       -[:HAS_ASSIGNEE]    ->    (User)
(Issue)       -[:IS_PART_OF]      ->    (Milestone)
(Issue)       -[:HAS_COMMENT]     ->    (Comment)

```



## Use Cases

### Overview

List all your open Issues over multiple repositories:

```mysql
MATCH
    (r:Repository)-[:HAS_ISSUE]->(i:Issue {state:"open"})
RETURN
    r.repositoryId, i.title, i.body
```

Count open Issues per repository:

```mysql
MATCH
    (r:Repository)-[:HAS_ISSUE]->(Issue {state:"open"})
RETURN
    r.repositoryId, count(*) AS issueCount
ORDER BY
    issueCount DESC
```

List open issues per user:

```mysql
MATCH
    (Issue {state:"open"})-[:HAS_ASSIGNEE]->(u:User)
RETURN
    u.login, count(*)
```

### Issue quality

Show issues without description:

```mysql
MATCH
    (i:Issue)
WHERE
    i.body = ""
RETURN
    i.issueId, i.title
```

Show issues without labels:

```mysql
MATCH 
    (i:Issue)
WHERE 
    NOT (i:Issue)-[:HAS_LABEL]->()
RETURN
    i.title, i.issueId
```

Show issues ordered descanding by the amount of coments:
```mysql
MATCH 
    path=((i:Issue)-[:HAS_COMMENT]->()-[:FOLLOWED_BY*]->())
RETURN
    i.title, i.issueId, length(path) AS pathLength, i.state
ORDER BY
    pathLength DESC, i.state DESC
```

## Development

Scan a _.jar_ artifact:
```bash
# Remove the old Neo4J database
rm -r jqassistant

# Scan the artifact
run/jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh scan -f test-project/target/test-project-1.0-SNAPSHOT.jar

# Start the Neo4J Web-UI
run/jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh server
```

Build the __GitHub-Issues__ plugin:
```bash
cd plugin

# Build a fat-JAR
mvn clean package

# Copy the resulting JAR into the jQAssistant CLI plugins folder
cp target/jqa-githubissues-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar ../run/jqassistant-commandline-neo4jv3-1.4.0/plugins/

```
