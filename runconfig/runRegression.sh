#!/usr/bin/env bash

./build.sh
./runErrExitTest.sh
./runLoopTest.sh
./runNestedScriptTest.sh
./runRemoteTestBit.sh

