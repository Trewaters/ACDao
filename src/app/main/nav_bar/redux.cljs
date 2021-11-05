(ns app.main.nav-bar.redux
  (:require [redux.verticals :as verts]))


(def click-on-connect (verts/compose-actions
                        (verts/create-action ::click-on-connect)))
