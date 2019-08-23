#!/usr/bin/env bash

logfile=remoteTestBit.log

rm $logfile
./runRemoteTest.sh -b > $logfile

echo comparing with golden...
diff golden/$logfile $logfile

