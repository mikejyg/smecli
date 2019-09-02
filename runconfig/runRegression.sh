#!/usr/bin/env bash

echo build.sh...
./build.sh
echo
echo runErrExitTest.sh...
./runErrExitTest.sh
echo
echo runLoopTest.sh
./runLoopTest.sh
echo
echo runNestedScriptTest.sh
./runNestedScriptTest.sh
echo
echo runRemoteTestBit.sh
./runRemoteTestBit.sh

