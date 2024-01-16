@echo off
docker container rm --force pulsar
docker network rm --force app-net
docker volume rm --force pulsardata pulsarconf

docker network create app-net
docker pull apachepulsar/pulsar:latest

docker run  ^
 --interactive ^
 --tty ^
 --publish 6650:6650 ^
 --publish 8080:8080 ^
 --name pulsar ^
 --net=app-net ^
 --mount source=pulsardata,target=/pulsar/data ^
 --mount source=pulsarconf,target=/pulsar/conf ^
 apachepulsar/pulsar:latest ^
 bin/pulsar ^
 standalone
pause