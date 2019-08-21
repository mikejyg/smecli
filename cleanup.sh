#!/usr/bin/env bash

pushd $(dirname $0)
rm cliTest.out
cd runconfig
rm *.out *.log
popd
