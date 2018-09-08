(ns clj-so.ws
  (:require [clj-so.dbc :refer :all]
            ;; [hiccup.core  :as hc]

            [lobos.analyzer :as la]

            [clojure.tools.logging :as log])
  (:gen-class))


;; (def qs (doall (map read-json-file data-files))) 
;; (def q0 (nth qs 12001))


;; (defn src-to-hiccup [s] 
;;   (let [h  (as-hiccup (parse s))]
;;     h))


