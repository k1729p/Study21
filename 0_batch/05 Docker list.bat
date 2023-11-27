@echo off
echo Docker networks:
docker network ls
echo.
echo Docker network 'app-net':
docker network inspect app-net
echo.
echo Docker containers:
docker ps --no-trunc --format "table {{.Names}}\t{{.Command}}"
echo.
docker ps --format "table {{.Names}}\t{{.Ports}}\t{{.Networks}}"
pause