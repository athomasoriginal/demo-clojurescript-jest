(ns demo.core_test
  (:require [demo.core :as component]))

;; Getting Started
;; -----------------------------------------------------------------------------
;; https://jestjs.io/docs/en/getting-started

(js/test
  "Adds 1 + 2 to equal 3"
  #(.. (js/expect (component/sum 1 2)) (toBe 3)))


;; Using Matchers
;; -----------------------------------------------------------------------------
;; https://jestjs.io/docs/en/using-matchers

(js/test
  "two plus two is four"
  #(.. (js/expect (+ 2 2)) (toBe 4)))


(js/test
  "Object Assignment"
  (fn []
    (let [data      {:one 1}
          next-data (assoc data :two 2)]
      (.. (js/expect next-data) (toEqual {:one 1 :two 2})))))


(js/test
  "null"
  (fn []
    (let [n nil]
      (.. (js/expect n) (toBeNull));
      (.. (js/expect n) (toBeDefined));
      (.. (js/expect n) -not (toBeUndefined));
      (.. (js/expect n) -not (toBeTruthy));
      (.. (js/expect n) (toBeFalsy)))));


(js/test
  "zero"
  (fn []
    (let [n 0]
      (.. (js/expect n) -not (toBeNull));
      (.. (js/expect n) (toBeDefined));
      (.. (js/expect n) -not (toBeUndefined));
      (.. (js/expect n) -not (toBeTruthy));
      (.. (js/expect n) (toBeFalsy)))));

(js/test
  "Two plus Two"
  (fn []
    (let [value (+ 2 2)]
      (.. (js/expect value) (toBeGreaterThan 3))
      (.. (js/expect value) (toBeGreaterThanOrEqual 3.5))
      (.. (js/expect value) (toBeLessThan 5))
      (.. (js/expect value) (toBeLessThanOrEqual 4.5))
      (.. (js/expect value) (toBeLessThanOrEqual 4))
      (.. (js/expect value) (toBeLessThanOrEqual 4)))))


(js/test
  "there is no I in team"
  (fn []
    (.. (js/expect "team") -not (toMatch "/I/"))))


(js/test
  "the shopping list has beer on it"
  (fn []
    (let [shopping-list ["diapers"
                         "kleenex"
                         "trash bags"
                         "paper towels"
                         "beer"]]
      (.. (js/expect shopping-list) (toContain "beer")))))
