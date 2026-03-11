# journey to the jankier clojure

1. starting point:
https://github.com/ikappaki/jank-win-release

```
curl -fsSL https://github.com/ikappaki/jank-win-release/raw/refs/heads/main/jank-win-updater | bash
```

2. add ~/.local/bin to PATH in MSYS2CLANG64's bash_profile

3. run jank-win-updater, error!

```sh
jank-win-updater --all
# [...some log truncated]
#
# -bash: clang: command not found
```

4. install clang 

```sh
pacman -Syu
pacman -S mingw-w64-clang-x86_64-clang
```

5. run jank-win-updater, ok!

```sh
jank-win-updater --all
```

6. add  C:\msys64\clang64\bin\ to Windows' System PATH

7. jank!

```sh
jank repl
# Note: Looks like your first run with these flags. Building pre-compiled header… done!
# user=> jank nREPL server is running on nrepl://127.0.0.1:65181
# (println "hello jank!")
# hello jank!
# nil
```

8. to the book!

the following took me a while. I made a hello.jank at src/chera and run...

```sh
# make a hello.jank at src/chera
jank --module-path src run-main
# error: Please provide the run-main command a module.
# huh?

jank --module-path "src" run-main
# error: Please provide the run-main command a module.

jank --module-path 'src' run-main
# error: Please provide the run-main command a module.

# in cmd instead of powershell
jank --module-path "src" run-main
# error: Please provide the run-main command a module.

jank run-main --module-path 'src' 
# error: Please provide the run-main command a module.

jank --module-path "[full-path]/src" run-main
# error: Please provide the run-main command a module.

# looking at source code... https://github.com/ikappaki/jank-win/blob/9ff68df86b8b01dfd3444e18810475ff3a130429/compiler%2Bruntime/src/cpp/jank/util/cli.cpp#L89 👀
# error from positional argument?
jank --module-path "[full-path]/src" run-main src
# ─ runtime/module-not-found ─────────────────────────────────────────────────────────────────────────
# error: Unable to find module 'src'.
# oh!

jank --module-path src run-main chera.hello
# hello jank!
```

ohhh, module = namespace?

# aot compile

```sh
jank --module-path src compile chera.hello -o hello.exe
.\hello.exe
# hello jank!
```

# lets jank!

applying insights from our clojure-clr journey https://github.com/keychera/clojureclr-playground

making a pseudo deps.edn project

```sh
# prepare our bbest (tools-bbin + bb.edn + equipment.lets), write some deps.edn, and voila
bb letsgo
# setting up jank pseudo deps.edn project
#   with ops: {:prep? false}
# running jank!
# hello jank!
```

# lets odoyle!

a typeless endeavor

also, jank error is so cool

```sh
─ parse/invalid-keyword ────────────────────────────────────────────────────────────────────────────
error: Unable to resolve namespace alias 's'                                                        

─────┬──────────────────────────────────────────────────────────────────────────────────────────────
     │ ...\odoyle-rules\src\odoyle\rules.cljc
─────┼──────────────────────────────────────────────────────────────────────────────────────────────
 47  │ (defn parse [spec content]
 48  │   #_(let [res (s/conform spec content)]
 49  │     (if (= ::s/invalid res)
     │            ^^^^^^^^^^^ Found here.


─ analyze/invalid-fn-parameters ────────────────────────────────────────────────────────────────────
error: This function has too many parameters. The max is 10.                                        

─────┬──────────────────────────────────────────────────────────────────────────────────────────────
     │ ...\odoyle-rules\src\odoyle\rules.cljc
─────┼──────────────────────────────────────────────────────────────────────────────────────────────
 97  │              facts ;; map of id -> (map of attr -> Fact)
 98  │              ))
 99  │ (defn ->MemoryNode [id
     │                    ^^ Found here.
     │ ^ Expanded from this macro.
100  │                     parent-id ;; JoinNode id
101  │                     child-id ;; JoinNode id
102  │                     leaf-node-id ;; id of the MemoryNode at the end (same as id if this is the leaf node)
103  │                     condition ;; Condition associated with this node
104  │                     matches ;; map of id+attrs -> Match
105  │                     what-fn ;; fn
106  │                     when-fn ;; fn
107  │                     then-fn ;; fn
108  │                     then-finally-fn ;; fn
109  │                     trigger ;; boolean indicating that the :then block can be triggered
110  │                     ]



libunwind:      pc not in table, pc=0x1D8BA132837

```

need more hammock, odoyle rely so much on spec/conform for ruleset macro
