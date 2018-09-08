(ns clj-so.util
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.tools.logging :as log])
  (:gen-class))

;;
(defn read-json-file [filename]
  (json/decode (slurp filename) true))


(defn list-dir
  [path]
  (let [df0  (seq (.list (io/file path)))]
    (map #(str path "/" %) df0)))

