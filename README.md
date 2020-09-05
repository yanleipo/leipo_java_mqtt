1 Create a Maven project
mvn archetype:generate -DgroupId=com.ylpsingapore.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

2 Compile the project
mvn package

3 Run the application
CLASSPATH=target/my-app-1.0-SNAPSHOT.jar:lib/* java com.ylpsingapore.app.MQTTSample -a subscribe  -b ylptest.ddns.net -p 8883  -w solace1 -v true -r /Users/leipoyan/Downloads/keep/certs2/MyRootCaCert.jks -s 1
CLASSPATH=target/my-app-1.0-SNAPSHOT.jar:lib/* java com.ylpsingapore.app.MQTTSample  -b ylptest.ddns.net -p 8883  -w solace1 -v true -r /Users/leipoyan/Downloads/keep/certs2/MyRootCaCert.jks -s 1

CLASSPATH=target/my-app-1.0-SNAPSHOT.jar:lib/* java com.ylpsingapore.app.TestMQTT -cip ylptest.ddns.net:8883 -cu default -cp=default
mvn exec:java -Dexec.mainClass="com.ylpsingapore.app.TestMQTT" -Dexec.args="-cip ylptest.ddns.net:8883 -cu default -cp default"
