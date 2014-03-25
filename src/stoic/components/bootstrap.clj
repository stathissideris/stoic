(ns stoic.components.bootstrap
  (:require [com.stuartsierra.component :as component]
            [stoic.components.foo]
            [stoic.protocols.config-supplier :as cs]
            [stoic.config.zk]))

;; Phase 1:
;; Fire up a component system map
;; Instantiate components with settings from ZK

;; Phase 2:
;; Bounce a component when change occurs in ZK (with test)
;; If session becomes expired handle reconnect and bounce

;; Misc:
;; Ability to reload


(defn choose-supplier []
  (stoic.config.zk/zk-config-supplier))

(def registry [[:foo stoic.components.foo/->Foo]])

(defn bounce-component! [config-supplier k state c]
  (component/stop c)
  (let [new-state (cs/fetch config-supplier k nil)]
    (reset! state new-state))
  (component/start c))

(defn bootstrap [component-registry]
  (let [config-supplier (choose-supplier)
        components (reduce into [] (for [[k c-c] component-registry
                                         :let [state (atom nil)
                                               c (c-c state)
                                               state-from-config-supplier (cs/fetch config-supplier k (partial bounce-component! config-supplier k state c))]]
                                     (do (reset! state state-from-config-supplier)
                                         [k c])))]
    (component/start (apply component/system-map components))))
