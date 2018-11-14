(ns demo.component
  (:require [reagent.core :as r]))


(defn button
  [{:keys [class type]} & children]
  [:button
    {:class class
     :type  type}
    "hello"])
