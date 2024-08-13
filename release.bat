set JAVA_HOME="C:\tools\java\jdk-21.0.1"

REM Dont forget to update version in README
set new_version=1.0.7

call mvnw clean package

call mvnw versions:set -DnewVersion=%new_version%
call mvnw versions:commit
call mvnw clean deploy -DskipTests

REM Commit and push Release info