#!/bin/sh -e
# Fakeroot and lein don't get along, so we set ownership after the fact.
chown -R root:root /usr/share/riemann
chown root:root /usr/bin/riemann
chown riemann:riemann /var/log/riemann
chown -R riemann:riemann /etc/riemann
chown root:root /etc/default/riemann

action="$1"

if [ "$action" = configure ]; then
	old_version="$2"

	if [ -z "$old_version" ]; then
		systemctl enable riemann.service
	else
		systemctl try-restart riemann.service
	fi
fi
