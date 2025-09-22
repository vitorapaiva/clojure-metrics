(ns clojure-metrics.integration.file-analyzer-test
  "Integration tests for the complete clojure-metrics analysis pipeline."
  (:require [clojure.test :refer :all]
            [clojure-metrics.controller.file-analyzer :as file-analyzer]
            [clojure.java.io :as io]
            [clojure.data.json :as json]))

(def test-fixtures-dir "test/clojure_metrics/resources/fixtures/")
(def code-example-file (str test-fixtures-dir "code_example.clj.sample"))

(deftest test-analyze-single-file
  (testing "Analyze a single Clojure file and validate all metrics"
    (let [result (file-analyzer/analyze-file code-example-file)]
      
      ;; Test basic structure
      (is (contains? result :file) "Should contain file path")
      (is (= code-example-file (:file result)) "Should have correct file path")
      
      ;; Test length metrics
      (is (contains? result :length) "Should contain length metrics")
      (let [length (:length result)]
        (is (contains? length :cloc) "Should contain CLOC")
        (is (contains? length :loc) "Should contain LOC")
        (is (contains? length :lloc) "Should contain LLOC")
        (is (contains? length :comment-density) "Should contain comment density")
        (is (> (:cloc length) 0) "CLOC should be positive")
        (is (> (:loc length) 0) "LOC should be positive")
        (is (> (:lloc length) 0) "LLOC should be positive")
        (is (>= (:comment-density length) 0) "Comment density should be non-negative"))
      
      ;; Test Halstead metrics
      (is (contains? result :halstead) "Should contain Halstead metrics")
      (let [halstead (:halstead result)]
        (is (contains? halstead :n1) "Should contain n1")
        (is (contains? halstead :n2) "Should contain n2")
        (is (contains? halstead :N1) "Should contain N1")
        (is (contains? halstead :N2) "Should contain N2")
        (is (contains? halstead :vocabulary) "Should contain vocabulary")
        (is (contains? halstead :length) "Should contain length")
        (is (contains? halstead :volume) "Should contain volume")
        (is (contains? halstead :difficulty) "Should contain difficulty")
        (is (contains? halstead :effort) "Should contain effort")
        (is (> (:n1 halstead) 0) "Should have unique operators")
        (is (> (:n2 halstead) 0) "Should have unique operands")
        (is (> (:volume halstead) 0) "Volume should be positive")
        (is (> (:difficulty halstead) 0) "Difficulty should be positive"))
      
      ;; Test cyclomatic complexity
      (is (contains? result :cyclomatic-complexity) "Should contain cyclomatic complexity")
      (is (number? (:cyclomatic-complexity result)) "Cyclomatic complexity should be a number")
      (is (> (:cyclomatic-complexity result) 0) "Should have positive complexity")
      ;; The example has 3 functions with multiple decision points, so complexity should be substantial
      (is (> (:cyclomatic-complexity result) 10) "Should reflect multiple decision points in example code")
      
      ;; Test maintainability index
      (is (contains? result :maintainability) "Should contain maintainability metrics")
      (let [maintainability (:maintainability result)]
        (is (contains? maintainability :index) "Should contain maintainability index")
        (is (contains? maintainability :classification) "Should contain classification")
        (is (contains? maintainability :recommendations) "Should contain recommendations")
        (is (number? (:index maintainability)) "Index should be a number")
        (is (>= (:index maintainability) 0) "Index should be non-negative")
        (is (<= (:index maintainability) 100) "Index should not exceed 100")
        (is (keyword? (:classification maintainability)) "Classification should be a keyword")
        (is (sequential? (:recommendations maintainability)) "Recommendations should be a sequence"))
      
      ;; Test structure metrics
      (is (contains? result :functions) "Should contain function count")
      (is (= 3 (:functions result)) "Should detect 3 functions in example")
      (is (contains? result :public-functions) "Should contain public function count")
      (is (= 3 (:public-functions result)) "All functions should be public")
      (is (contains? result :private-functions) "Should contain private function count")
      (is (= 0 (:private-functions result)) "No private functions in example")
      (is (contains? result :dependencies) "Should contain dependency count")
      (is (> (:dependencies result) 0) "Should detect clojure.string dependency"))))

