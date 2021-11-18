(ns redux.observable
  (:require
    ["redux-observable" :as ro]
    [redux.core :refer [wrap-js-middleware]]))

(defn create-epic-middleware [deps]
  (let [epic-middleware (ro/createEpicMiddleware #js {:dependencies deps})]
    [(wrap-js-middleware epic-middleware) (fn run [root-epic] (.run epic-middleware root-epic))]))
