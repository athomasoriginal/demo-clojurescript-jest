const fs = require("fs");
var vm = require("vm");
var path = require("path");
const jsdom = require("jsdom");

// command line args
const args = process.argv.slice(2);
const testScript = args[0];

// helpers
function addScript(fname, window) {
  var scriptText = fs.readFileSync(fname, { encoding: "utf-8" });
  var scriptEl = window.document.createElement("script");
  scriptEl.text = scriptText;
  window.document.body.appendChild(scriptEl);
}

// setup JSDOM options
const { JSDOM, VirtualConsole } = jsdom;

const html = "<!DOCTYPE html>";

const virtualConsole = new VirtualConsole();

const options = {
  // see https://github.com/jsdom/jsdom#loading-subresources
  url: "http://localhost:9500",
  resources: "usable",
  runScripts: "dangerously",
  pretendToBeVisual: true,
  // see https://github.com/jsdom/jsdom#virtual-consoles
  virtualConsole: virtualConsole.sendTo(console)
};

// create JSDOM instance
const dom = new JSDOM(html, options);

global.window = dom.window;

// run tests
addScript(testScript, dom.window);
