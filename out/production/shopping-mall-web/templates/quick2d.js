/* eslint-disable no-unused-vars,no-case-declarations */

import * as ARCH from "@/archiweb"

let scene, renderer, gui, camera;

/* ---------- GUI setup ---------- */
function initGUI() {

}


/* ---------- create your scene object ---------- */
function initScene() {
  
  // refresh global objects
  ARCH.refreshSelection();
}


/* ---------- animate per frame ---------- */
function draw() {

}


/* ---------- main entry ---------- */
function main() {
  const viewport = new ARCH.Viewport();
  renderer = viewport.renderer;
  scene = viewport.scene;
  gui = viewport.gui;
  camera = viewport.to2D();
  
  initGUI();
  initScene();
  
  viewport.draw = draw;
}

export {
  main
}