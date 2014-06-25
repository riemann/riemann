#!/bin/sh -e
# Fakeroot and lein don't get along, so we set ownership after the fact.
chown -R root:root /usr/share/riemann
chown root:root /usr/bin/riemann
chown riemann:riemann /var/log/riemann
chown -R riemann:riemann /etc/riemann
chown root:root /etc/init.d/riemann

# Start riemann on boot
if [ -x "/etc/init.d/riemann" ]; then
  if [ ! -e "/etc/init/riemann.conf" ]; then
    update-rc.d riemann defaults >/dev/null
  fi
fi
