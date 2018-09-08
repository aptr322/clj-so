(ns clj-so.core
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]

            [clj-http.client :as http]
            [cheshire.core :as json]

            [clj-time.coerce :as tc]
            [clj-time.format :as tf]
            
            [clj-so.config :refer :all]

            [clojure.tools.logging :as log])
  (:gen-class))

(def clj-app-key (:clj-app-key app-config))
(def topic-tag   (:topic-tag app-config))


;; ----
(def stackexchange-base-url "https://api.stackexchange.com/2.2/")
(def site-params {:tagged topic-tag :site "stackoverflow" :key clj-app-key })

(def extra-params (atom {}))


;; ---------------
(defn date-to-epoch [s] (tc/to-epoch (tf/parse s)))

(defn epoch-to-string [d]
  (tc/to-string (tc/from-long (* 1000 d))))



;; 
(defn write-file [fname s]
  (with-open [wrt (io/writer fname)]
    (.write wrt s)))

(defn write-question [q]
  (with-open [wrt (io/writer  (str "data/" topic-tag "/" (:question_id q) ".json") :append false)]
    (.write wrt (str (json/encode q) "\n"))))

;;
(defn file-read-lines [file]
  "read file content, returns list of lines. each line is complete json record."
  (with-open [rdr (io/reader file)]
    (reduce conj [] (line-seq rdr))))

(defn file-read-questions [filename]
  (map #(json/decode % true) (file-read-lines filename)))

(defn question-read [filename]
  (json/decode (slurp filename) true))



;; {:tagged "clojure", :site "stackoverflow", :page 1, :pagesize 100, :order "desc", :sort "creation", :fromdate 1405122032}
;; :filter "withbody"
;; (def resp (http/get "https://api.stackexchange.com/2.2/questions/39750509/answers" {:query-params {:site "stackoverflow" :filter "withbody"}}))

;;
(defn questions-query-params [from-date page-num]
  "d date string YYYY-MM-DD
   p page number."
  (into site-params {:page page-num :pagesize 100
                     ;; :order "desc"
                     :order "asc"
                     :sort "creation"
                     :fromdate (date-to-epoch from-date)
                     :filter "withbody"}))

(defn get-se [url query-params]
  ;;  (log/debug "url:" url " params:" (into {:query-params query-params} extra-params))
  (http/get url (into {:query-params query-params} @extra-params))
  )

(defn get-questions-page [from-date page]
  (let [params (questions-query-params from-date page)]
    (get-se (str stackexchange-base-url "questions") params)
  ))

(defn get-qlist [from-date]
  (let [rsp  (get-questions-page from-date 1)
        rsp-content (json/decode (:body rsp) true)]
    (:items rsp-content) )
  )

;;
(defn get-answers [queid]
  (let [params (into site-params {:filter "withbody"})
        rsp    (get-se (str stackexchange-base-url "questions/" queid "/answers") params)
        ]
    (:items (json/decode (:body rsp) true))
    ))

(defn get-qcomments [queid]
  (let [params (into site-params {:filter "withbody"})
        rsp (get-se (str stackexchange-base-url "questions/" queid "/comments") params)
        ]
    (:items (json/decode (:body rsp) true))
    ))

(defn get-acomments [ansid]
  (let [params (into site-params {:filter "withbody"})
        rsp (get-se (str stackexchange-base-url "answers/" ansid "/comments") params)
        ]
    (:items (json/decode (:body rsp) true))
    ))

;;
;; 
;; (def acs (map #(get-acomments (:answer_id %1)) ans))
;; (def nans (map answer-add-comments ans))

(defn answer-add-comments [a]
  (assoc a :comments (get-acomments (:answer_id a))))

(defn question-add-comments [q]
  (assoc q :comments (get-qcomments (:question_id q))))

(defn question-fill [q]
  (let [ans (get-answers (:question_id q))
        na  (map answer-add-comments ans)]
    (assoc q :answers na :comments (get-qcomments (:question_id q)))
    ))

(defn question-proc [q]
  (println "processing " (:question_id q))
  (-> q question-fill write-question)
  (write-file (str "data/" topic-tag ".ts") (epoch-to-string (:creation_date q)))
)


;; 
;; (def d0 (get-questions-page "2009-10-20" 1))
;; (let [s (:status1 d0)] (if (= s 200) true false))
;; (-> d0 :body (json/decode true) :items)
;; (-> d0 :body (json/decode true) :has_more)
;; (when (and (d0 :status) (= (d0 :status) 200)) (println "aaa"))
;; (def qs1 (filter #(not= 0 (:answer_count %1)) qs))



;;
;;(def exc (try (get-questions-page ts 1) (catch Exception e e)))

(defn get-qdata []
  (let [ts   (slurp (str "data/" topic-tag ".ts"))
        d0   (get-questions-page ts 1)
        qs   (-> d0 :body (json/decode true) :items)
        qs1  (filter #(not= 0 (:answer_count %1)) qs) ]
    (map question-proc qs1))
  )

;;
(defn get-questions [from-date]
  (doseq [page (range 1 2)]
    (let [rsp  (get-questions-page from-date page)
          rsp-content (json/decode (:body rsp) true)
          items (:items rsp-content)]
      (println "=====================================  ## " page)
      (println "page#" page (count items) (:has_more rsp-content) (:quota_remaining rsp-content))

      ))
  )

;;
(defn set-add-vec [s v] (apply conj s v))
;; (def all-tags (reduce set-add-vec #{} (map :tags qs)))

(defn inc-count [m k] (update m k (fnil inc 0)))
(defn inc-cv [m v] (reduce inc-count m v))
;; (def counts (reduce inc-cv {} (map :tags qs)))
;; (sort-by val > counts)



;;; command-line funcs


(defn parse-proxy-param [s]
  (let [[host port] (clojure.string/split s #":")]
    {:proxy-host host :proxy-port (Integer/parseInt port)}))

(def cli-opts
  [["-x" "--proxy proxy" "http proxy"]
   ["-t" "--tag"         "SO tag"]
   ["-h" "--help"]])

(defn proc-tag
  "proc-tag. "
  [& args]

  (let [{:keys [options arguments errors]} (parse-opts args cli-opts)]
    (log/debug "options" options)
    (log/debug "arguments" arguments)
    ;; (log/debug "errors" errors)

    (when (:proxy options)
      (reset! extra-params (parse-proxy-param (:proxy options)))
      (log/debug "extra-params" @extra-params))

    (while true (doall (get-qdata)))    
    )
  )


;;;
(defn -main
  "clj-so core main"
  [& args]

  (log/debug "starting -main")

  (while true (doall (get-qdata)))
  ;; (try (get-qdata) (catch Exception e (println e)))
  )

