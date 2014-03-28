(ns stoic.config.zk
  "Namespace to faciliate Stoic interaction with Zookeeper."
  (:require [environ.core :as environ]
            [zookeeper :as zk]
            [zookeeper.data :as zk-data]
            [stoic.protocols.config-supplier]))

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

(defn close [client]
  (zk/close client))

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

(defn path-for [root k]
  (format "/stoic/%s/components/%s" (name root) (name k)))

(defrecord ZkConfigSupplier [client root]
  stoic.protocols.config-supplier/ConfigSupplier

  (fetch [this k]
    (let [path (path-for root k)]
      (when-not (zk/exists client path)
        (zk/create-all client path :persistent? true))
      (read-from-zk client path)))

  (watch! [this k watcher-fn]
    (let [path (path-for root k)]
      (zk/exists client path :watcher
                 (fn the-watcher [event]
                   (when (= :NodeDataChanged (:event-type event))
                     (watcher-fn)
                     (zk/exists client path :watcher the-watcher)))))))

(defn zk-config-supplier []
  (ZkConfigSupplier. (connect) (zk-root)))
