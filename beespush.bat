@echo off
call play dist
call bees app:deploy -Rjava_version=1.7 -a aego -t play2 -P config.resource=dist.cloudbees.conf -P file.separator=\/ dist\aego-play-1.0-SNAPSHOT.zip
call bees app:update aego disableProxyBuffering=true
rem bees config:set -a aego config.resource=dist.cloudbees.conf
