(ns stoic.config.zk-test
  (:require [stoic.config.zk :refer :all]
            [clojure.test :refer :all]
            [stoic.protocols.config-supplier :as cs]))

(deftest can-write-and-read-from-zookeeper
  (let [expected {:a :b}
        zk (zk-config-supplier)]
    (add-to-zk (connect) (path-for :default :foo) expected)
    (is (= {:a :b} (cs/fetch zk :foo)))))
