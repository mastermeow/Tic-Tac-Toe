/*According to https://reactjs.org/blog/2015/01/27/react-v0.13.0-beta-1.html, React only supported ES6-style
(import/export) classes since version 0.13.0. So we don't use CommonJS style (require()/module.exports).*/
import React from 'react';
import ReactDOM from 'react-dom';
import App from './app';

ReactDOM.render( <App/>, document.getElementById('root'))