(ns clojure-metrics.logic.loc
  "Implementation of Lines of Code (LoC) calculation for Clojure code.
   
   Following PHPMetrics standard:
   - loc: total physical lines (Lines of Code)
   - cloc: comment lines (Comment Lines of Code)
   - lloc: logical lines = code lines (excluding comments and empty lines)"
  (:require [clojure.string :as str]))

(defn empty-line?
  "Checks if a line is empty or contains only whitespace."
  [line]
  (str/blank? line))

(defn comment-line?
  "Checks if a line contains only a comment (semicolon)."
  [line]
  (let [trimmed-line (str/trim line)]
    (and (not (str/blank? trimmed-line))
         (str/starts-with? trimmed-line ";"))))

(defn calculate-cloc
  "Calculates CLOC (Comment Lines of Code) - PHPMetrics standard."
  [lines]
  (->> lines
       (filter comment-line?)
       count))

(defn calculate-loc
  "Calculates LOC (total physical Lines of Code) - PHPMetrics standard."
  [lines]
  (count lines))

(defn calculate-lloc
  "Calculates LLOC (Logical Lines of Code) - code excluding comments and empty lines.
   PHPMetrics: lloc = lines after removing comments and empty lines."
  [lines]
  (->> lines
       (remove comment-line?)
       (remove empty-line?)
       count))

(defn calculate-comment-lines
  "Calculates comment lines count."
  [lines]
  (calculate-cloc lines))

(defn calculate-comment-density
  "Calculates comment density for maintainability index.
   perCM = cloc / loc (same as PHPMetrics: CM = cloc / loc)"
  [total-lines comment-lines]
  (if (> total-lines 0)
    (/ (double comment-lines) total-lines)
    0.0))

(defn calculate-length-metrics
  "Calculates length metrics following PHPMetrics standard."
  [source-code]
  (let [lines (str/split-lines source-code)
        loc (calculate-loc lines)
        cloc (calculate-cloc lines)
        lloc (calculate-lloc lines)
        comment-density (calculate-comment-density loc cloc)]
    {:cloc cloc
     :loc loc
     :lloc lloc
     :comment-density comment-density}))

(defn aggregate-length-metrics
  "Aggregates length metrics from multiple files."
  [file-analyses]
  (let [total-cloc (reduce + (map #(get-in % [:length :cloc]) file-analyses))
        total-loc (reduce + (map #(get-in % [:length :loc]) file-analyses))
        total-lloc (reduce + (map #(get-in % [:length :lloc]) file-analyses))
        system-comment-density (if (> total-loc 0)
                                 (calculate-comment-density total-loc total-cloc)
                                 0.0)]
    {:cloc total-cloc
     :loc total-loc
     :lloc total-lloc
     :comment-density system-comment-density}))