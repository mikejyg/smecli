#!/usr/bin/env bash

logfile=loopTest.log
./runCliTest.sh . loopTest.script > $logfile

echo comparing with golden...
diff golden/$logfile $logfile
