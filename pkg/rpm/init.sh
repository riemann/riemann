#! /bin/bash
### BEGIN INIT INFO
# Provides:          riemann
# Required-Start:    $all
# Required-Stop:     $all
# Default-Start:
# Default-Stop:      0 1 6
# Short-Description: Starts Riemann
# chkconfig: - 80 15
# Description: Riemann event monitoring server.
### END INIT INFO

# Source function library.
. /etc/rc.d/init.d/functions

# Pull in sysconfig settings
[ -f /etc/sysconfig/riemann ] && . /etc/sysconfig/riemann

RIEMANN_USER=riemann

DAEMON=/usr/bin/riemann
NAME=riemann
PID_FILE=${PIDFILE:-/var/run/${NAME}.pid}
LOCK_FILE=${LOCKFILE:-/var/lock/subsys/${NAME}}
NFILES=${NFILES:-32768}

RIEMANN_PATH_CONF=${RIEMANN_PATH_CONF:-/etc/${NAME}}
RIEMANN_CONFIG=${RIEMANN_CONFIG:-${RIEMANN_PATH_CONF}/riemann.config}

DAEMON_OPTS="${RIEMANN_OPTS} ${RIEMANN_CONFIG}"

start() {
    echo -n $"Starting ${NAME}: "
    ulimit -n $NFILES
#    daemon --pidfile $PID_FILE --user $RIEMANN_USER $DAEMON $DAEMON_OPTS
    daemonize -u $RIEMANN_USER -p $PID_FILE -l $LOCK_FILE $DAEMON $DAEMON_OPTS
    RETVAL=$?
    [ $RETVAL -eq 0 ] && touch $LOCK_FILE
    [ $RETVAL -eq 0 ] && success || failure
    echo
    return $RETVAL
}

reload() {
    echo -n $"Reloading ${NAME}: "
    killproc -p ${PID_FILE} $DAEMON -1
    RETVAL=$?
    echo
    return $RETVAL
}

stop() {
    echo -n $"Stopping ${NAME}: "
    killproc -p ${PID_FILE} -d 10 $DAEMON
    RETVAL=$?
    echo
    [ $RETVAL = 0 ] && rm -f ${LOCK_FILE} ${PID_FILE}
    return $RETVAL
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    status)
        status -p ${PID_FILE} $DAEMON
        RETVAL=$?
        ;;
    reload|force-reload)
        reload
        ;;
    restart)
        stop
        start
        ;;
    *)
        N=/etc/init.d/${NAME}
        echo "Usage: $N {start|stop|status|restart|force-reload}" >&2
        RETVAL=2
        ;;
esac

exit $RETVAL
