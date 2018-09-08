(ns clj-so.mongo
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            ;; [clojure.string :as str]

            [clojure.tools.logging :as log])
  (:gen-class)
  )


(def conn (mg/connect))
(def db   (mg/get-db conn "so"))

;; (def coll "haskell")

;; (mc/insert db coll {:b 10 :n 102 :text "aaaaa"})


(defn get-collection
  [col]
  (mc/find-maps db col))


(defn insert-records
  [col reqs]
  (doall
   (map #(mc/insert db col %1) reqs)))

