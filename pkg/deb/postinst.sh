#!/bin/sh
# Fakeroot and lein don't get along, so we set ownership after the fact.
chown -R root:root /usr/lib/riemann
chown root:root /usr/bin/riemann
chown riemann:riemann /var/log/riemann
chown -R riemann:riemann /etc/riemann
chown root:root /etc/init.d/riemann
