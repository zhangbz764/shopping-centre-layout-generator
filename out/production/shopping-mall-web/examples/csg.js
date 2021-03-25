/* eslint-disable no-unused-vars,no-case-declarations */

import * as ARCH from "@/archiweb"
import * as THREE from "three"
import {building, polygonmesh} from "@/assets/models/csg";

let scene, renderer, gui, camera;
let geoFty, matFty;
let astMgr;


/* ---------- create your scene object ---------- */
function initScene() {
  geoFty = new ARCH.GeometryFactory(scene);
  matFty = new ARCH.MaterialFactory();
  const geometry = new THREE.BufferGeometry();
  
  geometry.setAttribute('position', new THREE.Float32BufferAttribute(building.verts.flat(), 3))
  geometry.setIndex(building.faces.flat())
  geometry.computeVertexNormals()
  geometry.normalsNeedUpdate = true;
  
  const material = new THREE.MeshPhongMaterial({color: 0xdddddd, flatShading: true});
  const mesh = new THREE.Mesh(geometry, material);
  mesh.position.x = -600;
  ARCH.sceneAddMesh(scene, mesh)
  
  const position = [
    -50, 0, 0,
    50, 0, 0,
    50, 0, 100,
    200, 0, 100,
    200, 0, 300,
    -50, 0, 300
  ];
  let pts = geoFty.coordinatesToPoints(position, 3);
  
  let segs = geoFty.Segments(pts, true, 0xdddddd, true);
  segs.position.y = -600;
  let dddd = [
    [-110, 460, 6],
    [50, 500, 6],
    [240, 410, 6],
    [520, 640, 6],
    [320, 940, 6],
    [-190, 730, 6]
  ]
  
  geoFty.Segments(geoFty.coordinatesToPoints(dddd.flat(), 3), true, 0xeeeeee, true);
  
  
  let polymesh = geoFty.Mesh(polygonmesh.vertices, polygonmesh.faces, matFty.Flat());
  
  //
  ARCH.refreshSelection(scene);
}


/* ---------- main entry ---------- */
function main() {
  const viewport = new ARCH.Viewport();
  renderer = viewport.renderer;
  scene = viewport.scene;
  gui = viewport.gui;
  camera = viewport.camera;
  
  astMgr = viewport.enableAssetManager();
  viewport.enableDragFrames();
  viewport.enableTransformer();
  
  initScene();
  
  
  const sceneBasic = new ARCH.SceneBasic(scene, renderer);
  sceneBasic.addGUI(gui.gui);
}

export {
  main
}