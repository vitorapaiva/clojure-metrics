(ns clojure-metrics.logic.loc
  "Implementation of Lines of Code (LoC) calculation for Clojure code.
   
   Following PHPMetrics standard:
   - cloc: lines count (total lines)
   - loc: lines count without multiline comments (physical lines)
   - lloc: lines count without empty lines (logical lines)"
  (:require [clojure.string :as str]))

(defn empty-line?
  "Checks if a line is empty or contains only whitespace."
  [line]
  (str/blank? line))

(defn comment-line?
  "Checks if a line contains only a comment."
  [line]
  (let [trimmed-line (str/trim line)]
    (and (not (str/blank? trimmed-line))
         (str/starts-with? trimmed-line ";"))))

(defn calculate-cloc
  "Calculates CLOC (total lines count)."
  [lines]
  (count lines))

(defn calculate-loc
  "Calculates LOC (lines count without comments)."
  [lines]
  (->> lines
       (remove comment-line?)
       count))

(defn calculate-lloc
  "Calculates LLOC (lines count without empty lines)."
  [lines]
  (->> lines
       (remove empty-line?)
       count))

(defn calculate-comment-lines
  "Calculates comment lines."
  [lines]
  (->> lines
       (filter comment-line?)
       count))

(defn calculate-comment-density
  "Calculates comment density as percentage for maintainability index."
  [total-lines comment-lines]
  (if (> total-lines 0)
    (* 100.0 (/ comment-lines total-lines))
    0.0))

(defn calculate-length-metrics
  "Calculates length metrics following PHPMetrics standard."
  [source-code]
  (let [lines (str/split-lines source-code)
        total-lines (count lines)
        comment-lines (calculate-comment-lines lines)]
    {:cloc (calculate-cloc lines)    ; total lines count
     :loc (calculate-loc lines)      ; lines without comments  
     :lloc (calculate-lloc lines)    ; lines without empty lines
     :comment-density (calculate-comment-density total-lines comment-lines)})) ; for maintainability

(defn aggregate-length-metrics
  "Aggregates length metrics from multiple files."
  [file-analyses]
  (let [total-cloc (reduce + (map #(get-in % [:length :cloc]) file-analyses))
        total-loc (reduce + (map #(get-in % [:length :loc]) file-analyses))
        total-lloc (reduce + (map #(get-in % [:length :lloc]) file-analyses))
        total-comment-lines (- total-cloc total-loc)
        system-comment-density (if (> total-cloc 0)
                                 (calculate-comment-density total-cloc total-comment-lines)
                                 0.0)]
    {:cloc total-cloc
     :loc total-loc
     :lloc total-lloc
     :comment-density system-comment-density}))