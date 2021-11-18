(ns redux.core
  (:require
    ["invariant" :as invariant]
    [utils]))

(defn create-store
  ([reducer preloaded-state]
   (let [state (atom preloaded-state)
         get-state (fn [] @state)

         subscriptions (atom #{})
         subscribe (fn [subscriber]
                     (invariant
                       (fn? subscriber)
                       (str "subscribe expects subscriber to be a function but found " (or subscriber "nil")))
                     (swap! subscriptions conj subscriber)
                     (fn [] (swap! subscriptions disj subscriber)))

         dispatch (fn [action]
                    (println "action: " action (type action))
                    (invariant
                      (and (map? action) (:type action))
                      (str "dispatch expects all actions to be a map with a type set but found " (or action "nil")))

                    (swap! state #(reducer % action))
                    (run! #(%) @subscriptions))]

     ;; dispatch INIT so that each reducer populates it's own initial state
     ; TODO: runs but does not show in devtools
     (dispatch {:type ::INIT})

     {:dispatch dispatch
      :get-state get-state
      :subscribe subscribe}))


  ([reducer preloaded-state enhancer]
   (invariant
     (fn? enhancer)
     (str "create-store expected enhancer to be a function but found " enhancer))

   ((enhancer create-store) reducer preloaded-state)))

(defn apply-middlewares [& middlewares]
  (fn am-enhancer
    [create-store]
    (fn am-create-store
      [reducer preloaded-state]
      (let [store (create-store reducer preloaded-state)
            get-state (:get-state store)
            store-dispatch (:dispatch store)
            dispatch-ref (atom (fn [] (throw (js/Error "Do not dispatch during middleware creation"))))
            dispatch (fn dispatch [action] (@dispatch-ref action))

            api {:get-state get-state
                 :dispatch dispatch}

            chain (->>
                    middlewares
                    (map #(% api))
                    (apply comp))

            chain-dispatch (chain store-dispatch)]

        (reset! dispatch-ref chain-dispatch)
        (assoc store :dispatch chain-dispatch)))))

(defn wrap-js-middleware
  "wrap a third party middleware."
  [middleware]
  (fn [api]
    (let [js-api (utils/cljkk->js api)]
      (middleware js-api))))
