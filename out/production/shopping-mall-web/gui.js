"use strict";
import * as dat from 'dat.gui';

let gui, util;

const controls = new function () {
  this.info = function () {
    window.InfoCard.hideInfoCard(!window.InfoCard.show);
  };
};

function initGUI() {
  gui = new dat.GUI({autoPlace: false});
  
  util = gui.addFolder('Utils');
  util.add(controls, 'info');
  util.open();
  
  const container = document.getElementById('gui-container');
  container.appendChild(gui.domElement);
}

export {
  gui,
  util,
  initGUI
}
