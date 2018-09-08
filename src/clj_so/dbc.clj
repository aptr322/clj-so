(ns clj-so.dbc
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as jdbc]

            [cheshire.core :as json]

            [clojure.tools.logging :as log])
  (:import (java.sql DatabaseMetaData))
  (:gen-class))


(def dbc {:classname "com.mysql.jdbc.Driver" ; must be in classpath
          :subprotocol "mysql"
          :subname (str "//localhost:" 3306 "/cljso")
          :user "root"
          :password ""
          :useSSL false})

;;
;;  map-indexed
;; (map-indexed vector (:comments r))
;;

;; (let [{:keys [a b] :or {a 20 b 30} :as params } {:aa 1 :b 2 :c 3}] (println a b params))

(defn clear-db
  []
  (do
    (jdbc/execute! dbc ["DELETE FROM tags"])
    (jdbc/execute! dbc ["DELETE FROM questions"])
    (jdbc/execute! dbc ["DELETE FROM answers"])
    (jdbc/execute! dbc ["DELETE FROM qcomments"])
    (jdbc/execute! dbc ["DELETE FROM acomments"])))

;;
(defn write-table
  [t m]
  (jdbc/insert! dbc t m))

;;
(defn comment-to-qcomm
  [c]
  (into (select-keys c [:comment_id :creation_date :body :score]) {:question_id (:post_id c)}))

(defn comment-to-acomm
  [c]
  (into (select-keys c [:comment_id :creation_date :body :score]) {:answer_id (:post_id c)}))

(defn proc-tags
  [q]
  (let [qid    (:question_id q)
        tags   (:tags        q)]
    ;; (log/debug "proc-tags:" qid tags)
    (doall
     (map #(write-table :tags {:question_id qid :tag %}) tags))
    ))

(defn proc-qcomm
  [q]
  (let [cs  (:comments q)]
    (doall
     (map #(write-table :qcomments (comment-to-qcomm %1)) cs))))

(defn proc-acomm
  [a]
  (let [cs  (:comments a)]
    (doall
     (map #(write-table :acomments (comment-to-acomm %1)) cs))))

(defn proc-answer
  [a]
  (let [flds    (select-keys a [:answer_id :creation_date :question_id :body :score])]
    (write-table :answers flds)
    (proc-acomm a)
    ))

(defn proc-answers
  [q]
  (doall
   (map proc-answer (:answers q))))

(defn proc-question
  [q]
  (let [flds   (select-keys q [:question_id
                               :creation_date
                               :title
                               :body
                               :view_count
                               :last_activity_date
                               :last_edit_date
                               :score
                               :link])]
    (log/debug "new: " (:question_id q) (:title q))
    (try
      (do
        (proc-tags  q)
        (write-table :questions flds)
        (proc-qcomm q)
        (proc-answers q))
      (catch Exception ex (println "Exception " (:question_id q))))
    ))



;; haskell sql exceptions
(def haskell-exc-ids
  #{12288548 8379824 8379011 9301989 7782735 9302758 7523288 22235888 10665850 7382171 8695722 7304406})


;;
;; (jdbc/query dbc ["SELECT question_id FROM tags WHERE tag = 'haskell'"])


;; (j/with-db-metadata [m db-spec]
;;                     (->> (.getColumns m "mycatalog" nil "person" nil)
;;                          (j/metadata-query)
;;                          (map :column_name)))


;; (def answers-columns (jdbc/metadata-query (.getColumns md "cljso" nil "answers" nil)))


(defn find-tables
  [db-spec]
  (jdbc/with-db-metadata [m db-spec]
    (jdbc/metadata-result (.getTables m nil nil nil (into-array String ["TABLE"])))))

(defn find-columns [db-spec table]
  (jdbc/with-db-metadata [^DatabaseMetaData m db-spec]
    (jdbc/metadata-query (.getColumns m nil nil table nil))))

;; (defn find-table [db & {:keys [types] :or {types ["TABLE" "VIEW"]} }]
;;   (println types)
;;   (jdbc/with-db-metadata [^DatabaseMetaData m db]
;;     (jdbc/metadata-query (.getTables m nil nil nil types))))


(defn fdestruct [a0 & {:keys [p0 p1]}]
  (println p0))
