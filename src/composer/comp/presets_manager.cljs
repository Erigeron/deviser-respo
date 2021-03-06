
(ns composer.comp.presets-manager
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> >> <> span div button input a pre code]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo.util.list :refer [map-val]]
            [composer.core :refer [render-markup]]
            [respo-alerts.core :refer [comp-prompt comp-confirm]]
            [clojure.string :as string]
            [feather.core :refer [comp-icon comp-i]]
            [favored-edn.core :refer [write-edn]]
            [cljs.reader :refer [read-string]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defcomp
 comp-presets-manager
 (states presets)
 (let [cursor (:cursor states), state (or (:data states) {:pointer nil})]
   (div
    {:style (merge ui/expand ui/column {:padding 16})}
    (div
     {:style {:font-family ui/font-fancy, :font-size 20, :color (hsl 0 0 70)}}
     (<> "Presets")
     (=< 8 nil)
     (comp-prompt
      (>> states :create)
      {:trigger (comp-i :plus 14 (hsl 200 100 80)),
       :style {:display :inline-block},
       :text "Name for a preset:"}
      (fn [result d!] (d! :settings/create-preset result))))
    (div
     {:style (merge ui/flex ui/row), :class-name ""}
     (list->
      {:style (merge
               ui/column
               {:min-width 120, :border-right (str "1px solid " (hsl 0 0 90))})}
      (->> presets
           (map-val
            (fn [preset]
              (div
               {:style (merge
                        {:cursor :pointer, :padding "0 8px"}
                        (if (= (:id preset) (:pointer state))
                          {:background-color (hsl 200 80 70), :color :white})),
                :on-click (fn [e d!] (d! cursor (assoc state :pointer (:id preset))))}
               (<> (:name preset)))))))
     (=< 16 nil)
     (if (some? (:pointer state))
       (let [preset (get presets (:pointer state))]
         (div
          {:style (merge ui/flex ui/column)}
          (div
           {:style ui/row-parted}
           (div
            {:style ui/row-middle}
            (<> (:name preset))
            (=< 8 nil)
            (comp-prompt
             (>> states :rename)
             {:trigger (comp-i :edit-2 14 (hsl 200 80 70)),
              :initial (:name preset),
              :text "New name for preset:"}
             (fn [result d!] (d! :settings/rename-preset {:id (:id preset), :name result}))))
           (comp-confirm
            (>> states :remove)
            {:trigger (comp-i :x 14 (hsl 0 80 70)),
             :text (<< "This ~(:name preset) will be erased!")}
            (fn [e d!]
              (d! cursor (assoc state :pointer nil))
              (d! :settings/remove-preset (:id preset)))))
          (=< nil 16)
          (list->
           {:style {:font-family ui/font-code}}
           (->> (:style preset)
                (map
                 (fn [[k v]]
                   [k
                    (div
                     {:style (merge ui/row {:border-bottom (str "1px solid " (hsl 0 0 94))})}
                     (<> k {:display :inline-block, :min-width 160})
                     (<> v))]))))
          (=< nil 16)
          (div
           {}
           (comp-prompt
            (>> states :update)
            {:trigger (button {:inner-text "Edit", :style ui/button}),
             :multiline? true,
             :input-style {:font-family ui/font-code},
             :initial (write-edn (:style preset)),
             :text "Style definition in EDN:",
             :validator (fn [x] (try (do (read-string x) nil) (catch js/Error e (str e))))}
            (fn [result d!]
              (d! :settings/update-preset {:id (:id preset), :style (read-string result)}))))))
       (<> "No selection" {:font-family ui/font-fancy, :color (hsl 0 0 70)}))))))
