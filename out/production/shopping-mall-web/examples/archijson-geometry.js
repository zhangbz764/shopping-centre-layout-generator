import * as ARCH from "@/archiweb"
import {building} from "@/assets/models/csg";
// import * as THREE from "three";

let scene, renderer, gui;
let geoFty, matFty, astMgr;
let archijson;
let reconstructed = [];
let balls = []
let segments, prism;
let vertices;
let mesh;

/* ---------- GUI setup ---------- */
function initGUI() {
  const control = {
    send: function () {
      reconstructed.forEach((it) => {
        it.parent.remove(it);
      })
      reconstructed = [];
      
      astMgr.setCurrentID(1);
      archijson.sendArchiJSON('bts:sendGeometry', window.objects);
    }
  }
  gui.gui.add(control, 'send');
}

function parseGeometry(geometryElements) {
  for (let e of geometryElements) {
  
    let b = scene.getObjectByProperty('uuid', e.uuid);
    
    if (!b) {
      if (e.type === 'Mesh') {
        console.log(e)
        b = geoFty.Mesh(e.vertices, e.faces);
        console.log(b)
    
      } else {
        b = geoFty[e.type]();
        reconstructed.push(b);
      }
    }
    
    b.fromArchiJSON(b, e);
  }
  ARCH.refreshSelection(scene);
}


/* ---------- create your scene object ---------- */
function initScene() {
  geoFty = new ARCH.GeometryFactory(scene);
  matFty = new ARCH.MaterialFactory();
  archijson = new ARCH.ArchiJSON(scene, geoFty);
  
  const cuboid = geoFty.Cuboid([100, 100, 0], [200, 200, 300], matFty.Matte(0xff0000));
  
  const cylinder = geoFty.Cylinder([400, 300, 0], [100, 400], matFty.Matte(0xffff00), true);
  
  const plane = geoFty.Plane([-600, 300, 5], [600, 600], matFty.Matte(0xff00ff), true)
  
  // let points = [[-190, 730, 6], [320, 940, 6], [520, 640, 6], [240, 410, 6], [50, 500, 6], [-110, 460, 6]]
  let points = [
    [-110, 460, 6],
    [50, 500, 6],
    [240, 410, 6],
    [520, 640, 6],
    [320, 940, 6],
    [-190, 730, 6]
  ]
  // points = points.reverse();
  points.forEach((p) => balls.push(geoFty.Sphere(p, 10, matFty.Flat(0xff0000))));
  
  segments = geoFty.Segments(balls.map((handle) => handle.position), true);
  balls.forEach((c) => c.parent = segments);
  //
  prism = geoFty.Prism(segments,
    matFty.Matte(0x0000ff), 5, 1)
  
  /* ---------- generate points each 5000 area ---------- */
  vertices = geoFty.Vertices(geoFty.pointsInsideSegments(segments, 5000), 6)
  
  
  /* ---------- generate mesh ---------- */
  mesh = geoFty.Mesh({coordinates: building.verts.flat(), size: 3}, {index: building.faces.flat()}, matFty.Flat());
  // mesh = geoFty.Mesh(polygonmesh.vertices, polygonmesh.faces, matFty.Flat())
  
  /* ---------- refresh global objects ---------- */
  ARCH.refreshSelection(scene);
  astMgr.addSelection(balls, 2)
  astMgr.addSelection([cuboid, cylinder, plane, segments, vertices, mesh], 1);
  astMgr.setCurrentID(1);
  
  
  /* ---------- handle returned object ---------- */
  archijson.parseGeometry = parseGeometry;
}

window.searchSceneByUUID = function (uuid) {
  return scene.getObjectByProperty('uuid', uuid);
}


function draw() {
  if (segments && segments.dragging) {
    
    segments.geometry.setFromPoints((balls.map((handle) => handle.position)))
    prism.updateModel(prism, {segments: segments.modelParam(segments), height: 5, extruded: 1});
    
    vertices.geometry.setFromPoints(geoFty.pointsInsideSegments(segments, 5000))
  }
  
}


/* ---------- main entry ---------- */
function main() {
  const viewport = new ARCH.Viewport();
  renderer = viewport.renderer;
  scene = viewport.scene;
  gui = viewport.gui;
  
  astMgr = viewport.enableAssetManager();
  viewport.enableDragFrames();
  viewport.enableTransformer();
  
  viewport.draw = draw;
  initGUI();
  initScene();
  
  const sceneBasic = new ARCH.SceneBasic(scene, renderer);
  sceneBasic.floorColor = '#ffffff';
  sceneBasic.floor.material.color.set(sceneBasic.floorColor);
  sceneBasic.addGUI(gui.gui);
}

export {
  main
}