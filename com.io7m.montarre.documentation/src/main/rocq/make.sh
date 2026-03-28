#!/bin/sh -ex

rocq compile \
  -Q rentomos/src/main/coq com.io7m.entomos \
  -Q roctetorder/src/main/coq com.io7m.octetorder \
  -Q Montarre Montarre \
  Montarre/Package.v

mkdir -p html

rocq doc -Q Montarre Montarre --utf8 -d html Montarre/*.v
