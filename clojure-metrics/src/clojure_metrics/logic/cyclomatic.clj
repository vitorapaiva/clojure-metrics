(ns clojure-metrics.logic.cyclomatic
  "Implementation of cyclomatic complexity calculation for Clojure code.
   
   Cyclomatic complexity measures the number of linearly independent paths
   through a program's source code. It is calculated as:
   M = E - N + 2P
   where:
   - E = number of edges in the flow graph
   - N = number of nodes in the flow graph  
   - P = number of connected components (usually 1)
   
   For practical purposes, we count decision points + 1."
)

;; Constants for cyclomatic complexity calculation
(def LINEAR_PATH_COMPLEXITY 
  "Base complexity representing one linear execution path through any function."
  1)

(def MINIMUM_FUNCTION_COMPLEXITY 
  "Minimum complexity value - even empty functions have one path."
  LINEAR_PATH_COMPLEXITY)

(def COND_TEST_RESULT_PAIR_SIZE 
  "Number of arguments in a cond test-result pair: (test result)."
  2)

(def CASE_EXPR_OFFSET 
  "Offset for case expression - first argument is the test expression."
  1)

(def FALLBACK_DECISION_COMPLEXITY 
  "Default complexity when arity information is not available."
  1)

(def decision-points
  "Set of Clojure forms that create decision points in control flow."
  #{'if 'when 'when-not 'cond 'case 'condp
    'loop 'while 'doseq 'dotimes 'for
    'try 'catch
    'and 'or
    'when-let 'when-some 'if-let 'if-some})

(def special-decision-forms
  "Forms requiring special complexity calculation based on arity."
  #{'cond 'case})

(def function-definition-forms
  "Forms that define functions for complexity analysis."
  #{'clojure.core/defn 'clojure.core/defn- 'clojure.core/fn})

(defn calculate-cond-complexity
  "Calculates complexity for a cond form based on test-result pairs."
  [arity]
  (/ arity COND_TEST_RESULT_PAIR_SIZE))

(defn calculate-case-complexity
  "Calculates complexity for a case form based on number of cases."
  [arity]
  (- arity CASE_EXPR_OFFSET))

(defn calculate-special-form-complexity
  "Calculates complexity for cond and case forms."
  [usage]
  (let [arity (:arity usage)]
    (if arity
      (max MINIMUM_FUNCTION_COMPLEXITY 
           (if (= (:name usage) 'cond)
             (calculate-cond-complexity arity)
             (calculate-case-complexity arity)))
      FALLBACK_DECISION_COMPLEXITY)))

(defn calculate-function-complexity-excepto-special-forms
  "Calculates cyclomatic complexity for a single function."
  [var-usages-in-function]
  (let [simple-decisions (->> var-usages-in-function
                              (map :name)
                              (filter #(contains? decision-points %))
                              (remove #(contains? special-decision-forms %))
                              count) 
        special-complexity (->> var-usages-in-function
                                (filter #(contains? special-decision-forms (:name %)))
                                (map calculate-special-form-complexity)
                                (reduce +))]
    (+ LINEAR_PATH_COMPLEXITY simple-decisions special-complexity)))

(defn calculate-complexity-from-ast
  "Calculates total cyclomatic complexity for all functions in file."
  [ast]
  (let [var-definitions (:var-definitions ast)
        var-usages (:var-usages ast)
        function-complexities
        (->> var-definitions
             (filter #(contains? function-definition-forms (:defined-by %)))
             (map (fn [func-def]
                    (let [func-usages (filter #(= (:from-var %) (:name func-def)) var-usages)]
                      (calculate-function-complexity-excepto-special-forms func-usages)))))]
    (max MINIMUM_FUNCTION_COMPLEXITY (reduce + function-complexities))))

(defn aggregate-cyclomatic-metrics
  "Aggregates cyclomatic complexity metrics from multiple files."
  [file-analyses]
  (let [total-files (count file-analyses)
        total-cyclomatic (reduce + (map :cyclomatic-complexity file-analyses))
        avg-cyclomatic (if (> total-files 0) 
                        (/ total-cyclomatic total-files) 
                        0)]
    {:total-complexity total-cyclomatic
     :average-complexity avg-cyclomatic
     :total-files total-files}))
