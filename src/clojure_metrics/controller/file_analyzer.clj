(ns clojure-metrics.controller.file-analyzer
  (:require
   [clojure-metrics.components.konjo-analysis :as components.konjo-analysis]
   [clojure-metrics.logic.cyclomatic :as cyclomatic]
   [clojure-metrics.logic.halstead :as halstead]
   [clojure-metrics.logic.loc :as loc]
   [clojure-metrics.logic.maintainability :as maintainability]
   [clojure.java.io :as io]))

(defn analyze-file
  "Analyzes a Clojure file and returns calculated metrics."
  [file-path]
  (let [content (slurp file-path)
        ast (components.konjo-analysis/konjo-analysis file-path)
        length-metrics (loc/calculate-length-metrics content)
        halstead-metrics (halstead/calculate-halstead-from-ast ast content)
        cyclomatic-complexity (cyclomatic/calculate-complexity-from-ast ast) 
        maintainability-result (maintainability/calculate-index
                                 halstead-metrics
                                 cyclomatic-complexity
                                 length-metrics)
         
         ;; Extract additional metrics from AST
         var-definitions (:var-definitions ast)
         namespace-usages (:namespace-usages ast)
         
         functions (count var-definitions)
         dependencies (count namespace-usages)
         
         ;; Advanced metrics from clj-kondo AST
         public-functions (->> var-definitions
                               (remove :private)
                               count)
         private-functions (- functions public-functions)
         
         macros (->> var-definitions
                     (filter :macro)
                     count)
         
         total-keywords (count (:keywords ast))
         total-locals (count (:locals ast))]
         
    {:file file-path
     :length length-metrics
     :halstead halstead-metrics
     :cyclomatic-complexity cyclomatic-complexity
     :maintainability maintainability-result
     
     ;; Structure metrics
     :functions functions
     :public-functions public-functions
     :private-functions private-functions
     :macros macros
     :dependencies dependencies
     
     ;; Code detail metrics
     :keywords total-keywords
     :locals total-locals}))

(defn aggregate-structure-metrics
  "Aggregates structural metrics from multiple files."
  [file-analyses]
  (let [total-functions (reduce + (map :functions file-analyses))
        total-public-functions (reduce + (map :public-functions file-analyses))
        total-private-functions (reduce + (map :private-functions file-analyses))
        total-macros (reduce + (map :macros file-analyses))
        total-dependencies (reduce + (map :dependencies file-analyses))
        total-keywords (reduce + (map :keywords file-analyses))
        total-locals (reduce + (map :locals file-analyses))]
    {:functions total-functions
     :public-functions total-public-functions
     :private-functions total-private-functions
     :macros total-macros
     :dependencies total-dependencies
     :keywords total-keywords
     :locals total-locals}))

(defn aggregate-system-metrics
  "Aggregates metrics from all files to provide system-wide analysis."
  [file-analyses]
  (let [total-files (count file-analyses)
        aggregated-length (loc/aggregate-length-metrics file-analyses)
        aggregated-halstead (halstead/aggregate-halstead-metrics file-analyses)
        aggregated-cyclomatic (cyclomatic/aggregate-cyclomatic-metrics file-analyses)
        aggregated-structure (aggregate-structure-metrics file-analyses)
        aggregated-maintainability (maintainability/aggregate-maintainability-metrics 
                                    file-analyses)]
    
    {:system-summary
     {:total-files total-files
      :length aggregated-length
      :halstead aggregated-halstead
      :cyclomatic-complexity (:total-complexity aggregated-cyclomatic)
      :average-cyclomatic-complexity (:average-complexity aggregated-cyclomatic)
      :maintainability (:system-maintainability aggregated-maintainability)
      :average-maintainability (:average-maintainability aggregated-maintainability)
      :structure aggregated-structure}
     :files file-analyses}))

(defn process-directory
  "Processes all .clj files in a directory recursively."
  [path]
  (let [directory (io/file path)]
    (if (.isDirectory directory)
      (->> (file-seq directory)
           (filter #(.isFile %))
           (filter #(.endsWith (.getName %) ".clj"))
           (map #(.getPath %))
           (map analyze-file))
      (throw (IllegalArgumentException. (str "Path must be a directory: " path))))))