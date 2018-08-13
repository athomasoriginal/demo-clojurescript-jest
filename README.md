# DEMO REAGENT + JEST

Jest and CLJS

## Quickstart

- Install deps

  ```bash
  yarn
  ```

- Compile tests

  ```bash
  clj -A:cljs-tests
  ```

## Issues

- Jest globals are not available automatically

  You will notice that in jest tests you do not have to import `test`, `expect` etc. These are available [globally](https://jestjs.io/docs/en/api). However, we have to compile our code we need to import these. Three ways to handle this:

  In order to overcome this, we say `js/expect`. This is not bad because it indicates that these things are globals.

- Jest is going to replace CLJ test library

  We could split our tests into the ones that use components and the ones that do not and in this way, keep both frameworks

- Jest won't recognize compilers filename output

  Jest expects `module.test.js` but CLJS will output as `module_test.js`. The solution is to modify the test name search path in jest configs. To resolve this, we add Jest configs in the `package.json` - `"**/*+(_test).js"` for `testMatch`.

- Teaching jest how to load google closure libraries

  This is because when we run this in the browser, `goog` is a global and available to us because its imported ahead of our main code. To do this, we use `setupFiles` which allows us to specify things like global functions before running tests.

  However, google closure library thinks by default that its running in the DOM. So we have to find some nice way of requiring it so Jest tests can understand what is going on.

## Guide

The following are setup steps that I included in the event someone wants to see how I came to the current implementation. You don't need to run through these yourself, its mostly as a reminder for myself.

### Basic Jest Setup

- Init package.json

  ```bash
  yarn init -y
  ```

- Add jest deps

  ```bash
  yarn add --dev jest
  ```

### Getting Started

If we start with the [getting started section](https://jestjs.io/docs/en/getting-started) of Jest start by converting the Jest test to CLJS:

- demo.core

  ```clojure
  ;; js
  function sum(a, b) {
    return a + b;
  }

  ;; cljs
  (defn sum [a b]
    (+ a b))
  ```

- demo.core_test

  ```clojure
  ;; js
  test('adds 1 + 2 to equal 3', () => {
    expect(sum(1, 2)).toBe(3);
  });

  ;; cljs
  (js/test
    "Adds 1 + 2 to equal 3"
    (.. (js/expect (component/sum 1 2)) (js/toBe 3)))
  ```

  > Notice we are using the `js/` namespace, these are globals so it must be done

Before we can run jest against our tests, we have to compile our clojurescript. To make things easier we will use [figwheel.main](https://figwheel.org/)

- create a figwheel build

  see `test.cljs.dev`

- create a compile-tests alias in deps.edn

  ```clojure
  ;; ...

  :aliases {:cljs-tests {:main-opts ["-m" "figwheel.main" "--build test"]}}
  ```

  > tells figwheel to use the `test.cljs.dev` build we identified above

- Run figwheel

  ```clojure
  clj -A:cljs-tests
  ```

- Run jest

  ```bash

  ```

## Resources

[Node JS Modules From CLJS](https://anmonteiro.com/2017/03/requiring-node-js-modules-from-clojurescript-namespaces/)
