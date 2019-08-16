#!/usr/bin/env bash
./runCliTest.sh . nestedScriptTest.script | tee nestedScriptTest.log

echo comparing with golden...
diff golden/nestedScriptTest.log nestedScriptTest.log
