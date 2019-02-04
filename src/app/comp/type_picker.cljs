
(ns app.comp.type-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp cursor-> <> action-> span div]]
            [app.config :as config]
            [respo-alerts.comp.alerts :refer [comp-select]]))

(def node-types
  [{:value :box, :display "Box"}
   {:value :space, :display "Space"}
   {:value :icon, :display "Icon"}
   {:value :text, :display "Text"}
   {:value :template, :display "Template"}
   {:value :input, :display "Input"}
   {:value :button, :display "Button"}
   {:value :link, :display "Link"}
   {:value :if, :display "If expression"}
   {:value :value, :display "Value expression"}])

(defcomp
 comp-type-picker
 (states template-id focused-path markup)
 (div
  {:style ui/row-middle}
  (<> "Node Type:")
  (=< 8 nil)
  (cursor->
   :type
   comp-select
   states
   (:type markup)
   node-types
   {}
   (fn [result d! m!]
     (if (some? result)
       (d! :template/node-type {:template-id template-id, :path focused-path, :type result}))))))