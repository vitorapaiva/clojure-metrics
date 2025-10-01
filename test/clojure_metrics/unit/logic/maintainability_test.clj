(ns clojure-metrics.unit.logic.maintainability-test
  (:require [clojure.test :refer :all]
            [clojure-metrics.logic.maintainability :as maintainability]))

(deftest test-calculate-base-index
  (testing "Base maintainability index calculation"
    (let [volume 100
          complexity 5
          loc 50
          result (maintainability/calculate-base-index volume complexity loc)]
      (is (number? result) "Should return a number")
      (is (< result maintainability/MAINTAINABILITY_SCALE_FACTOR) "Should be less than scale factor")))

  (testing "High complexity reduces maintainability"
    (let [volume 100
          loc 50
          low-complexity-result (maintainability/calculate-base-index volume 1 loc)
          high-complexity-result (maintainability/calculate-base-index volume 20 loc)]
      (is (> low-complexity-result high-complexity-result) "Lower complexity should yield higher maintainability")))

  (testing "High volume reduces maintainability"
    (let [complexity 5
          loc 50
          low-volume-result (maintainability/calculate-base-index 10 complexity loc)
          high-volume-result (maintainability/calculate-base-index 1000 complexity loc)]
      (is (> low-volume-result high-volume-result) "Lower volume should yield higher maintainability"))))

(deftest test-calculate-comment-bonus
  (testing "Comment bonus calculation using MIcw = 50 * sin(sqrt(2.4 * perCM))"
    (is (= 0.0 (maintainability/calculate-comment-bonus 0.0)) "0% comments = 0 bonus")
    (is (< (Math/abs (- 34.97 (maintainability/calculate-comment-bonus 0.25))) 0.1) "25% comments ≈ 34.97 bonus")
    (is (< (Math/abs (- 44.46 (maintainability/calculate-comment-bonus 0.5))) 0.1) "50% comments ≈ 44.46 bonus")
    (is (< (Math/abs (- 49.99 (maintainability/calculate-comment-bonus 1.0))) 0.1) "100% comments ≈ 49.99 bonus")))

(deftest test-normalize-index
  (testing "Index normalization"
    (is (= 0 (maintainability/normalize-index -10)) "Negative values should be normalized to 0")
    (is (= 100 (maintainability/normalize-index 150)) "Values > 100 should be normalized to 100")
    (is (= 50 (maintainability/normalize-index 50)) "Values in range should remain unchanged")
    (is (= 0 (maintainability/normalize-index 0)) "0 should remain 0")
    (is (= 100 (maintainability/normalize-index 100)) "100 should remain 100")))

(deftest test-classify-maintainability
  (testing "Maintainability classification"
    (is (= :excellent (maintainability/classify-maintainability 90)))
    (is (= :excellent (maintainability/classify-maintainability 85)))
    (is (= :good (maintainability/classify-maintainability 75)))
    (is (= :good (maintainability/classify-maintainability 70)))
    (is (= :moderate (maintainability/classify-maintainability 60)))
    (is (= :moderate (maintainability/classify-maintainability 50)))
    (is (= :poor (maintainability/classify-maintainability 35)))
    (is (= :poor (maintainability/classify-maintainability 25)))
    (is (= :critical (maintainability/classify-maintainability 20)))
    (is (= :critical (maintainability/classify-maintainability 0)))))

