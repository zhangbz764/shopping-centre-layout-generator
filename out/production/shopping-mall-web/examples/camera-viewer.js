"use strict";
import * as THREE from 'three'
import * as ARCH from "@/archiweb"

/**
 *
 */

let scene, renderer, gui;

function initScene() {
  /* ---------- create basic geometry ---------- */
  const gb = new ARCH.GeometryFactory(scene);
  gb.Cuboid([150, 150, 0],
    [300, 300, 300],
    new THREE.MeshLambertMaterial({color: 0xdddddd}));
  
  gb.Cuboid([-300, -300, 0],
    [300, 300, 100],
    new THREE.MeshLambertMaterial({color: 0xdddddd}));
  
  gb.Cuboid([300, -500, 0],
    [300, 300, 150],
    new THREE.MeshLambertMaterial({color: 0xdddddd}));
  
  gb.Cylinder([-300, 0, 0],
    [50, 300, 4],
    new THREE.MeshLambertMaterial({color: 0xdddddd}), true);
  
  /* ---------- loading assets ---------- */
  const loader = new ARCH.Loader(scene);
  loader.addGUI(gui.util);
  
  loader.loadModel('http://model.amomorning.com/tree/spruce-tree.dae', (mesh) => {
    /* ----------  set position  ---------- */
    mesh.position.set(0, -300, 0);
    /* ----------  set material  ---------- */
    ARCH.setMaterial(mesh, new THREE.MeshLambertMaterial({color: 0x99A083, transparent: true, opacity: 0.8}))
    ARCH.setPolygonOffsetMaterial(mesh.material);
    /* ---------- set attributes ---------- */
    mesh.toCamera = true;
  });
  
  loader.loadModel('http://model.amomorning.com/tree/autumn-tree.dae', (mesh) => {
    /* ----------  set position  ---------- */
    mesh.position.set(500, 0, 0);
    mesh.scale.set(2, 2, 2);
    /* ----------  set material  ---------- */
    ARCH.setMaterialOpacity(mesh, 0.6);
    ARCH.setPolygonOffsetMaterial(mesh.material);
    /* ---------- set attributes ---------- */
    mesh.toCamera = true;
  });
  
  /* ---------- refresh to enable group and highlight ---------- */
  ARCH.refreshSelection(scene);
}


// Exporter
function main() {
  const viewport = new ARCH.Viewport();
  
  scene = viewport.scene;
  renderer = viewport.renderer;
  gui = viewport.gui;
  
  initScene();
  
  const sceneBasic = new ARCH.SceneBasic(scene, renderer);
  sceneBasic.addGUI(gui.gui);
}

export {
  main,
}
