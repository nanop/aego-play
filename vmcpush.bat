@echo off
rem play -Dconfig.file=conf/dist.cloudfoundry.conf dist 
call cfdist.bat
call vmc push aego --path=dist\aego-play-1.0-SNAPSHOT.zip 
