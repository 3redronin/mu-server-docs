#!/usr/bin/env bash

set -e
set -x


if [[ -f 'pom.xml' ]];
then

  scp deploy.sh ubuntu@muserver.io:~
#  mvn -B releaser:release
  mvn clean package
  scp target/docs-*.jar ubuntu@muserver.io:~
  ssh ubuntu@muserver.io 'bash deploy.sh'

else

  ls -la
  pkill -9 -f 'docs' || echo "Nothing killed"

  nohup java -jar docs*.jar &

fi
