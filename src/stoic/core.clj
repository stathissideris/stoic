(ns stoic.core
  (:require [environ.core :as environ]
            [zookeeper :as zk]
            [zookeeper.data :as zk-data]))

(defn zk-ips
  "Zookeeper IPs."
  []
  (or (environ/env :am-zk-hosts) "localhost:2181"))

(defn zk-root
  "Zookeeper Root."
  [] (keyword (or (environ/env :am-zk-root) :default)))

(defn connect []
  (zk/connect (zk-ips)
              :timeout-msec 10000))

(defn serialize-form
  "Serializes a Clojure form to a byte-array."
  ([form]
     (zk-data/to-bytes (pr-str form))))

(defn deserialize-form
  "Deserializes a byte-array to a Clojure form."
  ([form]
     (when form (read-string (zk-data/to-string form)))))

(defn add-to-zk [client path m]
  (when-not (zk/exists client path)
    (zk/create-all client path :persistent? true))
  (let [v (:version (zk/exists client path))]
    (zk/set-data client path (serialize-form m) v)))

(defn read-from-zk [client path]
  (deserialize-form (:data (zk/data client path))))
