(ns demo.core_test
  (:require [demo.core :as component]))


(js/test
  "Adds 1 + 2 to equal 3"
  (.. (js/expect (component/sum 1 2)) (js/toBe 3)))
