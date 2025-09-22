(ns clojure-metrics.resources.fixtures.clj-kondo-fixtures
  "Fixtures simulating clj-kondo AST output for testing")

(def simple-function-ast
  "AST for a simple function with basic operations"
  {:var-definitions [{:filename "test.clj"
                      :row 1
                      :col 1
                      :name 'add-numbers
                      :ns 'test
                      :defined-by 'clojure.core/defn
                      :private false
                      :macro false}]
   
   :var-usages [{:filename "test.clj"
                 :row 2
                 :col 3
                 :name '+
                 :to 'clojure.core/+
                 :from-var 'add-numbers
                 :arity 2}
                {:filename "test.clj"
                 :row 2
                 :col 5
                 :name 'a
                 :from-var 'add-numbers}
                {:filename "test.clj"
                 :row 2
                 :col 7
                 :name 'b
                 :from-var 'add-numbers}]
   
   :namespace-usages [{:filename "test.clj"
                       :row 1
                       :col 1
                       :from 'test
                       :to 'clojure.core}]
   
   :locals [{:filename "test.clj"
             :row 1
             :col 15
             :name 'a
             :scope-end-row 2}
            {:filename "test.clj"
             :row 1
             :col 17
             :name 'b
             :scope-end-row 2}]
   
   :keywords []})

(def complex-function-ast
  "AST for a function with control flow and more complexity"
  {:var-definitions [{:filename "test.clj"
                      :row 1
                      :col 1
                      :name 'process-numbers
                      :ns 'test
                      :defined-by 'clojure.core/defn
                      :private false
                      :macro false}]
   
   :var-usages [{:filename "test.clj"
                 :row 2
                 :col 3
                 :name 'if
                 :to 'clojure.core/if
                 :from-var 'process-numbers
                 :arity 3}
                {:filename "test.clj"
                 :row 2
                 :col 6
                 :name '>
                 :to 'clojure.core/>
                 :from-var 'process-numbers
                 :arity 2}
                {:filename "test.clj"
                 :row 3
                 :col 5
                 :name '*
                 :to 'clojure.core/*
                 :from-var 'process-numbers
                 :arity 2}
                {:filename "test.clj"
                 :row 4
                 :col 5
                 :name '+
                 :to 'clojure.core/+
                 :from-var 'process-numbers
                 :arity 2}
                {:filename "test.clj"
                 :row 5
                 :col 3
                 :name 'cond
                 :to 'clojure.core/cond
                 :from-var 'process-numbers
                 :arity 6}
                {:filename "test.clj"
                 :row 6
                 :col 5
                 :name '=
                 :to 'clojure.core/=
                 :from-var 'process-numbers
                 :arity 2}
                {:filename "test.clj"
                 :row 8
                 :col 5
                 :name '<
                 :to 'clojure.core/<
                 :from-var 'process-numbers
                 :arity 2}]
   
   :namespace-usages [{:filename "test.clj"
                       :row 1
                       :col 1
                       :from 'test
                       :to 'clojure.core}]
   
   :locals [{:filename "test.clj"
             :row 1
             :col 20
             :name 'x
             :scope-end-row 10}]
   
   :keywords [{:filename "test.clj"
               :row 7
               :col 6
               :name :zero}
              {:filename "test.clj"
               :row 9
               :col 6
               :name :negative}
              {:filename "test.clj"
               :row 11
               :col 6
               :name :positive}]})

(def multiple-functions-ast
  "AST for multiple functions in one file"
  {:var-definitions [{:filename "test.clj"
                      :row 1
                      :col 1
                      :name 'factorial
                      :ns 'test
                      :defined-by 'clojure.core/defn
                      :private false
                      :macro false}
                     {:filename "test.clj"
                      :row 7
                      :col 1
                      :name 'fibonacci
                      :ns 'test
                      :defined-by 'clojure.core/defn
                      :private false
                      :macro false}
                     {:filename "test.clj"
                      :row 13
                      :col 1
                      :name 'helper-function
                      :ns 'test
                      :defined-by 'clojure.core/defn-
                      :private true
                      :macro false}]
   
   :var-usages [{:filename "test.clj"
                 :row 2
                 :col 3
                 :name 'if
                 :from-var 'factorial
                 :arity 3}
                {:filename "test.clj"
                 :row 2
                 :col 6
                 :name '<=
                 :from-var 'factorial
                 :arity 2}
                {:filename "test.clj"
                 :row 4
                 :col 5
                 :name '*
                 :from-var 'factorial
                 :arity 2}
                {:filename "test.clj"
                 :row 4
                 :col 9
                 :name 'factorial
                 :from-var 'factorial
                 :arity 1}
                {:filename "test.clj"
                 :row 4
                 :col 20
                 :name 'dec
                 :from-var 'factorial
                 :arity 1}
                {:filename "test.clj"
                 :row 8
                 :col 3
                 :name 'cond
                 :from-var 'fibonacci
                 :arity 6}
                {:filename "test.clj"
                 :row 9
                 :col 5
                 :name '<=
                 :from-var 'fibonacci
                 :arity 2}
                {:filename "test.clj"
                 :row 11
                 :col 5
                 :name '+
                 :from-var 'fibonacci
                 :arity 2}]
   
   :namespace-usages [{:filename "test.clj"
                       :row 1
                       :col 1
                       :from 'test
                       :to 'clojure.core}]
   
   :locals [{:filename "test.clj"
             :row 1
             :col 12
             :name 'n
             :scope-end-row 5}
            {:filename "test.clj"
             :row 7
             :col 12
             :name 'n
             :scope-end-row 12}]
   
   :keywords []})

(def empty-file-ast
  "AST for an empty or minimal file"
  {:var-definitions []
   :var-usages []
   :namespace-usages []
   :locals []
   :keywords []})
