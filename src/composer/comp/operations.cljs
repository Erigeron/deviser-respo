
(ns composer.comp.operations
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> >> <> span div button a pre]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.core :refer [comp-prompt comp-confirm comp-select]]
            [composer.util :refer [path-with-children]]
            [composer.style :as style]
            [bisection-key.core :as bisection]
            ["copy-text-to-clipboard" :as copy!]
            [favored-edn.core :refer [write-edn]]
            [cljs.reader :refer [read-string]]
            [clojure.string :as string]))

(defcomp
 comp-operations
 (states template focused-path)
 (let [template-id (:id template)]
   (div
    {:style (merge ui/row {:padding "0 8px"})}
    (div {} (<> "Operations:" style/field-label))
    (div
     {:style (merge ui/flex)}
     (a
      {:style style/link,
       :inner-text "Append",
       :on-click (fn [e d!]
         (d! :template/append-markup {:template-id template-id, :path focused-path})
         (d! :router/move-append nil))})
     (a
      {:style style/link,
       :inner-text "After",
       :on-click (fn [e d!]
         (d! :template/after-markup {:template-id template-id, :path focused-path})
         (d! :router/move-after nil))})
     (a
      {:style style/link,
       :inner-text "Prepend",
       :on-click (fn [e d!]
         (d! :template/prepend-markup {:template-id template-id, :path focused-path})
         (d! :router/move-prepend nil))})
     (a
      {:style style/link,
       :inner-text "Before",
       :on-click (fn [e d!]
         (d! :template/before-markup {:template-id template-id, :path focused-path})
         (d! :router/move-before nil))})
     (comp-confirm
      (>> states :remove)
      {:trigger (a {:style ui/link, :inner-text "Remove"})}
      (fn [e d!]
        (d! :template/remove-markup {:template-id template-id, :path focused-path})
        (d! :session/focus-to {:path (vec (butlast focused-path))})))
     (a
      {:style style/link,
       :inner-text "Wrap",
       :on-click (fn [e d!]
         (d! :template/wrap-markup {:template-id template-id, :path focused-path})
         (d! :session/focus-to {:path focused-path}))})
     (a
      {:style style/link,
       :inner-text "Spread",
       :on-click (fn [e d!]
         (d! :template/spread-markup {:template-id template-id, :path focused-path})
         (d! :router/move-before nil))})
     (a
      {:style style/link,
       :inner-text "Copy",
       :on-click (fn [e d!]
         (let [branch (get-in (:markup template) (interleave (repeat :children) focused-path))]
           (copy! (write-edn branch)))
         (d! :session/copy-markup {:template-id template-id, :path focused-path}))})
     (a
      {:style style/link,
       :inner-text "Paste",
       :on-click (fn [e d!]
         (d! :session/paste-markup {:template-id template-id, :path focused-path}))})
     (comp-prompt
      (>> states :replace)
      {:trigger (a {:style style/link, :inner-text "Replace"}),
       :text "Replace markup",
       :multiline? true,
       :input-style {:font-family ui/font-code, :font-size 12, :min-height 200},
       :validator (fn [content]
         (if (string/blank? content)
           "Can't be empty"
           (try (do (read-string content) nil) (catch js/Error e (str e)))))}
      (fn [result d!]
        (let [data (read-string result)]
          (if (map? data)
            (d! :template/replace {:path focused-path, :template-id template-id, :data data})
            (js/console.error "Invalid data")))))))))
