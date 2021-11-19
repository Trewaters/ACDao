(ns redux.middlewares.array-actions)

(defn middleware [{:keys [dispatch]}]
  (fn [next]
    (fn [action]
      (if (vector? action)
        (run! #(dispatch %) action)
        (next action)))))
