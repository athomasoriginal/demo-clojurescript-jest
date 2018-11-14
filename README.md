# DEMO ClojureScript + Jest

Jest and CLJS. I always wondered why I never saw anyone using Jest with CLJS. As ClojureScript developers we lean heavily on React and the React ecosystem. Why not Jest?

- [Quickstart](#quickstart)
- [TODO](#todo)
- [Issues](#issues)
- [Breakdown](#breakdown)
  - [Basic Jest Setup](#basic-jest-setup)
  - [Getting Started](#getting-started)
  - [Multiple Tests](#multiple-tests)
  - [Snapshot Testing](#snapshot-testing)
    - [Step 1 Install Test Dependencies](#step-1-install-test-dependencies)
    - [Step 2 Setup webpack externs bundle](#step-2-setup-webpack-externs-bundle)
    - [Step 3 Write your react component test](#step-3-write-your-react-component-test)
    - [Gotchas](#gotchas)

## Quickstart

- Install deps

  ```bash
  yarn
  ```

- Build Dependencies

  ```bash
  yarn webpack
  ```

  > will create a `./dist/index_test_bundle.js` file in your root directory

- Compile tests

  ```bash
  clj -A:cljs-tests
  ```

- Run tests

  ```bash
  yarn test
  ```

## TODO

- [x] runtime speed with more advanced compiler settings
- [x] snapshot example
- [x] execute multiple test files in same dir
- [ ] medium complexity reagent example
- [ ] async example
- [ ] run tests as extra mains (watch) - [figwheel.main](https://figwheel.org/docs/extra_mains.html)
  - [ ] chain yarn tests to figwheel compilation
- [ ] execute all tests in the test dir
- [ ] example of running specific tests by file or name
- [ ] Structuring tests
- [ ] Clojure assertions
- [ ] test performance
- [ ] compilation errors
  - [ ] figwheel does not always return informative messages: incorrectly import something
- [x] Make react included the developer version
- [ ] Improve naming conventions for webpack and where the folders/files are located
- [ ] Troubleshooting externs
  - [ ] ability to add externs without losing reagent losing its internal react reference
- [ ] move snapshot tests outside of target directory (Jest 24)

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

- Runtime speed

  The first time I ran jest + cljs, I noticed that the time to run was much longer than I remembered when just using Jest and vanilla JS. So I put together another demo to just see vanilla jest run speed. The difference in first run speeds: `1.69s.` (vanilla) v. `13.25s` (cljs jest). The second run speeds however are dramatically improved at `1.50s` (vanilla) v.`2.09s` (cljs jest).

  My theory is that the reason for this happening is because of the fact that we are loading `cljs.core` and `google.closure.library`. To test this, I increase the compiler level to `:simple` in the `test.cljs.edn` file. This will create a file in `target/public/cljs-out` called `test-main.js` and `removes whitespace and shortens local variable names` (2100 line file). The result will be all of your JS in one file vs. spread across multiple files. We also have to update the `yarn test` script to execute the `test-main.js` file instead of our other file and also add in `"**/*+(-main).js"` so Jest knows how to find the `test-main.js` file.

  Once the above is done, we can run `yarn test` and we find that our tests run at `6.50s` (cljs jest + `:simple`) v. `13.25s` (cljs jest + `:none`) for cold start and the second start is now down to `1.75s`. Right on. Could we save more time?

  The answer is yes. We can run the compiler with `:advanced` (21 loc) and we can get jest to run initially at `1.75s` and then each subsequent run will be around `1.50s`. The issue with this one is that it seems that the google closure compiler is renaming `.toBe` to `h`, so I had to manually change this, but in truth, I doubt anyone is going to need to run this in advanced mode and just knowing this can work and the time savings are available to us is fine for now.

  I am not saying that CLJS compiled JS is faster here, I am just noting that there are a lot of libraries and extra code that come with it, but there are ways to improve the performance. For local development, running things with `:none` is fine. However, if you are running for a CI/CD flow, we might run with `:simple` to get some speed improvements? No idea. Just food for thought.

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

- demo.utils

  ```clojure
  ;; js
  function sum(a, b) {
    return a + b;
  }

  ;; cljs
  (defn sum [a b]
    (+ a b))
  ```

- demo.utils_test

  ```clojure
  ;; js
  test('adds 1 + 2 to equal 3', () => {
    expect(sum(1, 2)).toBe(3);
  });

  ;; cljs
  (js/test
    "Adds 1 + 2 to equal 3"
    (.. (js/expect (utils/sum 1 2)) (js/toBe 3)))
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

### Multiple Tests

In the getting started section we only had one file with tests. This means that our `yarn test` command was simple. We just told it to run the one file we had. However, as your project scales you are going to want to tell it how to run more than just one file. This section will explain how to scale to more than one file in the same dir. So lets update our `package.json` npm `test` script to look like this:

```bash
jest --verbose target/public/cljs-out/test/demo/*
```

### Snapshot Testing

In order to reproduce the minimal react snapshot test as outlined in the [Jest documentation](https://jestjs.io/docs/en/snapshot-testing) you need to perform the following setup:

1.  Install test dependencies
2.  Setup webpack externs bundle

#### Step 1 Install Test Dependencies

- Install dependencies

  ```bash
  yarn add -D react react-dom create-react-class react-test-renderer
  ```

#### Step 2 Setup webpack externs bundle

- Install webpack

  ```bash
  yarn webpack
  ```

- Configure test.cljs.edn

  ```clojure
  :npm-deps      false
  :infer-externs true
  :foreign-libs [{:file            "dist/index_test_bundle.js"
                   :provides       ["react"
                                    "react-dom"
                                    "create-react-class"
                                    "renderer"]
                   :global-exports {react              React
                                    react-dom          ReactDOM
                                    create-react-class createReactClass
                                    renderer           renderer}}]}
  ```

- Create your externs bundle

  ```javascript
  import React from "react";
  import ReactDom from "react-dom";
  import createReactClass from "create-react-class";
  import renderer from "react-test-renderer";

  window.React = React;
  window.ReactDOM = ReactDom;
  window.createReactClass = createReactClass;
  window.renderer = renderer;
  ```

- Compile your webpack externs bundle

  ```bash
  yarn webpack
  ```

#### Step 3 Write your react component test

The javascript version of Jest's example snapshot test looks like this:

```javascript
import React from "react";
import Link from "../Link.react";
import renderer from "react-test-renderer";

it("renders correctly", () => {
  const tree = renderer
    .create(<Link page="http://www.facebook.com">Facebook</Link>)
    .toJSON();
  expect(tree).toMatchSnapshot();
});
```

Convert the above into ClojureScript:

```clojure
(ns demo.component-test
  (:require [demo.component :as component]
            [reagent.core :as r]
            [renderer]))

(js/it
  "Render correctly"
  (fn []
    (let [button [component/button
                  {:class "test-class"
                   :type  "button"}]
            tree   (.. renderer (create (r/as-element button)) (toJSON))]
        (.. (js/expect tree) (toMatchSnapshot)))))
```

And now we can run our tests

```bash
yarn test
```

#### Gotchas

This section is going to review a bunch of the problems I faced when working through the above:

- which version of React?

  When you use the webpack externs bundle in tests Reagent will also be using the same version as in your externs bundle. The reason to mention this is because if you happen to be writing your main application without an externs bundle you may be on a different version then the one you are testing with in Jest. So how does this work?

  Webpack externs will create a global instance of `React`. This version is going to be picked up by Reagent. So how can you verify this? One way:

  ```javascript
  (js/console.log "After checks")
  (js/console.log (.. js/React -version))

  (js/console.log "Before test checks")
  (js/console.log (.. js/reagent -impl -template -global$module$react))
  ```

  All I am saying is be careful to take note of what you are using because if you are using an older version of Reagent then you are testing you can and will get some weird inconsistencies.

- Why do I need to install `react` and friends?

  `react-test-renderer` references a global `React` namespace (along with the other 3 libraries we installed in step 1). If you do not include these, `react-test-renderer` will not work. Is this a problem? Generally, no. Unless of course the question above is an issue then this could be a problem.

- Do I have to manually build my externs in `test.cljs.edn`?

  No. Figwheel can dynamically generate this for you if you add the following to the meta information section of your `test.cljs.edn` file

  ```clojure
  :npm      {:bundles {"dist/index_test_bundle.js" "src/js/index.js"}}}
  ```

  For more information see the official [figwheel npm setup guide](https://figwheel.org/config-options#npm)

* I am seeing weird errors about call of undefined?

  Assuming you got Jest working on its own, these are likely `Reagent` or `react-test-renderer` not being back to find their dependencies. To resolve see step 1 and 2 and carefully read the messages.

- Stale tests failing?

  sometimes when you change a files name, stale tests can be left behind and jest will try to test them anyways. In this case, just clear the `target` directory and trying compiling from scratch. The important takeaway is that this is not Jest failing. This is an issue with compilation.

* Capitalization / spelling of externs

  Don't capitalize React. It will not be found. This is another reason for defining your own externs in `test.cljs.edn`. You want to control the names used.

* [Not a valid react component](https://reactjs.org/docs/error-decoder.html/?invariant=31&args%5B%5D=object%20with%20keys%20%7Bkey%2C%20val%2C%20__hash%2C%20cljs%24lang%24protocol_mask%24partition0%24%2C%20cljs%24lang%24protocol_mask%24partition1%24%7D&args%5B%5D=)

  The following message happens in jest when you trying rendering a reagent component incorrectly.

  But there is more wrong with this. we don't see the react message inline. Lets make this a developer version of react.

  reason for this is when you pass reagent component -> react component

- snapshots will be stored beside the tests

  the issue that that target dir is getting rewritten all the time so syou get a message like:

  ```clojure
  1 snapshot obsolete.
  ```

  The good news is that this should be resolved in [jest 24](https://github.com/facebook/jest/pull/6143)

## When to use Jest

I would lean on Jest for front end specific testing

- When we want to test our reagent components - do they render? does their behaviour work as expected?
- When we want to test screen integration tests

## Resources

- [Node JS Modules From CLJS](https://anmonteiro.com/2017/03/requiring-node-js-modules-from-clojurescript-namespaces/)
- [Webpack Example Config](https://github.com/koba04/closure-webpack-example/blob/master/webpack.config.js)
- [Loading GCL from Node](http://blog.codekills.net/2012/01/10/loading-google-closure-libraries-from-node.js/)
- [Jest Github Issue](https://github.com/facebook/jest/issues/2417)
- [Learning VM's](https://60devs.com/executing-js-code-with-nodes-vm-module.html)
- [EventBrite Testing Best Practices](https://github.com/eventbrite/javascript/blob/master/react/testing.md)
- [React tests - structuring best practices](https://techblog.commercetools.com/testing-in-react-best-practices-tips-and-tricks-577bb98845cd)
