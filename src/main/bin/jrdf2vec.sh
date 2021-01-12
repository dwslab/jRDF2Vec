# Java commands for starting the jar
java_commands=-Xmx10G

# jrdf2vec jar location (only refer to the directory where the JAR resides e.g. target directory)
jrdf2vec_home=""

#------------------------------------------------------------

jrdf2vec_executable="${jrdf2vec_home}/jrdf2vec-1.1-SNAPSHOT.jar"

java -jar "$jrdf2vec_executable" "$@"
