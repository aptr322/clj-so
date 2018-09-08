(ns clj-so.data
  (:require ;;[clj-so.mongo :refer :all]

            [clj-time.core :as t]
            [clj-time.coerce :as tc]
;;            [clj-time.format :as tf]

            [clj-so.config :refer :all]
            [clj-so.util :as u]

            [clojure.tools.logging :as log])
  (:gen-class))



;; (def data-files (u/list-dir (str (:data-dir app-config) "/" (:topic-tag app-config))))
;; (def qs (doall (map u/read-json-file data-files)))


(defn total-answers [q]
 (reduce + 0 (map #(count (:answers %1)) q)))



;; date-time
(defn dt [v] (tc/from-long (* v 1000)))
(defn dt-day [dt] (t/date-time (t/year dt) (t/month dt) (t/day dt)))
(defn dt-month [dt] (t/date-time (t/year dt) (t/month dt)))


(defn create-month-counts
  [q]
  (sort (frequencies (map #(-> %1 :creation_date dt dt-month) q))))



;; check empty questions
;;(filter #(nil? (:question_id %)) qs)


(defn comments-count [q] (count (:comments q)))

(defn answers-comments-count [a] (reduce + 0 (map comments-count a)))

(defn total-comments [q]
  (let [qcs   (reduce + 0 (map comments-count q))
        acs   (reduce + 0 (map #(answers-comments-count (:answers %)) q))]
    (+ qcs acs)))


