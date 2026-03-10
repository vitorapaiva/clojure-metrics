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
  (testing "Comment lines calculation (PHPMetrics: cloc = Comment Lines of Code)"
    (let [lines [";; Comment 1" "line2" "" ";; Comment 2"]]
      (is (= 2 (loc/calculate-cloc lines)) "Should count only comment lines")))

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
  (testing "Total physical lines (PHPMetrics: loc = Lines of Code)"
    (let [lines [";; Comment"
                 "(defn test [])"
                 ";; Another comment"
                 "(+ 1 2)"
                 ""]]
      (is (= 5 (loc/calculate-loc lines)) "Should count all lines including comments and empty")))

  (testing "All comment lines"
    (let [lines [";; Comment 1" ";; Comment 2"]]
      (is (= 2 (loc/calculate-loc lines)) "Should count all lines when all are comments")))

  (testing "No comment lines"
    (let [lines ["(defn test [])" "(+ 1 2)" ""]]
      (is (= 3 (loc/calculate-loc lines)) "Should count all lines when no comments"))))

(deftest test-calculate-lloc
  (testing "Logical lines = code only, excluding comments and empty (PHPMetrics standard)"
    (let [lines ["(defn test [])"
                 ""
                 "(+ 1 2)"
                 "   "
                 ";; comment"]]
      (is (= 2 (loc/calculate-lloc lines)) "Should exclude empty lines and comments, only code")))

  (testing "All empty lines"
    (let [lines ["" "   " "\t"]]
      (is (= 0 (loc/calculate-lloc lines)) "Should return 0 when all lines are empty")))

  (testing "No empty lines, no comments"
    (let [lines ["(defn test [])" "(+ 1 2)" "  (inc x)"]]
      (is (= 3 (loc/calculate-lloc lines)) "Should count all code lines when no empty or comments"))))

(deftest test-calculate-comment-density
  (testing "Comment density as ratio 0-1 (PHPMetrics: CM = cloc/loc)"
    (is (= 0.25 (loc/calculate-comment-density 4 1)) "1 comment in 4 lines = 0.25")
    (is (= 0.5 (loc/calculate-comment-density 10 5)) "5 comments in 10 lines = 0.5")
    (is (= 0.0 (loc/calculate-comment-density 10 0)) "0 comments = 0")
    (is (= 1.0 (loc/calculate-comment-density 5 5)) "All comments = 1.0"))

  (testing "Edge cases"
    (is (= 0.0 (loc/calculate-comment-density 0 0)) "0 total lines should return 0")
    (is (= 0.0 (loc/calculate-comment-density 0 1)) "0 total lines should return 0 even with comments")))

(deftest test-calculate-length-metrics
  (testing "Full length metrics calculation (PHPMetrics standard)"
    (let [result (loc/calculate-length-metrics sample-code)
          lines (clojure.string/split-lines sample-code)]
      (is (contains? result :cloc) "Should contain cloc")
      (is (contains? result :loc) "Should contain loc")
      (is (contains? result :lloc) "Should contain lloc")
      (is (contains? result :comment-density) "Should contain comment-density")
      
      (is (= (count lines) (:loc result)) "LOC should equal total lines")
      (is (<= (:cloc result) (:loc result)) "CLOC (comments) should be <= LOC")
      (is (<= (:lloc result) (:loc result)) "LLOC (code only) should be <= LOC")
      (is (>= (:comment-density result) 0) "Comment density should be 0-1 ratio")))

  (testing "Code with empty lines"
    (let [result (loc/calculate-length-metrics code-with-empty-lines)]
      (is (<= (:lloc result) (:loc result)) "LLOC should be <= LOC")
      (is (>= (:comment-density result) 0) "Should have valid comment density")))

  (testing "Empty code"
    (let [result (loc/calculate-length-metrics "")]
      (is (= 1 (:loc result)) "Empty string creates one line")
      (is (= 0 (:cloc result)) "Empty string has 0 comment lines")
      (is (= 0 (:lloc result)) "Empty string LLOC should be 0")
      (is (= 0.0 (:comment-density result)) "Empty string should have 0 comment density")))

  (testing "Only comments"
    (let [comment-only ";; Comment 1\n;; Comment 2"
          result (loc/calculate-length-metrics comment-only)]
      (is (= 2 (:cloc result)) "Should count comment lines")
      (is (= 2 (:loc result)) "LOC = total lines")
      (is (= 0 (:lloc result)) "LLOC = 0 when only comments (no code)")
      (is (= 1.0 (:comment-density result)) "Should be 100% comment density (1.0)"))))

(deftest test-aggregate-length-metrics
  (testing "Aggregate metrics from multiple files (PHPMetrics: cloc<=loc, lloc<=loc)"
    (let [file-analyses [{:length {:cloc 2 :loc 10 :lloc 8}}
                         {:length {:cloc 3 :loc 20 :lloc 17}}
                         {:length {:cloc 1 :loc 5 :lloc 4}}]
          result (loc/aggregate-length-metrics file-analyses)]
      (is (= 6 (:cloc result)) "Should sum all CLOC (comment) values")
      (is (= 35 (:loc result)) "Should sum all LOC values")
      (is (= 29 (:lloc result)) "Should sum all LLOC values")
      (is (> (:comment-density result) 0) "Should calculate aggregated comment density")))

  (testing "Aggregate with single file"
    (let [file-analyses [{:length {:cloc 2 :loc 10 :lloc 8}}]
          result (loc/aggregate-length-metrics file-analyses)]
      (is (= 2 (:cloc result)))
      (is (= 10 (:loc result)))
      (is (= 8 (:lloc result)))))

  (testing "Aggregate with empty collection"
    (let [result (loc/aggregate-length-metrics [])]
      (is (= 0 (:cloc result)))
      (is (= 0 (:loc result)))
      (is (= 0 (:lloc result)))
      (is (= 0.0 (:comment-density result))))))

