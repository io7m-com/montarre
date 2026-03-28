#!/bin/sh

exec rocqide \
  -Q rentomos/src/main/coq com.io7m.entomos \
  -Q roctetorder/src/main/coq com.io7m.octetorder \
  -Q Montarre Montarre \
  "$@"
