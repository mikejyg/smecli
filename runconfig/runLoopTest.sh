#!/usr/bin/env bash

logfile=loopTest.log
tfile=loopTest.transcript

echo run the script in the 1st session > $logfile
./runCliTest.sh -t $tfile -c . loopTest.script >> $logfile

echo run the script in a sub-session >> $logfile
echo ". loopTest.script" | ./runCliTest.sh -i >> $logfile

echo comparing with golden...
diff golden/$logfile $logfile

echo compare transcript...
diff golden/$tfile $tfile
