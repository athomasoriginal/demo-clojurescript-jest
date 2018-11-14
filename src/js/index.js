import React from "react";
import ReactDom from "react-dom";
import createReactClass from "create-react-class";
import renderer from "react-test-renderer";

window.React = React;
window.ReactDOM = ReactDom;
window.createReactClass = createReactClass;
window.renderer = renderer;
