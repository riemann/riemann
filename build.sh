#!/bin/bash

docker build -f Dockerfile_build -t jamtur01/riemann_build .
docker run -i -v $PWD/target:/target jamtur01/riemann_build /bin/bash << COMMANDS
cp target/* /target
exit
COMMANDS
