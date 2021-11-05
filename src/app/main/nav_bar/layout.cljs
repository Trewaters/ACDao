(ns app.main.nav-bar.layout
  (:require [clojure.string :as str]
            [helix.core :refer [$]]
            [helix.dom :as d]
            ; ["reselect" :as reselect]
            [redux.core :refer [use-action]]
            [app.utils.core :refer [class-names]]
            [app.ui.button :refer [button]]
            [app.ui.pill :refer [pill]]
            [app.ui.loading :refer [loading-pulse]]
            [app.main.nav-bar.redux :as redux]))




(comment
  (= (as-> "0x1234000000000000000000000000000000056789" %
       (str/split % #"")
       (concat (take 5 %) "..." (take-last 4 %))
       (str/join %))
     "0x12...6789"))

(defn main []
  (let [show-no-provider true
        show-loading false
        chain-name "Tezos"
        show-wrong-network false
        address "1234"
        block-num 4
        click-on-connect (use-action redux/click-on-connect)]

    (d/div
      {:class-name
       (class-names
         :flex
         :flex-row
         :items-center
         :justify-center
         :mt-4
         :mb-12
         :px-2
         :w-full)}

      (d/div
        {:className
         (class-names
           :bg-gray-700
           :flex
           :flex-row
           :justify-end
           :space-x-2
           :h-16
           :px-4
           :items-center
           :rounded-xl
           :shadow-lg
           :text-white
           :w-full)}

        (and
          (> block-num 0)
          ($ pill (str "Block: " (.toLocaleString block-num))))

        (and
          (not (str/blank? address))
          ($ pill address))

        (and
          chain-name
          ($ pill (str "network: " chain-name)))

        (cond
          show-loading ($ loading-pulse)
          show-no-provider ($ pill
                              {:class-name "mx-1"}
                              "no metamask provider detected")
          show-wrong-network ($ pill
                                "warning! wrong network.")

          (not (str/blank? address)) nil

          :else
          ($ button
             {:on-click click-on-connect}
             "connect"))))))
