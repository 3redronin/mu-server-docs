#!/usr/bin/env bash
set -e
set -x


if [[ -f 'pom.xml' ]];
then

  scp deploy.sh ubuntu@muserver.io:~
#  mvn -B releaser:release
  mvn clean package
  scp target/docs-*.jar ubuntu@muserver.io:~
  ssh ubuntu@muserver.io "bash deploy.sh $(basename target/docs-*.jar)"

else

  sudo iptables -A PREROUTING -t nat -p tcp --dport 80 -j REDIRECT --to-port 8080
  sudo iptables -A PREROUTING -t nat -p tcp --dport 443 -j REDIRECT --to-port 8443

  ls -la
  pkill 'java' || echo "Nothing killed"

  java -version
  nohup java -jar $1 &

  tail -f logs/mudocs.log

fi
