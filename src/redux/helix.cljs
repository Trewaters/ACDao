(ns redux.helix
  (:require [helix.core :refer [create-context]]
            [helix.hooks :as h]
            ["invariant" :as invariant]))

(def react-redux-context (create-context))

(set! (.-displayName react-redux-context) "react-redux-context")

(defn use-redux-context []
  (h/use-context react-redux-context))

(defn use-store []
  (use-redux-context))

(defn use-selector [selector]
  (invariant (fn? selector)
             (str "use-selector expects a function but found " selector))
  (h/use-debug-value selector)

  (let [[_ force-rerender] (h/use-reducer inc 0)
        store (use-store)

        state-ref (h/use-ref nil)
        selector-ref (h/use-ref nil)
        selected-state-ref (h/use-ref nil)

        subscriber (fn []
                     (let [state ((:get-state store))
                           selected-state (@selector-ref state)]

                       (when (not (= selected-state @selected-state-ref))
                         (reset! state-ref state)
                         (reset! selected-state-ref selected-state)
                         (force-rerender))))

        new-state ((:get-state store))
        new-selected-state (selector new-state)

        selected-state (if
                         (and
                           ;; if global state or selector fn have changed
                           (or (not (= selector @selector-ref))
                               (not (= new-state @state-ref)))
                           ;; and
                           (or
                             ;; prev is nil (initial run)
                             (not @selected-state-ref)
                             ;; or selected state has changed
                             (not (= @selected-state-ref new-selected-state))))
                         ;; update local selected-state
                         new-selected-state
                         ;; else use prev selected state
                         @selected-state-ref)]

    (h/use-layout-effect
      :once
      (reset! selector-ref selector)
      (reset! state-ref new-state)
      (reset! selected-state-ref selected-state))

    ;; updates selector state on store updates
    ;; triggers comp update using force-rerender
    (h/use-layout-effect
      [store]
      ((:subscribe store) subscriber))

    selected-state))

(defn use-dispatch []
  (:dispatch (use-store)))

(defn use-action [action-creator]
  (invariant
    (fn? action-creator)
    (str "use-action expected a function but received " action-creator))
  (let [dispatch (use-dispatch)]
    (comp dispatch action-creator)))
