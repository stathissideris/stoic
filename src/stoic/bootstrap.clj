(ns stoic.bootstrap
  "Bootstrap a component system with components and settings."
  (:require [com.stuartsierra.component :as component]
            [stoic.components.foo]
            [stoic.protocols.config-supplier :as cs]
            [stoic.config.zk]))

(def registry [[:foo stoic.components.foo/->Foo]])

(defn- choose-supplier []
  (stoic.config.zk/zk-config-supplier))

(defn- create-components
  "Creates components passing a settings atom into each constructor."
  [registry component-settings]
  (into {}
        (for [[k c-c] registry :let [settings (get component-settings k)]]
          [k (c-c settings)])))

(defn- fetch-settings
  "Fetch settings from the config supplier and wrap in atoms."
  [config-supplier registry]
  (into {} (for [[k] registry]
             [k (atom (cs/fetch config-supplier k))])))

(defn- bounce-component! [config-supplier k c settings-atom]
  (component/stop c)
  (let [settings (cs/fetch config-supplier k)]
    (reset! settings-atom settings))
  (component/start c))

(defn- bounce-components-if-config-changes!
  "Add watchers to config to bounce relevant component if config changes."
  [config-supplier components component-settings]
  (doseq [[k c] components
          :let [settings-atom (get component-settings k)]]
    (cs/watch! config-supplier k
               (partial bounce-component! config-supplier k c settings-atom))))

(defn bootstrap
  "Bootstrap a component system from the supplied component registry."
  [registry]
  (let [config-supplier (choose-supplier)
        component-settings (fetch-settings config-supplier registry)
        components (create-components registry component-settings)]
    (bounce-components-if-config-changes! config-supplier components component-settings)
    (component/start (apply component/system-map (reduce into [] components)))))
