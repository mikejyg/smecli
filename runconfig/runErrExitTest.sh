#!/usr/bin/env bash

logfile=errExitTest.out

rm $logfile

./runCliTest.sh . errExitTest.script | tee $logfile
echo compare with golden...
diff golden/$logfile $logfile
