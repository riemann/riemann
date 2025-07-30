#!/bin/sh -e

action="$1"

if [ "$action" = remove ]; then
	systemctl --no-reload disable riemann.service
	systemctl stop riemann.service
fi
