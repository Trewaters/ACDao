(ns app.core
  (:require
    [helix.core :refer [$]]
    ["react-dom" :refer [render]]
    [redux.core :refer [create-store apply-middlewares]]
    [redux.helix :refer [react-redux-context]]
    [redux.verticals :as verts]
    [redux.dev-tool-ext :refer [dev-tools-enhancer]]
    [redux.array-action-middleware :as array-action]
    [redux.observable :refer [create-epic-middleware]]
    [tequito.core :as tq]
    [app.layout :as layout]
    [app.main.redux :as main-redux]
    [app.provider :as provider]))

(defonce ^:private get-state (atom (constantly nil)))

(defn ^:dev/after-load create-app
  ([] (create-app nil))
  ([default-state]
   (let [default-state (or (@get-state) default-state)
         tq-client (tq/create-client)
         root-epic provider/root-epic
         [epic-middleware run-epic] (create-epic-middleware {:client tq-client})
         store (create-store
                 (verts/combine-reducers
                   (merge
                     main-redux/reducer-slice))
                 default-state
                 (comp
                   (apply-middlewares
                     array-action/middleware
                     epic-middleware)
                   (dev-tools-enhancer)))]

     (reset! get-state (:get-state store))
     (run-epic root-epic)

     (render
       ($ (.-Provider react-redux-context)
          {:value store
           ;; key to force re-renders on hot reload
           :key (rand)}

          ($ layout/App))
       (js/document.getElementById "app")))))

(defn ^:export main [] (create-app))
