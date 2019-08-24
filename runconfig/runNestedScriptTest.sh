#!/usr/bin/env bash

logfile=nestedScriptTest.log
./runCliTest.sh -c . nestedScriptTest.script > $logfile

echo comparing with golden...
diff golden/$logfile $logfile
