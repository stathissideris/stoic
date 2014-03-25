(ns stoic.protocols.components)

(defprotocol StoicComponent
  (registry-key [component]))
