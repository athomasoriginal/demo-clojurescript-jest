var fs = require('fs');
var vm = require('vm');
var path = require('path');

global.CLOSURE_BASE_PATH = '../../target/public/cljs-out/test/goog/';

global.CLOSURE_IMPORT_SCRIPT = function(src) {
  require(src);
  return true;
};

var globalContext = vm.createContext(global);

const content = fs.readFileSync(
  path.resolve(__dirname, '../../target/public/cljs-out/test/goog/base.js')
);

const contentTwo = fs.readFileSync(
  path.resolve(__dirname, '../../target/public/cljs-out/test/cljs_deps.js')
);

vm.runInContext(content, globalContext, { filename: 'base.js' });

vm.runInContext(contentTwo, globalContext, { filename: 'cljs_deps.js' });
