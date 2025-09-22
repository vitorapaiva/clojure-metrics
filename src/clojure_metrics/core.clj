(ns clojure-metrics.core
  "Static analysis tool for Clojure code based on clj-kondo.
   Calculates code quality metrics including:
   - Halstead complexity measures
   - Cyclomatic complexity number
   - Maintainability Index
   - Lines of Code (LoC)"
  (:require
   [clojure-metrics.controller.file-analyzer :as controller.file-analyzer]
   [clojure.data.json :as json]
   [clojure.tools.cli :as cli])
  (:gen-class))

(def cli-options
  [["-p" "--path PATH" "Path to directory to be analyzed"
    :default "."]
   ["-h" "--help" "Show this help message"]])

(defn -main
  "Main application function."
  [& args]
  (let [{:keys [options _arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options)
      (println "Clojure Static Analysis Tool")
      (println "Usage: lein run -- -p <directory>")
      (println summary)
      
      errors
      (do (println "Argument error(s):")
          (doseq [error errors]
            (println " " error))
          (System/exit 1))
      
      :else
      (try
        (let [path (:path options)
              file-results (controller.file-analyzer/process-directory path)
              aggregated-results (controller.file-analyzer/aggregate-system-metrics file-results)
              json-output (json/write-str aggregated-results :escape-slash false)]
          (println json-output))
        
        (catch Exception e
          (println "Error during analysis:" (.getMessage e))
          (System/exit 1))))))
