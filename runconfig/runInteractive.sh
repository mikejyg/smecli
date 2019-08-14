pushd ..
./gradlew build
popd
java -cp ../build/libs/smecli.jar:../build/classes/java/test mikejyg.smecli.CliTest
