# Demonstrate how to add the --as-needed flag for all linked libraries

## Problem
The --as-needed flag is not used by default on GCC.
HOWEVER, some distribution provide GCC with a different [spec](https://gcc.gnu.org/onlinedocs/gcc/Spec-Files.html) (ex: ubuntu, alpine, debian).
To see the spec, use `g++ -dumpspecs` which will show under `link`.
You can also compile a simple application using `g++ --verbose` which will show the full linker commandl line.

You can see through Compiler Explorer the most raw specs:

<a href="https://godbolt.org/#g:!((g:!((g:!((h:codeEditor,i:(filename:'1',fontScale:14,fontUsePx:'0',j:1,lang:c%2B%2B,source:'//+Type+your+code+here,+or+load+an+example.%0Aint+square(int+num)+%7B%0A++++return+num+*+num%3B%0A%7D'),l:'5',n:'0',o:'C%2B%2B+source+%231',t:'0')),k:22.581113203469886,l:'4',n:'0',o:'',s:0,t:'0'),(g:!((h:compiler,i:(compiler:g142,filters:(b:'0',binary:'0',binaryObject:'1',commentOnly:'0',debugCalls:'1',demangle:'0',directives:'0',execute:'1',intel:'0',libraryCode:'0',trim:'1',verboseDemangling:'0'),flagsViewOpen:'1',fontScale:14,fontUsePx:'0',j:1,lang:c%2B%2B,libs:!(),options:'-dumpspecs',overrides:!(),selection:(endColumn:1,endLineNumber:1,positionColumn:1,positionLineNumber:1,selectionStartColumn:1,selectionStartLineNumber:1,startColumn:1,startLineNumber:1),source:1),l:'5',n:'0',o:'+x86-64+gcc+14.2+(Editor+%231)',t:'0')),k:29.254159849350174,l:'4',n:'0',o:'',s:0,t:'0'),(g:!((h:output,i:(compilerName:'x86-64+gcc+14.2',editorid:1,fontScale:14,fontUsePx:'0',j:1,wrap:'1'),l:'5',n:'0',o:'Output+of+x86-64+gcc+14.2+(Compiler+%231)',t:'0')),k:48.16472694717994,l:'4',n:'0',o:'',s:0,t:'0')),l:'2',n:'0',o:'',t:'0')),version:4">Here</a>

Aside from compiler explorer, we only found gcc:5.4 docker image to not provide the `--as-needed` flag.



```
$ docker run --rm -it -v $PWD:/workspace -w /workspace $(docker build -q .)
# ./gradlew assemble
# readelf -d app/build/exe/main/debug/app | grep NEEDED
 0x0000000000000001 (NEEDED)             Shared library: [liblib.so]
 0x0000000000000001 (NEEDED)             Shared library: [libstdc++.so.6]
 0x0000000000000001 (NEEDED)             Shared library: [libgcc_s.so.1]
 0x0000000000000001 (NEEDED)             Shared library: [libc.so.6]
# git apply use-plugin.patch
# ./gradlew assemble
# readelf -d app/build/exe/main/debug/app | grep NEEDED
 0x0000000000000001 (NEEDED)             Shared library: [libstdc++.so.6]
 0x0000000000000001 (NEEDED)             Shared library: [libgcc_s.so.1]
 0x0000000000000001 (NEEDED)             Shared library: [libc.so.6]
```


Running vanilla GCC using compiler explorer:
```
$ docker run --rm -it --platform linux/amd64 ubuntu:latest
# apt update && apt install -y make git curl python3 xz-utils
# apt install gcc-multilib # musl-dev on alpine
> 2 # America
> 102 # Montreal
# git clone https://github.com/compiler-explorer/infra.git && cd infra
# make ce
# ./bin/ce_install list # choose compiler
# ./bin/ce_install install compilers/c++/x86/gcc 14.2.0

# vim main.cpp
# /opt/compiler-explorer/gcc-14.2.0/bin/gcc main.cpp -lm -lcrypt
# readelf -d a.out | grep NEEDED
 0x0000000000000001 (NEEDED)             Shared library: [libm.so.6]
 0x0000000000000001 (NEEDED)             Shared library: [libcrypt.so.1]
 0x0000000000000001 (NEEDED)             Shared library: [libc.so.6]
# /opt/compiler-explorer/gcc-14.2.0/bin/gcc main.cpp -Wl,--as-needed -lm -lcrypt
# readelf -d a.out | grep NEEDED
 0x0000000000000001 (NEEDED)             Shared library: [libc.so.6]
```