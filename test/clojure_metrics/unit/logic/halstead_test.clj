(ns clojure-metrics.unit.logic.halstead-test
  (:require [clojure.test :refer :all]
            [clojure-metrics.logic.halstead :as halstead]
            [clojure-metrics.resources.fixtures.clj-kondo-fixtures :as fixtures]))

(deftest test-count-halstead
  (testing "Basic Halstead counting"
    (let [operators ['+ '- '*]
          operands ['a 'b 'c 'a]
          result (halstead/count-halstead operators operands)]
      (is (= 3 (:n1 result)) "Unique operators count")
      (is (= 3 (:n2 result)) "Unique operands count") 
      (is (= 3 (:N1 result)) "Total operators count")
      (is (= 4 (:N2 result)) "Total operands count")
      (is (= #{'+ '- '*} (:unique-operators result)))
      (is (= #{'a 'b 'c} (:unique-operands result)))))

  (testing "Empty collections"
    (let [result (halstead/count-halstead [] [])]
      (is (= 0 (:n1 result)))
      (is (= 0 (:n2 result)))
      (is (= 0 (:N1 result)))
      (is (= 0 (:N2 result)))))

  (testing "Only operators"
    (let [result (halstead/count-halstead ['+ '+] [])]
      (is (= 1 (:n1 result)))
      (is (= 0 (:n2 result)))
      (is (= 2 (:N1 result)))
      (is (= 0 (:N2 result))))))

(deftest test-calculate-derived-metrics
  (testing "Derived metrics calculation"
    (let [basic-metrics {:n1 2 :n2 3 :N1 5 :N2 7}
          result (halstead/calculate-derived-metrics basic-metrics)]
      (is (= 5 (:vocabulary result)) "Vocabulary = n1 + n2")
      (is (= 12 (:length result)) "Length = N1 + N2")
      (is (> (:volume result) 0) "Volume should be positive")
      (is (> (:difficulty result) 0) "Difficulty should be positive")
      (is (> (:effort result) 0) "Effort should be positive")))

  (testing "Zero complexity cases"
    (let [zero-metrics {:n1 0 :n2 0 :N1 0 :N2 0}
          result (halstead/calculate-derived-metrics zero-metrics)]
      (is (= 0 (:volume result)) "Volume should be 0 when vocabulary is 0")
      (is (= 0 (:difficulty result)) "Difficulty should be 0 when operands are 0")
      (is (= 0 (:effort result)) "Effort should be 0")))

  (testing "Edge case: only operators"
    (let [only-operators {:n1 2 :n2 0 :N1 5 :N2 0}
          result (halstead/calculate-derived-metrics only-operators)]
      (is (= 0 (:difficulty result)) "Difficulty should be 0 when n2 is 0"))))

(deftest test-extract-operators-from-ast
  (testing "Extract operators from simple function AST"
    (let [operators (halstead/extract-operators-from-ast fixtures/simple-function-ast)]
      (is (sequential? operators))
      (is (some #{'+} operators) "Should contain + operator")
      (is (some #{'defn} operators) "Should contain defn operator")))

  (testing "Extract operators from complex function AST"
    (let [operators (halstead/extract-operators-from-ast fixtures/complex-function-ast)]
      (is (some #{'if} operators) "Should contain if operator")
      (is (some #{'cond} operators) "Should contain cond operator")
      (is (some #{'>} operators) "Should contain > operator")
      (is (some #{'*} operators) "Should contain * operator")))

  (testing "Extract operators from empty AST"
    (let [operators (halstead/extract-operators-from-ast fixtures/empty-file-ast)]
      (is (empty? operators) "Should return empty collection for empty AST"))))

(deftest test-extract-operands-from-ast
  (testing "Extract operands from simple function AST"
    (let [operands (halstead/extract-operands-from-ast fixtures/simple-function-ast)]
      (is (sequential? operands))
      (is (some #{'a} operands) "Should contain parameter 'a'")
      (is (some #{'b} operands) "Should contain parameter 'b'")
      (is (some #{'add-numbers} operands) "Should contain function name")))

  (testing "Extract operands from complex function AST with keywords"
    (let [operands (halstead/extract-operands-from-ast fixtures/complex-function-ast)]
      (is (some #{'x} operands) "Should contain parameter 'x'")
      (is (some #{:zero} operands) "Should contain keyword :zero")
      (is (some #{:negative} operands) "Should contain keyword :negative")
      (is (some #{:positive} operands) "Should contain keyword :positive")))

  (testing "Extract operands from empty AST"
    (let [operands (halstead/extract-operands-from-ast fixtures/empty-file-ast)]
      (is (empty? operands) "Should return empty collection for empty AST"))))

(deftest test-calculate-halstead-from-ast
  (testing "Full Halstead calculation from AST"
    (let [result (halstead/calculate-halstead-from-ast fixtures/simple-function-ast "dummy content")]
      (is (contains? result :n1) "Should contain n1")
      (is (contains? result :n2) "Should contain n2")
      (is (contains? result :N1) "Should contain N1")
      (is (contains? result :N2) "Should contain N2")
      (is (contains? result :vocabulary) "Should contain vocabulary")
      (is (contains? result :length) "Should contain length")
      (is (contains? result :volume) "Should contain volume")
      (is (contains? result :difficulty) "Should contain difficulty")
      (is (contains? result :effort) "Should contain effort")
      (is (> (:n1 result) 0) "Should have unique operators")
      (is (> (:n2 result) 0) "Should have unique operands")))

  (testing "Halstead calculation with empty AST"
    (let [result (halstead/calculate-halstead-from-ast fixtures/empty-file-ast "")]
      (is (= 0 (:n1 result)) "Should have 0 unique operators")
      (is (= 0 (:n2 result)) "Should have 0 unique operands")
      (is (= 0 (:volume result)) "Should have 0 volume")
      (is (= 0 (:difficulty result)) "Should have 0 difficulty")
      (is (= 0 (:effort result)) "Should have 0 effort"))))

(deftest test-aggregate-halstead-metrics
  (testing "Aggregate metrics from multiple files"
    (let [file-analyses [{:halstead {:n1 2 :n2 3 :N1 5 :N2 7}}
                         {:halstead {:n1 1 :n2 2 :N1 3 :N2 4}}]
          result (halstead/aggregate-halstead-metrics file-analyses)]
      (is (= 3 (:n1 result)) "Should sum n1 values")
      (is (= 5 (:n2 result)) "Should sum n2 values")
      (is (= 8 (:N1 result)) "Should sum N1 values")
      (is (= 11 (:N2 result)) "Should sum N2 values")
      (is (= 8 (:vocabulary result)) "Should calculate system vocabulary")
      (is (= 19 (:length result)) "Should calculate system length")
      (is (> (:volume result) 0) "Should calculate system volume")
      (is (> (:difficulty result) 0) "Should calculate system difficulty")
      (is (> (:effort result) 0) "Should calculate system effort")))

  (testing "Aggregate with empty collection"
    (let [result (halstead/aggregate-halstead-metrics [])]
      (is (= 0 (:n1 result)))
      (is (= 0 (:n2 result)))
      (is (= 0 (:N1 result)))
      (is (= 0 (:N2 result)))
      (is (= 0 (:volume result)))
      (is (= 0 (:difficulty result)))
      (is (= 0 (:effort result))))))

(deftest test-constants
  (testing "Constants are properly defined"
    (is (= 2 halstead/LOG_BASE_2))
    (is (= 2.0 halstead/OPERATOR_DIFFICULTY_DIVISOR))
    (is (= 0 halstead/ZERO_COMPLEXITY))))