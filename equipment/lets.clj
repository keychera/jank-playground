(ns equipment.lets
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [clojure.tools.build.api :as b])
  (:import
   [java.io File]))

(defonce deps-basis (delay (b/create-basis {:project "deps.edn"})))

;; adapted from https://blog.agical.se/en/posts/how-to-create-a-really-simple-clojureclr-dependency-tool/
(defn unpack-jar [jar-path]
  (println "unpacking" jar-path)
  (let [deps-subdir (str/replace (fs/file-name jar-path) #".jar$" "")
        deps-dir    (str "/app/dependencies/" deps-subdir)]
    (fs/create-dirs deps-dir)
    (fs/unzip jar-path deps-dir {:replace-existing true})
    deps-dir))

(defn jank
  {:org.babashka/cli {:exec-args {:prep? false}
                      :coerce {:prep? :bool}}}
  [{:keys [prep?] :as opts}]
  (println "setting up jank pseudo deps.edn project\n  with ops:" opts)
  (let [classpath (into []
                        (map (if prep?
                               (fn [path]
                                 (if (str/ends-with? path ".jar")
                                   (unpack-jar path)
                                   path))
                               identity))
                        (keys (:classpath @deps-basis)))
        LOAD_PATH (str/join File/pathSeparator classpath)]
    (when prep?
      (try
        (b/process {:command-args ["clj-kondo" "--lint" LOAD_PATH
                                   "--dependencies" "--copy-configs" "--skip-lint"]})
        (catch Throwable err
          (println "error when running clj-kondo! cause:" (:cause (Throwable->map err))))))
    (println "running jank!")
    (b/process {:env {"CLOJURE_LOAD_PATH" LOAD_PATH}
                :command-args ["jank" "--module-path" LOAD_PATH "run-main" "chera.hello"]})))
