#!/bin/bash -e

cp /var/gerrit/etc/gerrit.config.orig /var/gerrit/etc/gerrit.config
cp /var/gerrit/etc/gc.config.orig /var/gerrit/etc/gc.config
cp /var/gerrit/etc/log4j2.xml.orig /var/gerrit/etc/log4j2.xml

if [[ ! -f /var/gerrit/etc/ssh_host_ed25519_key ]]
then
  echo "Initializing Gerrit site ..."
  java -jar /var/gerrit/bin/gerrit.war init --dev -d /var/gerrit --batch
fi

echo "Reindexing Gerrit ..."
java -jar /var/gerrit/bin/gerrit.war reindex -d /var/gerrit

touch /var/gerrit/logs/{gc_log,error_log,httpd_log,sshd_log,replication_log} && tail -f /var/gerrit/logs/* | grep --line-buffered -v 'HEAD /' &

echo "Running gc-executor ..."
java -DconfigFile=/var/gerrit/etc/gc.config -jar /var/gerrit/plugins/gc-executor.jar --console-log &

echo "Running Gerrit ..."
java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 /var/gerrit/bin/gerrit.war daemon -d /var/gerrit
