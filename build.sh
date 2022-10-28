#!/bin/bash

docker buildx build --platform linux/amd64 -f Dockerfile_build -t jamtur01/riemann_build .
docker run -i -v $PWD/target:/target jamtur01/riemann_build /bin/bash << COMMANDS
cp target/* /target
exit
COMMANDS
docker buildx build --platform linux/amd64 -t=riemannio/riemann .
