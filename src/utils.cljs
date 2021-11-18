(ns utils
  (:require
    [clojure.string :as string]
    [camel-snake-kebab.core :as csc :include-macros true]
    [camel-snake-kebab.extras :refer [transform-keys]]))


(defn safe-case [case-f]
 (fn [x]
   (cond-> (subs (name x) 1)
     true (string/replace "_" "*")
     true case-f
     true (string/replace "*" "_")
     true (->> (str (first (name x))))
     (keyword? x) keyword)))

(def camel-case (safe-case csc/->camelCase))
(def kebab-case (safe-case csc/->kebab-case))

(def js->cljk #(js->clj % :keywordize-keys true))

(def js->cljkk
  "#js {'fooBar' 'baz'} -> {:foo-bar 'baz'}"
  (comp (partial transform-keys kebab-case) js->cljk))

(comment
  (js->cljkk #js {"fooBar" "baz"}))

(def cljkk->js
  "{:foo-bar 'baz'} -> #js {'fooBar' 'baz'}"
  (comp clj->js (partial transform-keys camel-case)))

(comment
  (cljkk->js {:foo-bar "baz"}))
