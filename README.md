# smecli
Simple modular extendable console interface

## Design Goals

1. To provide a simple piece of software that can be easily used to add a text console to a program.

2. To be flexible enough so that it can accommodate most command line styles and argument types, e.g. a JSON string.

3. To provide an inter process communication mechanism, for reuse of programs across different languages, platforms, GUIs, etc.

4. Efficiency over doing it all.

5. Not restricting in any way, users of this to write their own code.  

## Dependencies

1. The testing code uses mikejyg/cloep (https://github.com/mikejyg/cloep) project for command line options parsing. The cloep directory needs to be in the same dir as smecli.
