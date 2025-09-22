(ns clojure-metrics.unit.logic.loc-test
  (:require [clojure.test :refer :all]
            [clojure-metrics.logic.loc :as loc]))

(def sample-code
  "(ns example
  \"This is a namespace\"
  (:require [clojure.string :as str]))

(defn add-numbers
  \"Adds two numbers together\"
  [a b]
  (+ a b))

;; This is a comment
(defn multiply
  [x y]
  (* x y))

;; Another comment
;; Multiline comment
(defn process-list
  [items]
  (when-not (empty? items)
    (map inc items)))")

(def code-with-empty-lines
  "
(defn example []
  
  (println \"hello\")
  
  ;; comment
  
  (+ 1 2))

")

(deftest test-empty-line?
  (testing "Empty line detection"
    (is (loc/empty-line? "") "Empty string should be empty line")
    (is (loc/empty-line? "   ") "Whitespace only should be empty line")
    (is (loc/empty-line? "\t\t") "Tabs only should be empty line")
    (is (not (loc/empty-line? "code")) "Code should not be empty line")
    (is (not (loc/empty-line? "  ;; comment")) "Comment should not be empty line")))

(deftest test-comment-line?
  (testing "Comment line detection"
    (is (loc/comment-line? ";; This is a comment"))
    (is (loc/comment-line? "  ;; Indented comment"))
    (is (loc/comment-line? "; Single semicolon"))
    (is (not (loc/comment-line? "")) "Empty line should not be comment")
    (is (not (loc/comment-line? "   ")) "Whitespace should not be comment")
    (is (not (loc/comment-line? "(defn test [])")) "Code should not be comment")
    (is (not (loc/comment-line? "  (+ 1 2) ;; inline comment")) "Inline comment should not be comment line")))

(deftest test-calculate-cloc
  (testing "Total lines calculation"
    (let [lines ["line1" "line2" "" "line4"]]
      (is (= 4 (loc/calculate-cloc lines)) "Should count all lines including empty")))

  (testing "Empty collection"
    (is (= 0 (loc/calculate-cloc [])) "Empty collection should return 0")))

(deftest test-calculate-comment-lines
  (testing "Comment lines counting"
    (let [lines [";; Comment 1"
                 "(defn test [])"
                 ";; Comment 2"
                 "  ;; Indented comment"
                 "(+ 1 2)"]]
      (is (= 3 (loc/calculate-comment-lines lines)) "Should count all comment lines")))

  (testing "No comment lines"
    (let [lines ["(defn test [])" "(+ 1 2)" ""]]
      (is (= 0 (loc/calculate-comment-lines lines)) "Should return 0 when no comments")))

  (testing "Empty collection"
    (is (= 0 (loc/calculate-comment-lines [])) "Empty collection should return 0")))

(deftest test-calculate-loc
  (testing "Lines without comments"
    (let [lines [";; Comment"
                 "(defn test [])"
                 ";; Another comment"
                 "(+ 1 2)"
                 ""]]
      (is (= 3 (loc/calculate-loc lines)) "Should exclude comment lines but include empty lines")))

  (testing "All comment lines"
    (let [lines [";; Comment 1" ";; Comment 2"]]
      (is (= 0 (loc/calculate-loc lines)) "Should return 0 when all lines are comments")))

  (testing "No comment lines"
    (let [lines ["(defn test [])" "(+ 1 2)" ""]]
      (is (= 3 (loc/calculate-loc lines)) "Should count all lines when no comments"))))

(deftest test-calculate-lloc
  (testing "Lines without empty lines"
    (let [lines ["(defn test [])"
                 ""
                 "(+ 1 2)"
                 "   "
                 ";; comment"]]
      (is (= 3 (loc/calculate-lloc lines)) "Should exclude empty lines but include comments")))

  (testing "All empty lines"
    (let [lines ["" "   " "\t"]]
      (is (= 0 (loc/calculate-lloc lines)) "Should return 0 when all lines are empty")))

  (testing "No empty lines"
    (let [lines ["(defn test [])" "(+ 1 2)" ";; comment"]]
      (is (= 3 (loc/calculate-lloc lines)) "Should count all lines when no empty lines"))))

(deftest test-calculate-comment-density
  (testing "Comment density calculation"
    (is (= 25.0 (loc/calculate-comment-density 4 1)) "1 comment in 4 lines = 25%")
    (is (= 50.0 (loc/calculate-comment-density 10 5)) "5 comments in 10 lines = 50%")
    (is (= 0.0 (loc/calculate-comment-density 10 0)) "0 comments = 0%")
    (is (= 100.0 (loc/calculate-comment-density 5 5)) "All comments = 100%"))

  (testing "Edge cases"
    (is (= 0.0 (loc/calculate-comment-density 0 0)) "0 total lines should return 0%")
    (is (= 0.0 (loc/calculate-comment-density 0 1)) "0 total lines should return 0% even with comments")))

(deftest test-calculate-length-metrics
  (testing "Full length metrics calculation"
    (let [result (loc/calculate-length-metrics sample-code)
          lines (clojure.string/split-lines sample-code)]
      (is (contains? result :cloc) "Should contain cloc")
      (is (contains? result :loc) "Should contain loc")
      (is (contains? result :lloc) "Should contain lloc")
      (is (contains? result :comment-density) "Should contain comment-density")
      
      (is (= (count lines) (:cloc result)) "CLOC should equal total lines")
      (is (< (:loc result) (:cloc result)) "LOC should be less than CLOC (has comments)")
      (is (< (:lloc result) (:cloc result)) "LLOC should be less than CLOC (has empty lines)")
      (is (> (:comment-density result) 0) "Should have positive comment density")))

  (testing "Code with empty lines"
    (let [result (loc/calculate-length-metrics code-with-empty-lines)]
      (is (> (:cloc result) (:lloc result)) "CLOC should be greater than LLOC due to empty lines")
      (is (> (:comment-density result) 0) "Should detect comments")))

  (testing "Empty code"
    (let [result (loc/calculate-length-metrics "")]
      (is (= 1 (:cloc result)) "Empty string creates one empty line")
      (is (= 1 (:loc result)) "Empty string LOC")
      (is (= 0 (:lloc result)) "Empty string LLOC should be 0")
      (is (= 0.0 (:comment-density result)) "Empty string should have 0% comment density")))

  (testing "Only comments"
    (let [comment-only ";; Comment 1\n;; Comment 2"
          result (loc/calculate-length-metrics comment-only)]
      (is (= 2 (:cloc result)) "Should count comment lines in CLOC")
      (is (= 0 (:loc result)) "Should have 0 LOC when only comments")
      (is (= 2 (:lloc result)) "Should count comments in LLOC")
      (is (= 100.0 (:comment-density result)) "Should be 100% comment density"))))

(deftest test-aggregate-length-metrics
  (testing "Aggregate metrics from multiple files"
    (let [file-analyses [{:length {:cloc 10 :loc 8 :lloc 9}}
                         {:length {:cloc 20 :loc 18 :lloc 17}}
                         {:length {:cloc 5 :loc 5 :lloc 4}}]
          result (loc/aggregate-length-metrics file-analyses)]
      (is (= 35 (:cloc result)) "Should sum all CLOC values")
      (is (= 31 (:loc result)) "Should sum all LOC values")
      (is (= 30 (:lloc result)) "Should sum all LLOC values")
      (is (> (:comment-density result) 0) "Should calculate aggregated comment density")))

  (testing "Aggregate with single file"
    (let [file-analyses [{:length {:cloc 10 :loc 8 :lloc 9}}]
          result (loc/aggregate-length-metrics file-analyses)]
      (is (= 10 (:cloc result)))
      (is (= 8 (:loc result)))
      (is (= 9 (:lloc result)))))

  (testing "Aggregate with empty collection"
    (let [result (loc/aggregate-length-metrics [])]
      (is (= 0 (:cloc result)))
      (is (= 0 (:loc result)))
      (is (= 0 (:lloc result)))
      (is (= 0.0 (:comment-density result))))))

