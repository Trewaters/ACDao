(ns rx.operators.core
  (:refer-clojure
    :exclude
    [true? map filter reduce merge repeat first
     last mapcat repeatedly zip dedupe drop
     take take-while map-indexed concat empty
     delay range throw do trampoline subs flatten])
  (:require
    ["rxjs/operators" :as op]))

(defn filter [predicate]
  (.filter op #(boolean (predicate %))))

(defn map [project]
  (.map op project))

(defn switchMap [project]
  (.map op project))

(defn catchError [selector]
  (.catchError op selector))

(def ^:private -tap (.-tap op))

(defn tap
  "Used to perform side-effects for notifications from the source observable"
  ([] (-tap))
  ([observerOrNext] (-tap observerOrNext))
  ([next error] (-tap next error))
  ([next error complete] (-tap next error complete)))
