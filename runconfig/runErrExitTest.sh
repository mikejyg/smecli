#!/usr/bin/env bash

logfile=errExitTest.out

rm $logfile

./runCliTest.sh . errExitTest.script > $logfile
echo comparing with golden...
diff golden/$logfile $logfile
