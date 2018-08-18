var fs = require('fs');
var vm = require('vm');
var path = require('path');

global.CLOSURE_BASE_PATH = '../../target/public/cljs-out/test/goog/';

global.CLOSURE_IMPORT_SCRIPT = function(src) {
  require(src);
  return true;
};

var globalContext = vm.createContext(global);

const base = fs.readFileSync(
  path.resolve(__dirname, '../../target/public/cljs-out/test/goog/base.js')
);

const cljs_deps = fs.readFileSync(
  path.resolve(__dirname, '../../target/public/cljs-out/test/cljs_deps.js')
);

vm.runInContext(base, globalContext, { filename: 'base.js' });

vm.runInContext(cljs_deps, globalContext, { filename: 'cljs_deps.js' });
