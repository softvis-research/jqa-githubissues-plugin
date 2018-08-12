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

## Use Cases

### Overview

List all your open Issues over multiple repositories:

```cypher
MATCH
    (r:Repository)-[:HAS_ISSUE]->(i:Issue {state:"open"})
RETURN
    r.repositoryId, i.title, i.body
```

Count open Issues per repository:

```cypher
MATCH
    (r:Repository)-[:HAS_ISSUE]->(Issue {state:"open"})
RETURN
    r.repositoryId, count(*) AS issueCount
ORDER BY
    issueCount DESC
```

List open issues per user:

```cypher
MATCH
    (Issue {state:"open"})-[:HAS_ASSIGNEE]->(u:User)
RETURN
    u.login, count(*)
```

### Issue quality

Show issues without description:

```cypher
MATCH
    (i:Issue)
WHERE
    i.body = ""
RETURN
    i.issueId, i.title
```

Show issues without labels:

```cypher
MATCH 
    (i:Issue)
WHERE 
    NOT (i:Issue)-[:HAS_LABEL]->()
RETURN
    i.title, i.issueId
```

Show issues ordered descanding by the amount of coments:
```cypher
MATCH 
    path=((i:Issue)-[:HAS_COMMENT]->()-[:FOLLOWED_BY*]->())
RETURN
    i.title, i.issueId, length(path) AS pathLength, i.state
ORDER BY
    pathLength DESC, i.state DESC
```

## Development

```bash
# cd .
rm -r jqassistant
run/jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh scan -f test-project/target/test-project-1.0-SNAPSHOT.jar

run/jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh server

# cd plugin
mvn clean package

cp target/jqa-githubissues-plugin-1.0-SNAPSHOT-jar-with-dependencies.jar ../run/jqassistant-commandline-neo4jv3-1.4.0/plugins/

```
