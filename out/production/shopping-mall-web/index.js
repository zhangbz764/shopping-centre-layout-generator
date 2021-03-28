/* eslint-disable no-unused-vars,no-case-declarations */
"use strict";
import * as THREE from 'three'
import * as ARCH from "@/archiweb"

let renderer, scene, gui;

let camera;

let gb, assetManager;

function initScene() {
  scene.background = new THREE.Color(0xfafafa);
  
  
  gb = new ARCH.GeometryFactory(scene);
  const mt = new ARCH.MaterialFactory();
  
  const b1 = gb.Cuboid([150, 150, 0], [300, 300, 300], mt.Matte());
  
  const b2 = gb.Cuboid([-300, -300, 0], [300, 300, 100], mt.Matte());
  
  const b3 = gb.Cuboid([300, -500, 0], [300, 300, 150], mt.Matte());
  
  
  assetManager.refreshSelection(scene);
  assetManager.addSelection([b1, b2, b3], 1);
  assetManager.setCurrentID(1);
  
}

function main() {
  const viewport = new ARCH.Viewport();
  scene = viewport.scene;
  renderer = viewport.renderer;
  gui = viewport.gui;
  camera = viewport.camera;
  
  assetManager = viewport.enableAssetManager();
  viewport.enableDragFrames();
  viewport.enableTransformer();
  
  initScene();
  
  const sceneBasic = new ARCH.SceneBasic(scene, renderer);
  sceneBasic.addGUI(gui.gui);
  
}

export {
  main
}
