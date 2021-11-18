(ns redux.observable
  (:require
    ["redux-observable" :as ro]
    [rx.operators.core :as op]
    [redux.core :refer [wrap-js-middleware]]
    [redux.verticals :as verts]))


(defn create-epic-middleware [deps]
  (let [epic-middleware (ro/createEpicMiddleware #js {:dependencies deps})]
    [(wrap-js-middleware epic-middleware) (fn run [root-epic] (.run epic-middleware root-epic))]))

(defn is-type?
  "creates an rxjs operator that filters for specific types"
  ([action-type] (op/filter (verts/is-type? action-type)))
  ([action-type & types]
   (op/filter
     (fn [action]
       (apply some #(verts/is-type? % action) action-type types)))))

(defn combine-epics
  "combine epics to create one root epic"
  [& args]
  (apply js-invoke ro "combineEpics" args))
