#!/bin/sh -ex

coqc -Q Montarre Montarre Montarre/Package.v

mkdir -p html

coqdoc -Q Montarre Montarre --utf8 -d html Montarre/*.v
