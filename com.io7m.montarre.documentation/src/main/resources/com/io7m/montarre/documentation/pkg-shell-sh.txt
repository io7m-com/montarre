#!/bin/sh
# Auto generated: Do not edit.
# This is a launch script for UNIX-like platforms.

if [ -z "${MONTARRE_HOME}" ]
then
  echo "MONTARRE_HOME is unset" 1>&2
  exit 1
fi

#
# Check that the available Java runtime is suitable.
#

/usr/bin/env java -jar "${MONTARRE_HOME}/bin/launch.jar" \
  check-java-version 21 || exit 1

#
# Build a module path. This is guaranteed to be:
#   ${MONTARRE_HOME}/lib
#   ${MONTARRE_HOME}/lib/${ARCH}/${OS}
#

MONTARRE_MODULE_PATH=$(/usr/bin/env java -jar "${MONTARRE_HOME}/bin/launch.jar" \
  get-module-path "${MONTARRE_HOME}") || exit 1

#
# Run the application.
#

exec /usr/bin/env java \
  -p "${MONTARRE_MODULE_PATH}" \
  -m com.io7m.montarre.cmdline/com.io7m.montarre.cmdline.MMain \
  "$@"

