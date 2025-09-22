(ns clojure-metrics.unit.logic.cyclomatic-test
  (:require [clojure.test :refer :all]
            [clojure-metrics.logic.cyclomatic :as cyclomatic]
            [clojure-metrics.resources.fixtures.clj-kondo-fixtures :as fixtures]))

(deftest test-calculate-cond-complexity
  (testing "Cond complexity calculation"
    (is (= 3 (cyclomatic/calculate-cond-complexity 6)) "6 args = 3 test-result pairs")
    (is (= 2 (cyclomatic/calculate-cond-complexity 4)) "4 args = 2 test-result pairs")
    (is (= 1 (cyclomatic/calculate-cond-complexity 2)) "2 args = 1 test-result pair")))

(deftest test-calculate-case-complexity
  (testing "Case complexity calculation"
    (is (= 4 (cyclomatic/calculate-case-complexity 5)) "5 args = 1 expr + 4 cases")
    (is (= 2 (cyclomatic/calculate-case-complexity 3)) "3 args = 1 expr + 2 cases")
    (is (= 0 (cyclomatic/calculate-case-complexity 1)) "1 arg = just the expression")))

(deftest test-calculate-special-form-complexity
  (testing "Cond special form complexity"
    (let [cond-usage {:name 'cond :arity 6}
          result (cyclomatic/calculate-special-form-complexity cond-usage)]
      (is (= 3 result) "Cond with 6 args should have complexity 3")))

  (testing "Case special form complexity"
    (let [case-usage {:name 'case :arity 5}
          result (cyclomatic/calculate-special-form-complexity case-usage)]
      (is (= 4 result) "Case with 5 args should have complexity 4")))

  (testing "Special form without arity"
    (let [usage {:name 'cond}
          result (cyclomatic/calculate-special-form-complexity usage)]
      (is (= cyclomatic/FALLBACK_DECISION_COMPLEXITY result) "Should use fallback complexity")))

  (testing "Minimum complexity enforcement"
    (let [case-usage {:name 'case :arity 1}
          result (cyclomatic/calculate-special-form-complexity case-usage)]
      (is (= cyclomatic/MINIMUM_FUNCTION_COMPLEXITY result) "Should enforce minimum complexity"))))

(deftest test-calculate-function-complexity-excepto-special-forms
  (testing "Simple function complexity"
    (let [var-usages [{:name 'if}
                      {:name '+}
                      {:name 'when}]
          result (cyclomatic/calculate-function-complexity-excepto-special-forms var-usages)]
      (is (= 3 result) "Base complexity 1 + 2 decision points (if, when)")))

  (testing "Function with special forms"
    (let [var-usages [{:name 'if}
                      {:name 'cond :arity 4}
                      {:name 'case :arity 3}]
          result (cyclomatic/calculate-function-complexity-excepto-special-forms var-usages)]
      (is (= 6 result) "Base 1 + if 1 + cond 2 + case 2")))

  (testing "Function with no decision points"
    (let [var-usages [{:name '+}
                      {:name 'map}
                      {:name 'println}]
          result (cyclomatic/calculate-function-complexity-excepto-special-forms var-usages)]
      (is (= cyclomatic/LINEAR_PATH_COMPLEXITY result) "Should return base complexity")))

  (testing "Empty var usages"
    (let [result (cyclomatic/calculate-function-complexity-excepto-special-forms [])]
      (is (= cyclomatic/LINEAR_PATH_COMPLEXITY result) "Should return base complexity"))))

(deftest test-calculate-complexity-from-ast
  (testing "Simple function AST complexity"
    (let [result (cyclomatic/calculate-complexity-from-ast fixtures/simple-function-ast)]
      (is (>= result cyclomatic/MINIMUM_FUNCTION_COMPLEXITY) "Should be at least minimum complexity")
      (is (number? result) "Should return a number")))

  (testing "Complex function AST complexity"
    (let [result (cyclomatic/calculate-complexity-from-ast fixtures/complex-function-ast)]
      (is (> result cyclomatic/MINIMUM_FUNCTION_COMPLEXITY) "Should be higher than minimum")
      (is (number? result) "Should return a number")))

  (testing "Multiple functions AST complexity"
    (let [result (cyclomatic/calculate-complexity-from-ast fixtures/multiple-functions-ast)]
      (is (> result cyclomatic/MINIMUM_FUNCTION_COMPLEXITY) "Should sum all function complexities")
      (is (number? result) "Should return a number")))

  (testing "Empty AST complexity"
    (let [result (cyclomatic/calculate-complexity-from-ast fixtures/empty-file-ast)]
      (is (= cyclomatic/MINIMUM_FUNCTION_COMPLEXITY result) "Should return minimum complexity for empty file"))))

(deftest test-aggregate-cyclomatic-metrics
  (testing "Aggregate metrics from multiple files"
    (let [file-analyses [{:cyclomatic-complexity 5}
                         {:cyclomatic-complexity 3}
                         {:cyclomatic-complexity 7}]
          result (cyclomatic/aggregate-cyclomatic-metrics file-analyses)]
      (is (= 15 (:total-complexity result)) "Should sum all complexities")
      (is (= 5 (:average-complexity result)) "Should calculate average")
      (is (= 3 (:total-files result)) "Should count files")))

  (testing "Aggregate with single file"
    (let [file-analyses [{:cyclomatic-complexity 10}]
          result (cyclomatic/aggregate-cyclomatic-metrics file-analyses)]
      (is (= 10 (:total-complexity result)))
      (is (= 10 (:average-complexity result)))
      (is (= 1 (:total-files result)))))

  (testing "Aggregate with empty collection"
    (let [result (cyclomatic/aggregate-cyclomatic-metrics [])]
      (is (= 0 (:total-complexity result)))
      (is (= 0 (:average-complexity result)))
      (is (= 0 (:total-files result))))))

(deftest test-constants
  (testing "Constants are properly defined"
    (is (= 1 cyclomatic/LINEAR_PATH_COMPLEXITY))
    (is (= cyclomatic/LINEAR_PATH_COMPLEXITY cyclomatic/MINIMUM_FUNCTION_COMPLEXITY))
    (is (= 2 cyclomatic/COND_TEST_RESULT_PAIR_SIZE))
    (is (= 1 cyclomatic/CASE_EXPR_OFFSET))
    (is (= 1 cyclomatic/FALLBACK_DECISION_COMPLEXITY))))

(deftest test-decision-points-set
  (testing "Decision points set contains expected forms"
    (is (contains? cyclomatic/decision-points 'if))
    (is (contains? cyclomatic/decision-points 'when))
    (is (contains? cyclomatic/decision-points 'cond))
    (is (contains? cyclomatic/decision-points 'case))
    (is (contains? cyclomatic/decision-points 'loop))
    (is (contains? cyclomatic/decision-points 'try))))

(deftest test-special-decision-forms
  (testing "Special decision forms set"
    (is (contains? cyclomatic/special-decision-forms 'cond))
    (is (contains? cyclomatic/special-decision-forms 'case))
    (is (= 2 (count cyclomatic/special-decision-forms)) "Should only contain cond and case")))

(deftest test-function-definition-forms
  (testing "Function definition forms set"
    (is (contains? cyclomatic/function-definition-forms 'clojure.core/defn))
    (is (contains? cyclomatic/function-definition-forms 'clojure.core/defn-))
    (is (contains? cyclomatic/function-definition-forms 'clojure.core/fn))))
