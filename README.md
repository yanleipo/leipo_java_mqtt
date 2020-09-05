1 Create a Maven project
mvn archetype:generate -DgroupId=com.ylpsingapore.app -DartifactId=my-app -DarchetypeArtifactId=maven-archetype-quickstart -DarchetypeVersion=1.4 -DinteractiveMode=false

2 Compile the project
mvn package

3 Run the application
java -cp target/my-app-1.0-SNAPSHOT.jar com.ylpsingapore.app.TestMQTT
CLASSPATH=target/my-app-1.0-SNAPSHOT.jar:lib/* java com.ylpsingapore.app.TestMQTT -cip ylptest.ddns.net:8883 -cu default -cp=default
mvn exec:java -Dexec.mainClass="com.ylpsingapore.app.TestMQTT" -Dexec.args="-cip ylptest.ddns.net:8883 -cu default -cp default"
