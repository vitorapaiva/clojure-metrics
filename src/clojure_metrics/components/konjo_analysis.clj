(ns clojure-metrics.components.konjo-analysis
  (:require [clj-kondo.core :as clj-kondo]))

(defn konjo-analysis
  "Analyzes a Clojure file with clj-kondo and returns detailed AST analysis.
   Enables keywords and locals extraction for accurate Halstead metrics."
  [file-path]
  (:analysis (clj-kondo/run! {:lint [file-path] 
                              :config {:output {:analysis {:keywords true
                                                           :locals true
                                                           :arglists true
                                                           :var-definitions {:meta true}
                                                           :var-usages {:meta true}}
                                               :format :edn}}})))