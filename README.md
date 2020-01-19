# jQAssistant GitHub-Issues Plugin

[![GitHub license](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://github.com/softvis-research/jqa-githubissues-plugin/blob/master/LICENSE)
[![Build Status](https://travis-ci.com/softvis-research/jqa-githubissues-plugin.svg?branch=master)](https://travis-ci.com/softvis-research/jqa-githubissues-plugin)
[![codecov](https://codecov.io/gh/softvis-research/jqa-githubissues-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/softvis-research/jqa-githubissues-plugin)

This is a [GitHub](https://github.com/) issue scanner for [jQAssistant](https://jqassistant.org/). 
It enables jQAssistant to scan and analyze [GitHub](https://github.com/) issues.

## Getting Started

Download the jQAssistant command line tool for your system: [jQAssistant - Get Started](https://jqassistant.org/get-started/).

Next download the latest version from the release tab. Put the `jqa-githubissues-plugin-*.jar` into the plugins folder 
of the jQAssistant commandline tool.

Create a file named `githubissues.xml`. 
The plugin can scan multiple repositories owned by different users. Please note that
the [GitHub REST-API](https://developer.github.com/v3/) requires login credentials to
access any of its functions. Therefore, login credentials must be provided per 
repository.

```xml
<github-issues-configuration>
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
</github-issues-configuration>
```

Now scan your configuration and wait for the plugin to finish:

```bash
jqassistant.sh scan -f githubissues.xml
```

You can then start a local Neo4j server to start querying the database at [http://localhost:7474](http://localhost:7474):

```bash
jqassistant.sh server
```

## Labels

The __GitHub-Issues__ plugin uses the following labels in the resulting graph:

| Label | Description                                                  | ID |
| ----- | ------------------------------------------------------------ |----|
|GitHub-Issues-Configuration-File|A configuration file for the plugin.| - |
|GitHub |Parent label for all nodes related to the GitHub-Issues plugin.| -|
|Repository|Represents a GitHub Repository.| "repo-user/repo-name"|
|Issue|Represents a GitHub Issue.| "repo-user/repo-name#issue-number" |
|Milestone|Represents a GitHub Milestone which is a collection of Issues. | "repo-user/repo-name#milestone-id" |
|Comment|Represents a Comment under a GitHub Issue.| - |
|PullRequest|Every PullRequest is an Issue, but not every Issue is a PullRequest.| "repo-user/repo-name#issue-number" |
|User|Represents a GitHub User.| "user-name" |
|Commit|Represents a GitHub Commit.| "repo-user/repo-name#commit-sha" |

Here are the possible relations between those labels:

```java
(GitHub-Issues-Configuration-File)  -[:SPECIFIES_REPOSITORY]    ->  (Repository)

(Repository)    -[:HAS_ISSUE]         ->    (Issue)
(Repository)    -[:HAS_MILESTONE]     ->    (Milestone)

(Issue)         -[:HAS_LABEL]         ->    (Label)
(Issue)         -[:HAS_COMMENT]       ->    (Comment)
(Issue)         -[:HAS_ASSIGNEE]      ->    (User)
(Issue)         -[:CREATED_BY]        ->    (User)
(Issue)         -[:IS_PART_OF]        ->    (Milestone)

(PullRequest)   -[:HAS_LAST_COMMIT]   ->    (Commit)

(Milestone)     -[:CREATED_BY]        ->    (User)

(Comment)       -[:FOLLOWED_BY]       ->    (Comment)
(Comment)       -[:CREATED_BY]        ->    (User)

(Issue|Comment) -[:REFERENCES_ISSUE]  ->    (Issue)
(Issue|Comment) -[:REFERENCES_COMMIT] ->    (Commit)
(Issue|Comment) -[:REFERENCES_USER]   ->    (User)
```

## Use Cases

### Overview

List all your open Issues over multiple repositories:

```java
MATCH
    (r:Repository)-[:HAS_ISSUE]->(i:Issue {state:"open"})
RETURN
    r.repositoryId, i.title, i.body
```

Count open Issues per repository:

```java
MATCH
    (r:Repository)-[:HAS_ISSUE]->(Issue {state:"open"})
RETURN
    r.repositoryId, count(*) AS issueCount
ORDER BY
    issueCount DESC
```

List open issues per user:

```java
MATCH
    (Issue {state:"open"})-[:HAS_ASSIGNEE]->(u:User)
RETURN
    u.login, count(*)
```

### Issue quality

Show issues without description:

```java
MATCH
    (i:Issue)
WHERE
    i.body = ""
RETURN
    i.issueId, i.title
```

Show issues without labels:

```java
MATCH 
    (i:Issue)
WHERE 
    NOT (i:Issue)-[:HAS_LABEL]->()
RETURN
    i.title, i.issueId
```

Show issues ordered descending by the amount of comments:
```java
MATCH 
    path=((i:Issue)-[:HAS_COMMENT]->()-[:FOLLOWED_BY*]->())
RETURN
    i.title, i.issueId, length(path) AS pathLength, i.state
ORDER BY
    pathLength DESC, i.state DESC
```

Show durations it needed to resolve an issue:
```java
WITH
    issue, duration.inDays(date(issue.createdAt), date(issue.updatedAt)).days AS duration
RETURN 
    issue.issueId, issue.title, duration + " days" AS timToSolve
ORDER BY
    duration DESC
```

Show issues older than 1 month that are still open:
```java
MATCH
    (issue:Issue {state:"open"})
WHERE
    date(issue.createdAt) <= date('20180713')
RETURN 
    *
```

#### Why are these issues still open?

Let's have a look at a few indicators:

- Do these Issues have labels?
```java
MATCH
    (issue:Issue {state:"open"})
WHERE
    date(issue.createdAt) <= date('20180713') AND NOT (issue:Issue)-[:HAS_LABEL]->()
RETURN 
    *
```
&rarr; If not, then probably no one looked at these issues.

- Is anyone assigned to this issue?
```java
MATCH
    (issue:Issue {state:"open"})
WHERE
    date(issue.createdAt) <= date('20180713') AND NOT (issue:Issue)-[:HAS_ASSIGNEE]->(:User)
RETURN 
    issue
```
&rarr; If not, then probably no one feels responsible for this issue.

## Known Problems

### Performance

The performance of the plugin is limited by the GitHub REST API. We are not allowed to make more than one request
per second. See [this](https://developer.github.com/v3/guides/best-practices-for-integrators/#dealing-with-abuse-rate-limits)
for more information. For each __Issue__ and every __Comment__ the body text gets parsed to HTML by 
[this endpoint](https://developer.github.com/v3/markdown/#render-an-arbitrary-markdown-document).
We need to do this to resolve references to __User__, __Commits__ and __Commits__ in the markdown texts.

That is why a analysis of ~1800 issues can take a few hours.

### Only one configuration file

At the moment only one configuration file is supported. When you scan more than one at a time nodes representing the
same real world entity won't be identified. Furthermore, the plugin doesn't print a warning so be careful
to avoid wrong analyses of your repositories!

### Did you find a bug?

Please have a look at the issue section in GitHub. If you can't find your bug open a ticket
with an reproducible example and your error logs.

## Contribution

If you want to contribute here are a few tips to get you started:

Build the __GitHub-Issues__ plugin:

```bash
cd plugin

# Build a fat-JAR
mvn clean package

# Copy the resulting JAR into the jQAssistant CLI plugins folder
cp target/jqa-githubissues-plugin-0.1-jar-with-dependencies.jar ../run/jqassistant-commandline-neo4jv3-1.4.0/plugins/
```
Run code coverage via [Corbertura](http://cobertura.github.io/cobertura/):

```bash
mvn cobertura:cobertura
```

The coverage reports can be found under `target/site/cobertura`.
