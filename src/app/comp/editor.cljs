
(ns app.comp.editor
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button a]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-prompt comp-confirm comp-select]]
            [app.util :refer [path-with-children]]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [app.comp.presets :refer [comp-presets]]
            [app.comp.type-picker :refer [comp-type-picker]]
            [app.comp.bg-picker :refer [comp-bg-picker]]
            [app.comp.dict-editor :refer [comp-dict-editor]]
            [app.style :as style]))

(def node-layouts
  [{:value :row, :display "Row"}
   {:value :row-middle, :display "Row Middle"}
   {:value :row-parted, :display "Row Parted"}
   {:value :row-center, :display "Row Center"}
   {:value :column, :display "Column"}
   {:value :column-parted, :display "Column Parted"}
   {:value :center, :display "Center"}])

(defcomp
 comp-layout-picker
 (states template-id path markup)
 (div
  {:style ui/row-middle}
  (<> "Layout:" style/field-label)
  (=< 8 nil)
  (cursor->
   :picker
   comp-select
   states
   (:layout markup)
   node-layouts
   {}
   (fn [result d! m!]
     (d! :template/node-layout {:template-id template-id, :path path, :layout result})))))

(def style-element
  {:display :inline-block,
   :cursor :pointer,
   :padding "0 8px",
   :margin-bottom 8,
   :background-color (hsl 0 0 88),
   :color :white,
   :border-radius "4px",
   :vertical-align :top,
   :line-height "24px"})

(defcomp
 comp-markup
 (markup path focused-path)
 (div
  {:class-name "no-shadows",
   :style (merge
           {:padding-left 8}
           (if (empty? (:children markup)) {:border-left "1px solid #eee"}))}
  (div
   {:style (merge
            style-element
            (if (= path focused-path) {:background-color (hsl 200 80 70)})),
    :on-click (fn [e d! m!] (d! :router/set-focused-path path))}
   (<> (:type markup)))
  (list->
   {:style (merge
            {:padding-left 8, :margin-left 8}
            (let [amount (count (:children markup))]
              (if (or (<= amount 1)
                      (and (<= amount 5 )
                           (every? (fn [x] (empty? (:children markup))) (:children markup))))
                {:display :inline-block})))}
   (->> (:children markup)
        (sort-by (fn [[k child-markup]] k))
        (map-val
         (fn [child-markup]
           (comp-markup child-markup (conj path (:id child-markup)) focused-path)))))))

(defcomp
 comp-operations
 (states template-id focused-path)
 (div
  {:style (merge ui/row {:width 300})}
  (div {} (<> "Operations:" style/field-label))
  (div
   {:style {}}
   (a
    {:style ui/link,
     :inner-text "Append",
     :on-click (fn [e d! m!]
       (d! :template/append-markup {:template-id template-id, :path focused-path})
       (d! :router/move-append nil))})
   (=< 8 nil)
   (a
    {:style ui/link,
     :inner-text "After",
     :on-click (fn [e d! m!]
       (d! :template/after-markup {:template-id template-id, :path focused-path})
       (d! :router/move-after nil))})
   (=< 8 nil)
   (a
    {:style ui/link,
     :inner-text "Prepend",
     :on-click (fn [e d! m!]
       (d! :template/prepend-markup {:template-id template-id, :path focused-path})
       (d! :router/move-prepend nil))})
   (a
    {:style ui/link,
     :inner-text "Before",
     :on-click (fn [e d! m!]
       (d! :template/before-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (=< 8 nil)
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (a {:style ui/link, :inner-text "Remove"})}
    (fn [e d! m!]
      (d! :template/remove-markup {:template-id template-id, :path focused-path})
      (d! :router/set-focused-path (vec (butlast focused-path))))))))

(defcomp
 comp-editor
 (states template focused-path)
 (div
  {:style (merge ui/flex ui/row {:overflow :auto})}
  (div
   {:style (merge ui/flex ui/column {:overflow :auto})}
   (cursor-> :operations comp-operations states (:id template) (or focused-path []))
   (div {:style {:height 1, :background-color (hsl 0 0 90)}})
   (div
    {:style (merge ui/flex {:overflow :auto, :padding 8})}
    (comp-markup (:markup template) [] focused-path)
    (when config/dev? (comp-inspect "Markup" (:markup template) {:bottom 0}))))
  (let [child (get-in (:markup template) (interleave (repeat :children) focused-path))
        template-id (:id template)]
    (div
     {:style (merge ui/flex {:overflow :auto, :padding 8})}
     (cursor-> :type comp-type-picker states template-id focused-path child)
     (cursor-> :layout comp-layout-picker states template-id focused-path child)
     (cursor-> :background comp-bg-picker states template-id focused-path child)
     (when config/dev? (comp-inspect "Node" child {:bottom 0}))
     (cursor-> :presets comp-presets states (:presets child) template-id focused-path)
     (cursor->
      :props
      comp-dict-editor
      states
      "Props:"
      (:props child)
      (fn [change d! m!]
        (d!
         :template/node-props
         (merge {:template-id template-id, :path focused-path} change))))
     (cursor->
      :attrs
      comp-dict-editor
      states
      "Attrs:"
      (:attrs child)
      (fn [change d! m!]
        (d!
         :template/node-attrs
         (merge {:template-id template-id, :path focused-path} change))))
     (cursor->
      :style
      comp-dict-editor
      states
      "Style:"
      (:style child)
      (fn [change d! m!]
        (d!
         :template/node-style
         (merge {:template-id template-id, :path focused-path} change))))))))
