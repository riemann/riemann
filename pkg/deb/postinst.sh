#!/bin/sh -e

# Start riemann on boot
if [ -x "/etc/init.d/riemann" ]; then
  if [ ! -e "/etc/init/riemann.conf" ]; then
    update-rc.d riemann defaults >/dev/null
  fi
fi

invoke-rc.d riemann start
