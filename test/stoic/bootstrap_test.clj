(ns stoic.bootstrap-test
  (:use [clojure.core.async :only [chan timeout >!! <!! buffer alts!!]])
  (:require [stoic.config.zk :refer :all]
            [clojure.test :refer :all]
            [stoic.protocols.config-supplier :as cs]
            [com.stuartsierra.component :as component]
            [stoic.bootstrap :as b]))

(defrecord TestComponent [starts stops]
  component/Lifecycle

  (start [{:keys [settings]}]
    (>!! starts (:a @settings)))

  (stop [{:keys [settings]}]
    (>!! stops (:a @settings))))

;; TODO test components with dependencies get injectedyo

(deftest can-bounce-component-on-config-change
  (let [starts (chan (buffer 1))
        stops (chan (buffer 1))
        client (connect)]
    (add-to-zk client (path-for :default :test) {:a :initial-value})

    (component/start
     (b/bootstrap
      (component/system-map :foo (->TestComponent starts stops))))

    (is (= :initial-value (first (alts!! [(timeout 2000) starts]))))

    (add-to-zk client (path-for :default :test) {:a :b})

    (is (= :initial-value (first (alts!! [(timeout 2000) stops]))))
    (is (= :b (first (alts!! [(timeout 2000) starts]))))

    (add-to-zk client (path-for :default :test) {:a :c})

    (is (= :b (first (alts!! [(timeout 2000) stops]))))
    (is (= :c (first (alts!! [(timeout 2000) starts]))))))
