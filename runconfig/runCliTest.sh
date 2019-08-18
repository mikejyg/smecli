cd ..
./gradlew build
cd runconfig
java -cp ../build/libs/smecli.jar:../build/classes/java/test mikejyg.smecli.CliTest "$@"
