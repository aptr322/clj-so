(defproject clj-so "0.1.0-SNAPSHOT"
  :description "collect stack overflow data"
  :url "http://none.none"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.cli "0.4.1"]

                 [clj-http "3.9.1"]
                 [cheshire "5.8.1"]
                 [clj-time "0.14.4"]

                 [com.novemberain/monger "3.1.0"]

                 [org.clojure/java.jdbc "0.7.8"]
                 [mysql/mysql-connector-java "5.1.39"]
                 ;; [mysql/mysql-connector-java "8.0.8"]
                 ;; [lobos "1.0.0-beta3"]
                 
                 [hiccup "1.0.5"]
                 ;; [hickory "0.7.1"]

                 [cprop "0.1.13"]
                 
                 [org.clojure/tools.logging "0.4.1"]
                 [log4j/log4j "1.2.17"]]
  :plugins [[lein-ancient "0.6.15"]]
  ;; :main ^:skip-aot clj-so.core
  ;; :main ^:skip-aot clj-so.dbc
  :main ^:skip-aot clj-so.ws
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:jvm-opts ["-Dconf=../config.edn"]}})
