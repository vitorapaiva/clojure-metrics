(ns clojure-metrics.logic.maintainability
  "Implementation of Maintainability Index calculation for Clojure code.
   
   The Maintainability Index is a composite metric that considers:
   - Halstead Volume (complexity of implementation)
   - Cyclomatic Complexity (complexity of control flow)
   - Lines of Code (size factor)
   - Comment percentage (documentation factor)")

;; Constants for maintainability index calculation
(def MAINTAINABILITY_SCALE_FACTOR 
  "Scale factor for maintainability index calculation."
  171)

(def VOLUME_COEFFICIENT 
  "Coefficient for volume in maintainability calculation."
  5.2)

(def COMPLEXITY_COEFFICIENT 
  "Coefficient for cyclomatic complexity in maintainability calculation."
  0.23)

(def LOC_COEFFICIENT 
  "Coefficient for lines of code in maintainability calculation."
  16.2)

(def COMMENT_BONUS_MULTIPLIER 
  "Multiplier for comment density bonus."
  50)

(def MAX_MAINTAINABILITY_INDEX 
  "Maximum possible maintainability index value."
  100)

(def MIN_MAINTAINABILITY_INDEX 
  "Minimum possible maintainability index value."
  0)

;; Core calculation functions
(defn calculate-base-index
  "Calculates base maintainability index using Halstead volume, 
   cyclomatic complexity, and lines of code."
  [volume cyclomatic-complexity loc]
  (- MAINTAINABILITY_SCALE_FACTOR
     (* VOLUME_COEFFICIENT (Math/log volume))
     (* COMPLEXITY_COEFFICIENT cyclomatic-complexity)
     (* LOC_COEFFICIENT (Math/log loc))))

(defn calculate-comment-bonus
  "Calculates bonus points for comment density."
  [comment-density]
  (* COMMENT_BONUS_MULTIPLIER comment-density))

(defn normalize-index
  "Normalizes maintainability index to be between 0 and 100."
  [index]
  (max MIN_MAINTAINABILITY_INDEX 
       (min MAX_MAINTAINABILITY_INDEX index)))

(defn classify-maintainability
  "Classifies maintainability based on index value."
  [index]
  (cond
    (>= index 85) :excellent
    (>= index 70) :good
    (>= index 50) :moderate
    (>= index 25) :poor
    :else :critical))

(defn get-recommendations
  "Gets recommendations based on maintainability classification and metrics."
  [classification halstead-metrics]
  (let [base-recommendations
        (case classification
          :excellent ["Code is highly maintainable" "Consider code reviews to maintain quality"]
          :good ["Code is well maintained" "Monitor complexity growth"]
          :moderate ["Consider refactoring complex functions" "Improve documentation"]
          :poor ["Urgent refactoring needed" "Break down complex functions" "Add comprehensive tests"]
          :critical ["Immediate attention required" "Major refactoring needed" "Consider rewriting problematic areas"])]
    
    (cond-> base-recommendations
      (> (:difficulty halstead-metrics) 30)
      (conj "High difficulty detected - simplify expressions")
      
      (> (:effort halstead-metrics) 10000)
      (conj "High effort required - consider code splitting")
      
      (< (:volume halstead-metrics) 100)
      (conj "Very small codebase - ensure adequate functionality"))))

(defn analyze-impact-factors
  "Analyzes which factors most impact maintainability."
  [halstead-metrics cyclomatic-complexity length-metrics]
  (let [volume (:volume halstead-metrics)
        lloc (:lloc length-metrics)
        volume-impact (* VOLUME_COEFFICIENT (Math/log volume))
        complexity-impact (* COMPLEXITY_COEFFICIENT cyclomatic-complexity)
        loc-impact (* LOC_COEFFICIENT (Math/log lloc))]
    {:volume-impact volume-impact
     :complexity-impact complexity-impact
     :loc-impact loc-impact
     :total-negative-impact (+ volume-impact complexity-impact loc-impact)}))

(defn calculate-index
  "Calculates comprehensive Maintainability Index with detailed analysis."
  [halstead-metrics cyclomatic-complexity length-metrics]
  (let [volume (:volume halstead-metrics)
        ; Use lloc (logical lines) for maintainability calculation
        loc (:lloc length-metrics)
        ; Use comment density from length metrics for MIcw calculation
        comment-density (:comment-density length-metrics)
        
        base-index (calculate-base-index volume cyclomatic-complexity loc)
        comment-bonus (calculate-comment-bonus comment-density)
        adjusted-index (+ base-index comment-bonus)
        final-index (normalize-index adjusted-index)
        classification (classify-maintainability final-index)
        impact-factors (analyze-impact-factors halstead-metrics 
                                               cyclomatic-complexity 
                                               length-metrics)
        recommendations (get-recommendations classification halstead-metrics)]
    
    {:index final-index
     :raw-index base-index
     :comment-bonus comment-bonus
     :classification classification
     :impact-factors impact-factors
     :recommendations recommendations}))

(defn aggregate-maintainability-metrics
  "Aggregates maintainability metrics from multiple files."
  [file-analyses aggregated-halstead aggregated-cyclomatic aggregated-length]
  (let [total-files (count file-analyses)
        
        ;; Calculate system-wide maintainability using aggregated metrics
        system-maintainability (calculate-index
                                aggregated-halstead
                                (:total-complexity aggregated-cyclomatic)
                                aggregated-length)
        
        ;; Calculate average maintainability across files
        avg-maintainability (if (> total-files 0)
                             (/ (reduce + (map #(get-in % [:maintainability :index]) file-analyses)) total-files)
                             0)]
    
    {:system-maintainability system-maintainability
     :average-maintainability avg-maintainability}))