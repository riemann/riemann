#!/bin/sh -e

if [ "$1" = "purge" ] ; then
  update-rc.d riemann remove >/dev/null
fi