(deftest test-get-recommendations
  (testing "Recommendations for excellent code"
    (let [halstead-metrics {:difficulty 10 :effort 1000 :volume 200}
          recommendations (maintainability/get-recommendations :excellent halstead-metrics)]
      (is (some #(re-find #"highly maintainable" %) recommendations))
      (is (some #(re-find #"code reviews" %) recommendations))))

  (testing "Recommendations for critical code"
    (let [halstead-metrics {:difficulty 10 :effort 1000 :volume 200}
          recommendations (maintainability/get-recommendations :critical halstead-metrics)]
      (is (some #(re-find #"Immediate attention" %) recommendations))
      (is (some #(re-find #"Major refactoring" %) recommendations))))

  (testing "High difficulty detection"
    (let [halstead-metrics {:difficulty 35 :effort 1000 :volume 200}
          recommendations (maintainability/get-recommendations :good halstead-metrics)]
      (is (some #(re-find #"High difficulty detected" %) recommendations))))

  (testing "High effort detection"
    (let [halstead-metrics {:difficulty 10 :effort 15000 :volume 200}
          recommendations (maintainability/get-recommendations :good halstead-metrics)]
      (is (some #(re-find #"High effort required" %) recommendations))))

  (testing "Small codebase detection"
    (let [halstead-metrics {:difficulty 10 :effort 1000 :volume 50}
          recommendations (maintainability/get-recommendations :good halstead-metrics)]
      (is (some #(re-find #"Very small codebase" %) recommendations)))))

(deftest test-analyze-impact-factors
  (testing "Impact factors analysis"
    (let [halstead-metrics {:volume 100}
          cyclomatic-complexity 10
          length-metrics {:lloc 50}
          result (maintainability/analyze-impact-factors halstead-metrics cyclomatic-complexity length-metrics)]
      (is (contains? result :volume-impact))
      (is (contains? result :complexity-impact))
      (is (contains? result :loc-impact))
      (is (contains? result :total-negative-impact))
      (is (number? (:volume-impact result)))
      (is (number? (:complexity-impact result)))
      (is (number? (:loc-impact result)))
      (is (= (:total-negative-impact result)
             (+ (:volume-impact result) (:complexity-impact result) (:loc-impact result)))))))

(deftest test-calculate-index
  (testing "Full maintainability index calculation"
    (let [halstead-metrics {:volume 100 :difficulty 10 :effort 1000}
          cyclomatic-complexity 5
          length-metrics {:lloc 50 :comment-density 0.2}
          result (maintainability/calculate-index halstead-metrics cyclomatic-complexity length-metrics)]
      (is (contains? result :index))
      (is (contains? result :miwoc))
      (is (contains? result :micw))
      (is (contains? result :raw-index))  ; backward compatibility
      (is (contains? result :comment-bonus))  ; backward compatibility
      (is (contains? result :classification))
      (is (contains? result :impact-factors))
      (is (contains? result :recommendations))
      
      (is (number? (:index result)))
      (is (>= (:index result) 0))
      (is (<= (:index result) 100))
      (is (keyword? (:classification result)))
      (is (sequential? (:recommendations result)))))

  (testing "High comment density increases maintainability"
    (let [halstead-metrics {:volume 100 :difficulty 10 :effort 1000}
          cyclomatic-complexity 5
          low-comments {:lloc 50 :comment-density 0.1}
          high-comments {:lloc 50 :comment-density 0.5}
          low-result (maintainability/calculate-index halstead-metrics cyclomatic-complexity low-comments)
          high-result (maintainability/calculate-index halstead-metrics cyclomatic-complexity high-comments)]
      (is (< (:micw low-result) (:micw high-result)) "Higher comment density should give bigger bonus")
      (is (<= (:index low-result) (:index high-result)) "Higher comments should not decrease final index")))

  (testing "Edge case: zero values"
    (let [halstead-metrics {:volume 1 :difficulty 0 :effort 0}
          cyclomatic-complexity 1
          length-metrics {:lloc 1 :comment-density 0.0}
          result (maintainability/calculate-index halstead-metrics cyclomatic-complexity length-metrics)]
      (is (number? (:index result)) "Should handle zero values gracefully"))))

(deftest test-aggregate-maintainability-metrics
  (testing "Aggregate maintainability from multiple files"
    (let [file-analyses [{:maintainability {:index 80}}
                         {:maintainability {:index 70}}
                         {:maintainability {:index 90}}]
          aggregated-halstead {:volume 300 :difficulty 15 :effort 4500}
          aggregated-cyclomatic {:total-complexity 15}
          aggregated-length {:lloc 150 :comment-density 0.3}
          result (maintainability/aggregate-maintainability-metrics 
                  file-analyses)]
      (is (contains? result :system-maintainability))
      (is (contains? result :average-maintainability))
      (is (= 80 (:average-maintainability result)) "Should calculate correct average")
      (is (map? (:system-maintainability result)) "System maintainability should be a complete analysis")))

  (testing "Aggregate with single file"
    (let [file-analyses [{:maintainability {:index 75}}]
          aggregated-halstead {:volume 100 :difficulty 10 :effort 1000}
          aggregated-cyclomatic {:total-complexity 5}
          aggregated-length {:lloc 50 :comment-density 0.2}
          result (maintainability/aggregate-maintainability-metrics 
                  file-analyses)]
      (is (= 75 (:average-maintainability result)))))

  (testing "Aggregate with empty collection"
    (let [aggregated-halstead {:volume 0 :difficulty 0 :effort 0}
          aggregated-cyclomatic {:total-complexity 1}
          aggregated-length {:lloc 1 :comment-density 0.0}
          result (maintainability/aggregate-maintainability-metrics 
                  [])]
      (is (= 0 (:average-maintainability result))))))

(deftest test-constants
  (testing "Constants are properly defined"
    (is (= 171 maintainability/MAINTAINABILITY_SCALE_FACTOR))
    (is (= 5.2 maintainability/VOLUME_COEFFICIENT))
    (is (= 0.23 maintainability/COMPLEXITY_COEFFICIENT))
    (is (= 16.2 maintainability/LOC_COEFFICIENT))
    (is (= 50 maintainability/COMMENT_BONUS_MULTIPLIER))
    (is (= 100 maintainability/MAX_MAINTAINABILITY_INDEX))
    (is (= 0 maintainability/MIN_MAINTAINABILITY_INDEX))))
