(ns clojure-metrics.logic.halstead
  "Implementation of Halstead complexity measures for Clojure code.
   
   Halstead measures include:
   - n1: number of unique operators
   - n2: number of unique operands  
   - N1: total number of operators
   - N2: total number of operands
   - Vocabulary (n): n1 + n2
   - Length (N): N1 + N2
   - Volume: N * log2(n)
   - Difficulty: (n1/2) * (N2/n2)
   - Effort: Difficulty * Volume")

;; Constants for Halstead complexity calculation
(def LOG_BASE_2 
  "Logarithm base for volume calculation in Halstead metrics."
  2)

(def OPERATOR_DIFFICULTY_DIVISOR 
  "Divisor for operator count in difficulty calculation (n1/2)."
  2.0)

(def ZERO_COMPLEXITY 
  "Zero value for cases where calculation is not possible."
  0)

;; Operator definitions
(def clojure-operators
  "Set of common Clojure operators."
  #{;; Arithmetic operators
    '+ '- '* '/ 'mod 'inc 'dec 'max 'min
    ;; Comparison operators
    '= 'not= '< '> '<= '>= 'compare
    ;; Logical operators
    'and 'or 'not
    ;; Control flow operators
    'if 'when 'when-not 'cond 'case 'do 'let 'loop 'recur
    ;; Function operators
    'fn 'defn 'defn- 'defmacro 'def 'defonce
    ;; Sequence operators
    'first 'rest 'next 'last 'count 'empty? 'seq 'cons 'conj 'assoc 'dissoc
    'map 'filter 'reduce 'take 'drop 'take-while 'drop-while
    ;; Namespace operators
    'ns 'require 'use 'import 'refer
    ;; Other common operators
    'try 'catch 'finally 'throw 'assert 'quote 'unquote})

;; Core functions
(defn count-halstead
  "Counts basic Halstead metrics."
  [operators operands]
  (let [unique-operators (set operators)
        unique-operands (set operands)
        n1 (count unique-operators)
        n2 (count unique-operands)
        N1 (count operators)
        N2 (count operands)]
    {:n1 n1
     :n2 n2
     :N1 N1
     :N2 N2
     :unique-operators unique-operators
     :unique-operands unique-operands}))

(defn calculate-derived-metrics
  "Calculates all derived Halstead metrics including the complete set of 8 measures."
  [{:keys [n1 n2 N1 N2]}]
  (let [vocabulary (+ n1 n2)
        length (+ N1 N2)
        
        ;; Calculated program length: N' = n1 * log2(n1) + n2 * log2(n2)
        calculated-length (if (and (> n1 ZERO_COMPLEXITY) (> n2 ZERO_COMPLEXITY))
                           (+ (* n1 (/ (Math/log n1) (Math/log LOG_BASE_2)))
                              (* n2 (/ (Math/log n2) (Math/log LOG_BASE_2))))
                           ZERO_COMPLEXITY)
        
        ;; Volume: V = N * log2(n)
        volume (if (> vocabulary ZERO_COMPLEXITY)
                 (* length (/ (Math/log vocabulary) (Math/log LOG_BASE_2)))
                 ZERO_COMPLEXITY)
        
        ;; Difficulty: D = (n1/2) * (N2/n2)
        difficulty (if (and (> n2 ZERO_COMPLEXITY) (> n1 ZERO_COMPLEXITY))
                     (* (/ n1 OPERATOR_DIFFICULTY_DIVISOR) (/ N2 n2))
                     ZERO_COMPLEXITY)
        
        ;; Effort: E = D * V
        effort (* difficulty volume)
        
        ;; Level: L = 1/D (inverse of difficulty)
        level (if (> difficulty ZERO_COMPLEXITY)
                (/ 1.0 difficulty)
                ZERO_COMPLEXITY)
        
        ;; Time required to program: T = E/18 seconds
        time (/ effort 18.0)
        
        ;; Number of delivered bugs: B = V/3000
        bugs (/ volume 3000.0)
        
        ;; Intelligent content: I = L * V
        intelligent-content (* level volume)]
    
    {:vocabulary vocabulary
     :length length
     :calculated-length calculated-length
     :volume volume
     :difficulty difficulty
     :effort effort
     :level level
     :time time
     :bugs bugs
     :intelligent-content intelligent-content}))

(defn extract-operators-from-ast
  "Extracts operators from clj-kondo AST with frequency count."
  [ast]
  (let [var-usages (:var-usages ast)
        var-definitions (:var-definitions ast)]
    (concat
      ;; Operators from var usages
      (->> var-usages
           (map :name)
           (filter #(contains? clojure-operators %)))
      ;; Function definitions are also operators
      (->> var-definitions
           (filter #(contains? #{'clojure.core/defn 'clojure.core/defn-
                                 'clojure.core/defmacro 'clojure.core/def
                                 'clojure.core/defonce} (:defined-by %)))
           (map (fn [_] 'defn))) ; Count each function definition as defn operator
      ;; Built-in operators from the analysis
      (->> var-usages
           (filter #(contains? #{'clojure.core/+ 'clojure.core/-
                                'clojure.core/* 'clojure.core//
                                'clojure.core/= 'clojure.core/not=
                                'clojure.core/< 'clojure.core/>
                                'clojure.core/<= 'clojure.core/>=} (:to %)))
           (map #(symbol (name (:to %))))))))

(defn extract-operands-from-ast
  "Extracts operands from clj-kondo AST including literals and variables."
  [ast]
  (let [var-usages (:var-usages ast)
        var-defs (:var-definitions ast)
        locals (:locals ast)
        keywords (:keywords ast)]
    (concat
      ;; Variable usages that are not operators (actual variable references)
      (->> var-usages
           (map :name)
           (remove #(contains? clojure-operators %)))
      ;; Variable names being defined (only the names, not the definition itself)
      (->> var-defs
           (map :name)
           (remove nil?)) ; Remove any nil names
      ;; Local bindings (let, fn params, etc.)
      (->> locals
           (map :name)
           (remove nil?))
      ;; Keywords as operands (literals)
      (->> keywords
           (map :name)
           (remove nil?)))))

(defn calculate-halstead-from-ast
  "Calculates Halstead metrics using clj-kondo AST (more accurate)."
  [ast _source-code]
  (let [operators (extract-operators-from-ast ast)
        operands (extract-operands-from-ast ast)
        basic-metrics (count-halstead operators operands)
        derived-metrics (calculate-derived-metrics basic-metrics)]
    (merge basic-metrics derived-metrics)))

(defn aggregate-halstead-metrics
  "Aggregates Halstead metrics from multiple files with complete set of measures."
  [file-analyses]
  (let [total-n1 (reduce + (map #(get-in % [:halstead :n1]) file-analyses))
        total-n2 (reduce + (map #(get-in % [:halstead :n2]) file-analyses))
        total-N1 (reduce + (map #(get-in % [:halstead :N1]) file-analyses))
        total-N2 (reduce + (map #(get-in % [:halstead :N2]) file-analyses))
        
        ;; Recalculate all system-wide derived metrics using the complete formula set
        system-metrics (calculate-derived-metrics {:n1 total-n1
                                                   :n2 total-n2
                                                   :N1 total-N1
                                                   :N2 total-N2})]
    
    (merge {:n1 total-n1
            :n2 total-n2
            :N1 total-N1
            :N2 total-N2}
           system-metrics)))