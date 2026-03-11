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
