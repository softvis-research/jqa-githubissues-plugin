# Remove existing database
rm -r jqassistant

# Scan the test project
run/jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh scan -f test-project/target/test-project-1.0-SNAPSHOT.jar

# Start Neo4J server
run/jqassistant-commandline-neo4jv3-1.4.0/bin/jqassistant.sh server
