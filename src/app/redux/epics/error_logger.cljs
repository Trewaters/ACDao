(ns app.redux.epics.error-logger
  (:require
    [rx.core :as rx]
    [rx.operators.core :as op]))

(def error-logger-epic
  (rx/pipe
    (op/filter
      #(->
         %
         (:error)
         (boolean)))
    (op/tap
      (fn [{:keys [type payload]}]
        (js/console.info (str "Error action of type" type))
        (js/console.error payload)))
    (op/ignore-elements)))
