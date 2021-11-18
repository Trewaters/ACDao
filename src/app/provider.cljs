(ns app.provider
  (:require
    [rx.core :as rx]
    [rx.operators.core :as op]
    [redux.verticals :as verts :refer [create-action]]
    [redux.observable :as ro]
    [tequito.core :as tq :refer [create-beacon-wallet]]
    [app.redux :as app]))


(defonce ^:private wallet-ref (atom nil))

(defn ^:private reset-wallet!
  "clears wallet"
  [wallet]
  (let [old-wallet @wallet-ref]
    (when old-wallet (.disconnect old-wallet))
    (when wallet (reset! wallet-ref wallet))
    wallet))

(def ^:private chains
  {:MAINNET "NetXdQprcVkpaWU",
   :CARTHAGENET "NetXjD3HPJJjmcd",
   :DELPHINET "NetXm8tYqnMWky1",
   :EDONET "NetXSgo1ZT2DRUG",
   :FLORENCENET "NetXxkAx4woPLyu",
   :GRANADANET "NetXz969SFaFn8k",
   :HANGZHOUNET "NetXZSsxBpMQeAT"})

(def chain-to-id-map
  (->>
    chains
    (seq)
    (map (fn [[k v]] [(keyword k) v]))
    (into (sorted-map))))

; actions

(def connect-wallet (create-action ::connect-wallet))
(def connect-wallet-complete (create-action ::connect-wallet-complete))
(def connect-wallet-error (create-action ::connect-wallet-error))
(def get-address-complete (create-action ::get-address-complete))
(def get-address-error (create-action ::get-address-error))

(def default-state
  {:chain-id nil
   :address ""})

; selectors
(def rinkeby?-selector #(get-in % [::state :rinkeby?]))
(def chain-id-selector #(get-in % [::state :chain-id]))
(def chain-name-selector (comp #(get chains % "NA") chain-id-selector))
(def address-selector #(get-in % [::state :address]))
(def block-num-selector #(get-in % [::state :block-num]))


(def reducer-slicer
  {::state
   (verts/handle-actions
     {::connect-wallet-complete
      (verts/map-from-action
        #(get % :payload)
        (partial assoc :address))}

     default-state)})

(defn init-client-epic [actions _ {:keys [client]}]
  (->
    actions
    ((rx/pipe
       (op/filter (verts/is-type? (get app/types :on-mount)))
       (op/map (comp reset-wallet! create-beacon-wallet (constantly nil)))
       (op/tap #(tq/set-wallet-provider client %))
       (op/switch-map
         (fn [wallet]
           (->
             (rx/defer (partial tq/get-active-account wallet))
             ((rx/pipe
               (op/filter (comp not nil?))
               (op/map (comp get-address-complete :address))
               (op/catch-error (comp rx/of get-address-error)))))))))))

(def connect-wallet-epic
  (rx/pipe
    (op/filter (verts/is-type? ::connect-wallet))
    (op/map #(deref wallet-ref))
    (op/switch-map
      (fn [wallet]
        ((rx/pipe
           (op/map connect-wallet-complete)
           (op/catch-error (comp rx/of connect-wallet-error)))
         (rx/defer (partial tq/request-permission wallet)))))))

(def root-epic (ro/combine-epics init-client-epic connect-wallet-epic))
