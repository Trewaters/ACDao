(ns app.core
  (:require
    [helix.core :refer [$]]
    ["react-dom" :refer [render]]
    [redux.core :refer [react-redux-context create-store apply-middlewares]]
    [redux.verticals :as verts]
    [redux.dev-tool-ext :refer [dev-tools-enhancer]]
    [redux.array-action-middleware :as array-action]
    ["redux-observable" :refer [createEpicMiddleware]]
    [tequito.core :as tq]
    [app.layout :as layout]
    [app.main.redux :as main-redux]))


(defonce ^:private get-state (atom (constantly nil)))

(defn ^:dev/after-load create-app
  ([] (create-app nil))
  ([default-state]
   (let [default-state (or (@get-state) default-state)
         tq-client (tq/create-client)
         epicMiddleware (createEpicMiddleware {:dependencies {:client tq-client}})
         store (create-store
                 (verts/combine-reducers
                   (merge
                     main-redux/reducer-slice))
                 default-state
                 (comp
                   (apply-middlewares
                     array-action/middleware
                     epicMiddleware)
                   (dev-tools-enhancer)))]

     (reset! get-state (:get-state store))
     (render ($ (.-Provider react-redux-context)
                {:value store
                 ;; key to force re-renders on hot reload
                 :key (rand)}

                ($ layout/App))
             (js/document.getElementById "app")))))

(defn ^:export main [] (create-app))
