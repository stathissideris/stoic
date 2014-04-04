(ns stoic.bootstrap-test
  (:use [clojure.core.async :only [chan timeout >!! <!! buffer alts!!]])
  (:require [stoic.config.zk :as stoic-zk]
            [clojure.test :refer :all]
            [stoic.protocols.config-supplier :as cs]
            [com.stuartsierra.component :as component]
            [stoic.bootstrap :as b]))

(defrecord TestAsyncComponent [starts stops]
  component/Lifecycle

  (start [{:keys [settings]}]
    (>!! starts (:a @settings)))

  (stop [{:keys [settings]}]
    (>!! stops (:a @settings))))

(defmacro harness [& body]
  `(let [~'client (stoic-zk/connect)]
     (try
       ~@body
       (finally
         (stoic-zk/close ~'client)))))

(deftest can-bounce-component-on-config-change
  (harness
   (stoic-zk/add-to-zk client (stoic-zk/path-for :default :test) {:a :initial-value})

   (let [starts (chan (buffer 1))
         stops (chan (buffer 1))]
     (component/start
      (b/bootstrap
       (component/system-map :test (->TestAsyncComponent starts stops))))

     (is (= :initial-value (first (alts!! [(timeout 2000) starts]))))

     (stoic-zk/add-to-zk client (stoic-zk/path-for :default :test) {:a :b})

     (is (= :initial-value (first (alts!! [(timeout 2000) stops]))))
     (is (= :b (first (alts!! [(timeout 2000) starts]))))

     (stoic-zk/add-to-zk client (stoic-zk/path-for :default :test) {:a :c})

     (is (= :b (first (alts!! [(timeout 2000) stops]))))
     (is (= :c (first (alts!! [(timeout 2000) starts])))))))

(defrecord TestComponent [s]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]))

(defrecord TestDependentComponent [s funk]
  component/Lifecycle
  (start [this]
    this)
  (stop [this]))

(deftest component-with-dependencies-get-injected
  (harness
   (stoic-zk/add-to-zk client (stoic-zk/path-for :default :test1) {:a :test-1-value})
   (stoic-zk/add-to-zk client (stoic-zk/path-for :default :test2) {:a :test-2-value})

   (let [system (component/start
                 (b/bootstrap
                  (component/system-map :test1 (map->TestComponent {:s "sad"})
                                        :test2 (component/using
                                                (map->TestDependentComponent {:s "fo"})
                                                [:test1]))))]

     (is (= :test-1-value (-> system :test2 :test1 :settings deref :a))))))
