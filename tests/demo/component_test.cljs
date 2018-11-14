(ns demo.component-test
  (:require [demo.component :as component]
            [reagent.core :as r]
            [renderer]))


;; Snapshot testing
;; -----------------------------------------------------------------------------
;; https://jestjs.io/docs/en/snapshot-testing

(js/it
  "Render correctly"
  (fn []
    (let [button [component/button
                  {:class "test-class"
                   :type  "button"}]
            tree   (.. renderer (create (r/as-element button)) (toJSON))]
        (.. (js/expect tree) (toMatchSnapshot)))))
