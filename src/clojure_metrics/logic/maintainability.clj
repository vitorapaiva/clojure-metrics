(ns clojure-metrics.logic.maintainability
  "Implementation of Maintainability Index calculation for Clojure code.
   
   The Maintainability Index is a composite metric that considers:
   - Halstead Volume (complexity of implementation)
   - Cyclomatic Complexity (complexity of control flow)
   - Lines of Code (size factor)
   - Comment percentage (documentation factor)
   
   Uses the same normalization as PHPMetrics for comparability:
   MI_normalized = (MI_raw * 100) / 171")

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

(def PHPMETRICS_NORMALIZATION_FACTOR
  "Normalization factor used by PHPMetrics: (MI * 100) / 171"
  171.0)

;; Core calculation functions
(defn calculate-base-index
  "Calculates MIwoc (Maintainability Index without comments) using average metrics per module.
   Formula: MIwoc = 171 - 5.2 * ln(aveV) - 0.23 * aveG - 16.2 * ln(aveLOC)"
  [average-volume average-cyclomatic average-loc]
  (- MAINTAINABILITY_SCALE_FACTOR
     (* VOLUME_COEFFICIENT (Math/log average-volume))
     (* COMPLEXITY_COEFFICIENT average-cyclomatic)
     (* LOC_COEFFICIENT (Math/log average-loc))))

(defn calculate-comment-bonus
  "Calculates MIcw (Maintainability Index comment weight) using average comment percentage.
   Formula: MIcw = 50 * sin(sqrt(2.4 * perCM))"
  [average-comment-percentage]
  (let [;; Convert percentage to decimal if needed (0-100 -> 0-1)
        percent-decimal (if (> average-comment-percentage 1.0)
                         (/ average-comment-percentage 100.0)
                         average-comment-percentage)
        ;; Apply the formula: 50 * sin(sqrt(2.4 * perCM))
        sqrt-term (Math/sqrt (* 2.4 percent-decimal))
        sin-term (Math/sin sqrt-term)]
    (* 50 sin-term)))

(defn normalize-index
  "Normalizes maintainability index to be between 0 and 100.
   Uses the same normalization as PHPMetrics: (MI * 100) / 171
   This ensures comparability between Clojure and PHP metrics."
  [index]
  (let [normalized (* (/ index PHPMETRICS_NORMALIZATION_FACTOR) 100)]
    (max MIN_MAINTAINABILITY_INDEX 
         (min MAX_MAINTAINABILITY_INDEX normalized))))

(defn classify-maintainability
  "Classifies maintainability based on normalized index value.
   Thresholds aligned with PHPMetrics scale (0-100 normalized):
   - excellent: >= 65 (highly maintainable)
   - good: >= 50 (maintainable with minor issues)
   - moderate: >= 35 (moderately maintainable)
   - poor: >= 20 (difficult to maintain)
   - critical: < 20 (very difficult to maintain)"
  [index]
  (cond
    (>= index 65) :excellent
    (>= index 50) :good
    (>= index 35) :moderate
    (>= index 20) :poor
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
  "Calculates comprehensive Maintainability Index with detailed analysis.
   For single file: uses file metrics directly as 'averages'
   For system: should use actual averages across modules"
  [halstead-metrics cyclomatic-complexity length-metrics]
  (let [volume (:volume halstead-metrics)
        ; Use lloc (logical lines) for maintainability calculation
        loc (:lloc length-metrics)
        ; Use comment density from length metrics for MIcw calculation
        comment-density (:comment-density length-metrics)
        
        ;; Calculate MIwoc = 171 - 5.2 * ln(aveV) - 0.23 * aveG - 16.2 * ln(aveLOC)
        miwoc (calculate-base-index volume cyclomatic-complexity loc)
        
        ;; Calculate MIcw = 50 * sin(sqrt(2.4 * perCM))
        micw (calculate-comment-bonus comment-density)
        
        ;; MI = MIwoc + MIcw
        mi-total (+ miwoc micw)
        final-index (normalize-index mi-total)
        classification (classify-maintainability final-index)
        impact-factors (analyze-impact-factors halstead-metrics 
                                               cyclomatic-complexity 
                                               length-metrics)
        recommendations (get-recommendations classification halstead-metrics)]
    
    {:index final-index
     :miwoc miwoc
     :micw micw
     :raw-index miwoc  ; For backward compatibility with tests
     :comment-bonus micw  ; For backward compatibility with tests
     :classification classification
     :impact-factors impact-factors
     :recommendations recommendations}))

(defn aggregate-maintainability-metrics
  "Aggregates maintainability metrics from multiple files using correct averages per module."
  [file-analyses]
  (let [total-files (count file-analyses)
        
        ;; Calculate averages per module (file) as required by MI formula
        ;; Filter out nil values to avoid NullPointerException
        volumes (keep #(get-in % [:halstead :volume]) file-analyses)
        cyclomatics (keep :cyclomatic-complexity file-analyses)
        locs (keep #(get-in % [:length :lloc]) file-analyses)
        comment-densities (keep #(get-in % [:length :comment-density]) file-analyses)
        
        ;; Calculate averages, ensuring we have valid data
        average-volume (if (seq volumes)
                        (/ (reduce + volumes) (count volumes))
                        1.0)  ; Avoid log(0) error
        average-cyclomatic (if (seq cyclomatics)
                            (/ (reduce + cyclomatics) (count cyclomatics))
                            1.0)  ; Default to 1 for minimal complexity
        average-loc (if (seq locs)
                     (/ (reduce + locs) (count locs))
                     1.0)  ; Avoid log(0) error
        average-comment-density (if (seq comment-densities)
                                 (/ (reduce + comment-densities) (count comment-densities))
                                 0.0)
        
        ;; Calculate system maintainability using proper averages
        ;; MIwoc = 171 - 5.2 * ln(aveV) - 0.23 * aveG - 16.2 * ln(aveLOC)
        miwoc (calculate-base-index average-volume average-cyclomatic average-loc)
        
        ;; MIcw = 50 * sin(sqrt(2.4 * perCM))
        micw (calculate-comment-bonus average-comment-density)
        
        ;; MI = MIwoc + MIcw
        system-mi (+ miwoc micw)
        final-system-mi (normalize-index system-mi)
        system-classification (classify-maintainability final-system-mi)
        
        ;; Calculate average maintainability across individual files
        avg-maintainability (if (> total-files 0)
                             (/ (reduce + (map #(get-in % [:maintainability :index]) file-analyses)) total-files)
                             0)]
    
    {:system-maintainability {:index final-system-mi
                              :miwoc miwoc
                              :micw micw
                              :classification system-classification
                              :average-volume average-volume
                              :average-cyclomatic average-cyclomatic
                              :average-loc average-loc
                              :average-comment-density average-comment-density}
     :average-maintainability avg-maintainability}))