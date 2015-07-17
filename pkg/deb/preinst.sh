#!/bin/sh -e
# Create riemann user and group
USERNAME="riemann"
GROUPNAME="riemann"
getent group "$GROUPNAME" >/dev/null || groupadd -r "$GROUPNAME"
getent passwd "$USERNAME" >/dev/null || \
  useradd -r -g "$GROUPNAME" -d /usr/share/riemann -s /bin/false \
  -c "Riemann monitoring system" "$USERNAME"
exit 0
