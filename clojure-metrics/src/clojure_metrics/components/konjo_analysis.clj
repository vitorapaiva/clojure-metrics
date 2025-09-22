(ns clojure-metrics.components.konjo-analysis
  (:require [clj-kondo.core :as clj-kondo]))

(defn konjo-analysis
  "Analyzes a Clojure file with clj-kondo and returns detailed AST analysis."
  [file-path]
  (:analysis (clj-kondo/run! {:lint [file-path] 
                              :config {:output {:analysis true 
                                               :format :edn}}})))