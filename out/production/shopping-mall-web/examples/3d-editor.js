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
  
  const b4 = gb.Cylinder([330, 430, 0], [50, 100], mt.Matte(), true);
  
  const loader = new ARCH.Loader(scene);
  loader.addGUI(gui.util);
  
  loader.loadModel('http://model.amomorning.com/tree/spruce-tree.dae', (mesh) => {
    mesh.position.set(0, -300, 0);
    ARCH.setMaterial(mesh, new THREE.MeshLambertMaterial({color: 0x99A083, transparent: true, opacity: 0.8}))
    ARCH.setPolygonOffsetMaterial(mesh.material);
    mesh.toCamera = true;
    assetManager.refreshSelection(scene);
  });
  
  loader.loadModel('http://model.amomorning.com/tree/autumn-tree.dae', (mesh) => {
    mesh.position.set(500, 0, 0);
    mesh.scale.set(2, 2, 2);
    ARCH.setPolygonOffsetMaterial(mesh.material);
    ARCH.setMaterialOpacity(mesh, 0.6);
    mesh.toCamera = true;
    assetManager.refreshSelection(scene);
  });
  const points = [
    new THREE.Vector2(100, 100),
    new THREE.Vector2(200, 100),
    new THREE.Vector2(200, 0),
    new THREE.Vector2(300, 0),
    new THREE.Vector2(300, 300),
    new THREE.Vector2(100, 300),
  ];
  const v = new THREE.Vector2(100, 200);
  points.forEach((pt) => {
    pt.add(v);
  })
  const segs = gb.Segments(points, true)
  segs.visible = false;
  const s1 = gb.Prism(segs, mt.Matte(), 100, 10, true);
  
  assetManager.refreshSelection(scene);
  assetManager.addSelection([b1, b2, b3, b4, s1], 1);
  assetManager.setCurrentID(1);
  
}


// APIs

function updateObject(uuid, model) {
  const o = scene.getObjectByProperty('uuid', uuid);
  o.updateModel(o, model);
}

window.searchSceneByUUID = function (uuid) {
  return scene.getObjectByProperty('uuid', uuid);
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
  main,
  updateObject,
}
