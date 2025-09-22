(defproject clojure-metrics "0.0.1"
  :description "Comprehensive static analysis tool for Clojure code with quality metrics"
  :url "https://github.com/your-username/clojure-metrics"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :scm {:name "git"
        :url "https://github.com/your-username/clojure-metrics"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-kondo/clj-kondo "2025.09.19"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/tools.cli "1.0.219"]]
  :main ^:skip-aot clojure-metrics.core
  :target-path "target/%s"
  :resource-paths ["resources"]
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
             :test {:resource-paths ["test/clojure_metrics/resources"]}})
