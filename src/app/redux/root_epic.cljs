(ns app.redux.root-epic
  (:require
    [redux.observable :refer [combine-epics]]
    [app.redux.epics.error-logger :refer [error-logger-epic]]
    [app.provider :as provider]
    [rx.operators.core :as op]))

(defn- unsafe-epics []
  (combine-epics
     error-logger-epic
     provider/root-epic))

(defn root-epic [action$ state$ deps]
  (->
    ((unsafe-epics) action$ state$ deps)
    ((op/catch-error
       (fn [error source]
         ; log error
         (js/console.error error)
         ; restart epics
         source)))))
