(ns equipment.lets
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [clojure.tools.build.api :as b])
  (:import
   [java.io File]))

(defonce deps-basis (delay (b/create-basis {:project "deps.edn"})))

(defn build-module-path []
  (str/join File/pathSeparator (keys (:classpath @deps-basis))))

(defn prep-kondo [module-path]
  (try
    (b/process {:command-args ["clj-kondo" "--lint" module-path
                               "--dependencies" "--copy-configs" "--skip-lint"]})
    (catch Throwable err
      (println "error when running clj-kondo! cause:" (:cause (Throwable->map err))))))

(defn prep [{}]
  (let [module-path (build-module-path)]
    (prep-kondo module-path)))

(defn jank
  [{}]
  (println "setting up jank pseudo deps.edn project")
  (let [module-path (build-module-path)]
    (b/process {:command-args ["jank" "--module-path" module-path "run-main" "chera.hello"]})))

(defn jepl
  [{}]
  (let [module-path (build-module-path)]
    (b/process {:command-args ["jank" "--module-path" module-path "repl"]})))

(defn compile-jank
  [{}]
  (let [module-path (build-module-path)]
    (b/process {:command-args ["jank" "--module-path" module-path
                               "compile" "chera.hello"
                               "-o" "hello.exe"]})))

(defn cpp
  [{}]
  (b/process {:command-args ["clang++" "-shared"
                             "-o" "native/libcompress.dll"
                             "-lz" "native/compress.cpp"]}))