(ns tequito.core
  (:require
    [cljs.core :refer [clj->js]]
    [utils]
    ["@taquito/taquito" :as tz :refer [TezosToolkit]]
    ["@taquito/beacon-wallet" :refer [BeaconWallet]]
    ["@airgap/beacon-sdk" :refer [NetworkType BeaconEvent defaultEventCallbacks]]))

(def rpc-url "https://florencenet.smartpy.io")

(defn create-client []
  (let [client (new TezosToolkit rpc-url)]
    client))

(defn set-wallet-provider
  "set a new wallet provider on a client"
  [client wallet]
  (.setWalletProvider client wallet))

(comment (create-client))

(defn create-beacon-wallet
  "creates an instance of the beacon wallet"
  ([] (create-beacon-wallet {}))
  ([{:keys [pair-success]}]
   (new
     BeaconWallet
     (clj->js
       {:name "ACDao"
        :appUrl "https://example.com"
        :preferedNetwork (.-FLORENCENET NetworkType)
        :disableDefaultEvents true
        :eventHandlers
        {(.-PAIR_INIT BeaconEvent) (.-PAIR_INIT defaultEventCallbacks)
         (.-PAIR_SUCCESS BeaconEvent) (or pair-success identity)}}))))

(defn get-active-account
  "checks if wallet was already active. Returns a promise"
  [^js/BeaconWallet wallet]
  (->
    wallet
    (.-client)
    (.getActiveAccount)
    (.then utils/js->cljkk)))

(defn get-pkh
  "get public key hash of wallet. Returns a promise"
  [^js/BeaconWallet wallet]
  (.getPKH wallet))

(defn request-permission
  "request permission to connect to wallet"
  [wallet]
  (->
    wallet
    (.requestPermission (clj->js {:network {:type (.-FLORENCENET NetworkType) :rpcUrl rpc-url}}))))
