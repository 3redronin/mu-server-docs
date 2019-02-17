#!/usr/bin/env bash

set -e
set -x


if [[ -f 'pom.xml' ]];
then

  scp deploy.sh ubuntu@18.236.152.164:~
  mvn -B releaser:release
  scp target/docs-*.jar ubuntu@18.236.152.164:~
  ssh ubuntu@18.236.152.164 'deploy.sh'

else

  ls -la

fi
