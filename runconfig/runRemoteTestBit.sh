#!/usr/bin/env bash

logfile=remoteTestBit.log

rm $logfile
./runRemoteTest.sh -b | tee $logfile

echo comparing with golden...
diff golden/$logfile $logfile

