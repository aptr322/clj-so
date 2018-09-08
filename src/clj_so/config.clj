(ns clj-so.config
  (:require [cprop.core :refer [load-config]]
            [clojure.tools.logging :as log])
  (:gen-class))


(def app-config (load-config))

(log/info app-config)

