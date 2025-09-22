(ns clojure-metrics.integration.core-test
  "Integration test comparing actual output with expected JSON fixture."
  (:require [clojure.test :refer :all]
            [clojure-metrics.core :as core]
            [clojure.data.json :as json]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.io StringWriter]))

(defn capture-output [f]
  (let [output (StringWriter.)]
    (binding [*out* output]
      (try (f) (catch Exception e nil))
      (.toString output))))

(defn parse-json-output [output]
  (let [json-line (first (filter #(str/starts-with? % "{") (str/split-lines output)))]
    (when json-line (json/read-str json-line :key-fn keyword))))

(deftest test-code-example-analysis
  (testing "Analysis of code_example.clj matches expected JSON output"
    (let [;; Create temporary directory with only code_example.clj
          temp-dir "temp_test_dir"
          _ (io/make-parents (str temp-dir "/dummy"))
          _ (with-open [in (io/input-stream (io/resource "fixtures/code_example.clj.sample"))
                        out (io/output-stream (io/file temp-dir "code_example.clj"))]
              (io/copy in out))
          
          ;; Load expected result
          expected-json (json/read-str 
                         (slurp (io/resource "fixtures/expected_code_example_result.json"))
                         :key-fn keyword)
          
          ;; Run analysis
          output (capture-output #(core/-main "-p" temp-dir))
          actual-result (parse-json-output output)]
      
      ;; Cleanup
      (.delete (io/file temp-dir "code_example.clj"))
      (.delete (io/file temp-dir))
      
      ;; Normalize file paths for comparison
      (is (= expected-json actual-result)
          "Actual analysis result should match expected JSON fixture"))))