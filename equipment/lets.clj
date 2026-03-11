(ns equipment.lets
  (:require
   [babashka.fs :as fs]
   [clojure.string :as str]
   [clojure.tools.build.api :as b])
  (:import
   [java.io File]))

(defonce deps-basis (delay (b/create-basis {:project "deps.edn"})))

;; adapted from https://blog.agical.se/en/posts/how-to-create-a-really-simple-clojureclr-dependency-tool/
(defn unpack-jar [jar-path prep?]
  (println "unpacking" jar-path)
  (let [deps-subdir (-> (fs/file-name jar-path)
                        (str/replace #".jar$" "")
                        (str/replace #"\." "-"))
        deps-dir    (fs/path "C:\\app" "dependencies" deps-subdir)]
    (when prep?
      (fs/create-dirs deps-dir)
      (fs/unzip jar-path deps-dir {:replace-existing true}))
    deps-dir))

(defn process-modules [prep?]
  (comp
   (remove (fn [[module-path _coord]] (re-matches #"clojure-1\..*\.jar" (fs/file-name module-path))))
   (map (fn [[module-path coord]]
          (println coord "=>" module-path)
          (if (str/ends-with? module-path ".jar")
            (unpack-jar module-path prep?)
            module-path)))))

(defn jank
  {:org.babashka/cli {:exec-args {:prep? false}
                      :coerce {:prep? :bool}}}
  [{:keys [prep?] :as opts}]
  (println "setting up jank pseudo deps.edn project\n  with ops:" opts)
  (let [classpath (into [] (process-modules prep?) (:classpath @deps-basis))
        LOAD_PATH (str/join File/pathSeparator classpath)]
    (when prep?
      (try
        (b/process {:command-args ["clj-kondo" "--lint" LOAD_PATH
                                   "--dependencies" "--copy-configs" "--skip-lint"]})
        (catch Throwable err
          (println "error when running clj-kondo! cause:" (:cause (Throwable->map err))))))
    (println "running jank!" LOAD_PATH)
    (b/process {:command-args ["jank" "--module-path" LOAD_PATH "run-main" "chera.hello"]})))