(deftest test-process-directory
  (testing "Process directory containing test fixtures"
    (let [results (file-analyzer/process-directory test-fixtures-dir)]
      
      ;; Should process multiple files
      (is (sequential? results) "Should return a sequence of results")
      (is (> (count results) 0) "Should process at least one file")
      
      ;; Each result should be a valid analysis
      (doseq [result results]
        (is (contains? result :file) "Each result should have a file path")
        (is (contains? result :length) "Each result should have length metrics")
        (is (contains? result :halstead) "Each result should have Halstead metrics")
        (is (contains? result :cyclomatic-complexity) "Each result should have cyclomatic complexity")
        (is (contains? result :maintainability) "Each result should have maintainability metrics")))))

(deftest test-aggregate-system-metrics
  (testing "Aggregate metrics from multiple files"
    (let [file-results (file-analyzer/process-directory test-fixtures-dir)
          aggregated (file-analyzer/aggregate-system-metrics file-results)]
      
      ;; Test aggregated structure
      (is (contains? aggregated :system-summary) "Should contain system summary")
      (is (contains? aggregated :files) "Should contain individual file results")
      (is (= file-results (:files aggregated)) "Files should match original results")
      
      (let [summary (:system-summary aggregated)]
        ;; Test system summary structure
        (is (contains? summary :total-files) "Should contain total file count")
        (is (= (count file-results) (:total-files summary)) "Total files should match")
        
        (is (contains? summary :length) "Should contain aggregated length metrics")
        (is (contains? summary :halstead) "Should contain aggregated Halstead metrics")
        (is (contains? summary :cyclomatic-complexity) "Should contain total cyclomatic complexity")
        (is (contains? summary :average-cyclomatic-complexity) "Should contain average cyclomatic complexity")
        (is (contains? summary :maintainability) "Should contain system maintainability")
        (is (contains? summary :average-maintainability) "Should contain average maintainability")
        (is (contains? summary :structure) "Should contain aggregated structure metrics")
        
        ;; Test aggregated values are reasonable
        (is (> (:cyclomatic-complexity summary) 0) "Total complexity should be positive")
        (is (> (:average-cyclomatic-complexity summary) 0) "Average complexity should be positive")
        (is (map? (:maintainability summary)) "System maintainability should be a complete analysis")
        (is (number? (:average-maintainability summary)) "Average maintainability should be a number"))))

(deftest test-json-serialization
  (testing "Results can be serialized to JSON"
    (let [file-results (file-analyzer/process-directory test-fixtures-dir)
          aggregated (file-analyzer/aggregate-system-metrics file-results)
          json-str (json/write-str aggregated :escape-slash false)]
      
      ;; Should produce valid JSON
      (is (string? json-str) "Should produce a JSON string")
      (is (> (count json-str) 0) "JSON should not be empty")
      
      ;; Should be parseable back to data
      (let [parsed (json/read-str json-str :key-fn keyword)]
        (is (map? parsed) "Parsed JSON should be a map")
        (is (contains? parsed :system-summary) "Parsed should contain system summary")
        (is (contains? parsed :files) "Parsed should contain files")))))

(deftest test-empty-directory-handling
  (testing "Handle empty directory gracefully"
    ;; Create a temporary empty directory for testing
    (let [temp-dir (io/file "temp-empty-test-dir")]
      (try
        (.mkdir temp-dir)
        (let [results (file-analyzer/process-directory (.getPath temp-dir))]
          (is (sequential? results) "Should return a sequence even for empty directory")
          (is (= 0 (count results)) "Should return empty sequence for empty directory"))
        (finally
          (.delete temp-dir))))))

(deftest test-error-handling
  (testing "Handle non-existent directory"
    (is (thrown? IllegalArgumentException
                 (file-analyzer/process-directory "non-existent-directory"))
        "Should throw exception for non-existent directory"))
  
  (testing "Handle file instead of directory"
    (is (thrown? IllegalArgumentException
                 (file-analyzer/process-directory code-example-file))
        "Should throw exception when file is passed instead of directory"))))
