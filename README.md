# DEMO ClojureScript + Jest

Jest and CLJS. I always wondered why I never saw anyone using Jest with CLJS. As ClojureScript developers we lean heavily on React and the React ecosystem. Why not Jest?

- [Quickstart](#quickstart)
- [Issues](#issues)
- [Breakdown](#breakdown)
  - [Basic Jest Setup](#basic-jest-setup)
  - [Getting Started](#getting-started)

## Quickstart

- Install deps

  ```bash
  yarn
  ```

- Compile tests

  ```bash
  clj -A:cljs-tests
  ```

- Run tests

  ```bash
  yarn test
  ```

## Issues

- Jest globals are not available automatically

  When writing tests in Jest, you are provided many [global](https://jestjs.io/docs/en/api) functions like `test`, `expect` etc. To write this in ClojureScript, we have to use some JS interop. This means that in order to access any of the jest globals, we have to prefix them with `js`. For example, if we want to use `expect` we would write, in our ClojureScript, `js/expect`.

- Jest is going to replace CLJ test library

  This is not so much an issue, as a point of clarification. Clojure provides an awesome test library. Its simple, small and works. With this in mind, by going with Jest, at least to my knowledge at this point, we would move over whole sale, for the front end related code, to Jest.

- Jest won't recognize how the compiler names files

  Out of the box, Jest expects that the files it receives be formatted as `module.test.js`. However, base on how we write CLJ/S files in closure, our files will be outputted as `module_test.js`. In order to get Jest to recognize our files, we have to update the jest config. We do this in the `package.json`. See the property called `testMatch` in our `package.json`

- Teaching jest how to load google closure libraries

  There are several ways to get this two work, the two I explored ranged from easy to really have to build a proper mental model:

  **Solution 1:** Run `:simple` optimizations which puts everything into one test and now we don't have to worry about anything. The downside is I am wondering if there are performance implications. We would have to test this 1:1 with the JS version and see what happens.

  **Solution 2:** Actually overwrite and import in a Node friendly way

  The first thing is we have to make `goog` available everywhere. The second thing is that `google closure`'s module system (`goog.provide`, `goog.require` etc) has an initial assumption which should be considered when trying to get this part to work: that its running in an HTML file. What this means, and I am skirting around some other stuff to keep this brief, is that when a file has a `goog.require` line in it, closure is going to try to write a script to the HTML. This script will then import the contents of the required file.

  The resolve the first item, we do this by loading, and evaluating, the contents of `base.js` into the current runtime. Now that we have `goog` available, we still run into a problem with the module system. Yet, its not where you initially think it would be. `goog.require` and `goog.provide` work. However, if you try to access something you required, it won't be available. This is because, as noted above, the native behaviour of the require is to write a script.

  In order to make it work, we have to overwrite some of the variables in google closure. Specifically, we are going to overwrite `CLOSURE_IMPORT_SCRIPT` and `CLOSURE_BASE_PATH`. This will make it so when we `require` something, its not going to try to write a script to the DOM, its going to use Nodes require and put all the JS we need into our context.

  > Note that you do not have to set the `CLOSURE_BASE_PATH` var. You could just prefix a relative path in front of `src` in the require. However, this is cleaner as `CLOSURE_BASE_PATH` is used in `base.js` to build the `src` we use in `CLOSURE_IMPORT_SCRIPT`. With this said, keep in mind that if you are setting this on a different project and your paths are not the same as mine, the rule of thumb is that `CLOSURE_BASE_PATH` has to eventually lead to where `base.js` lives.

## Breakdown

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
  yarn test
  ```

## Resources

[Node JS Modules From CLJS](https://anmonteiro.com/2017/03/requiring-node-js-modules-from-clojurescript-namespaces/)
[Webpack Example Config](https://github.com/koba04/closure-webpack-example/blob/master/webpack.config.js)

http://blog.codekills.net/2012/01/10/loading-google-closure-libraries-from-node.js/
https://github.com/facebook/jest/issues/2417
https://60devs.com/executing-js-code-with-nodes-vm-module.html - learning vms
