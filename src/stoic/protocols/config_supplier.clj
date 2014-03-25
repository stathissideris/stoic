(ns stoic.protocols.config-supplier)

(defprotocol ConfigSupplier
  (fetch [this k watcher-fn]))
