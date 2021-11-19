(ns redux.middlewares.only-truthies)

(defn middleware []
  (fn [next]
    (fn [action]
      (when (not (nil? action))
        (next action)))))
