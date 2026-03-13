(ns equipment.lets
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b])
  (:import
   [java.io File]))

(defonce deps-basis (delay (b/create-basis {:project "deps.edn"})))

(defn log [& stuff]
  (apply println "[chera@jank]" stuff))

(defn ->jank-deps-edn []
  (let [basis @deps-basis]
    {:module-path  (str/join File/pathSeparator (keys (:classpath basis)))
     :include-dirs (-> basis :jank :include-dirs)}))

(defn jank-command [jank-deps-edn command {:keys [main-module extra]}]
  (let [{:keys [module-path include-dirs]} jank-deps-edn
        includes (into []
                       (mapcat (fn [inc-dir] ["-I" inc-dir "-L" inc-dir]))
                       include-dirs)]
    (into []
          (remove nil?)
          (concat
           ["jank" "--module-path" module-path]
           includes
           [command main-module]
           extra))))

(defn prep-kondo [classpath #_is-module-path]
  (try
    (b/process {:command-args ["clj-kondo" "--lint" classpath
                               "--dependencies" "--copy-configs" "--skip-lint"]})
    (catch Throwable err
      (println "error when running clj-kondo! cause:" (:cause (Throwable->map err))))))

(defn prep [{}]
  (let [{:keys [module-path]} (->jank-deps-edn)]
    (prep-kondo module-path)))

(defn jank
  {:org.babashka/cli {:exec-args {:args nil}}}
  [{:keys [args]}]
  (println "setting up jank pseudo deps.edn project" args)
  (let [jedn (->jank-deps-edn)
        jcmd (jank-command jedn "run-main" {:main-module "chera.hello"
                                            :extra ["-l" "compress" "--" args]})]
    (log jcmd)
    (b/process {:command-args jcmd})))

(defn jepl
  [{}]
  (let [jedn (->jank-deps-edn)
        jcmd (jank-command jedn "repl" {})]
    (log jcmd)
    (b/process {:command-args jcmd})))

(defn compile-jank
  [{}]
  (let [jedn (->jank-deps-edn)
        jcmd (jank-command jedn "compile" {:main-module "chera.hello"
                                           :extra ["-o" "hello.exe"]})]
    (log jcmd)
    (b/process {:command-args jcmd})))

(defn cpp
  [{}]
  (b/process {:command-args ["clang++" "-shared"
                             "-o" "native/libcompress.dll"
                             "-lz" "native/compress.cpp"]}))

(defn check [& _]
  (prn @deps-basis))
