import * as THREE from "three";
import * as ARCH from "@/archiweb"


import {DragControls} from "three/examples/jsm/controls/DragControls";

let scene, renderer, gui, camera;
let drag, controller;
let curve, gb, line;
let count = 100;
let tans = [];
let cl = [];
let left, right;

function initScene() {
  
  const axes = new THREE.AxesHelper(50)
  scene.add(axes);
  gb = new ARCH.GeometryFactory(scene);
  let controls = {
    color: 0xfafafa
  };
  scene.background = new THREE.Color(controls.color);
  
  gui.gui.addColor(controls, 'color').onChange(function () {
    scene.background = new THREE.Color(controls.color);
  });
  
  
  const light = new THREE.SpotLight(0xffffff, 1.5);
  light.position.set(0, 0, 1000);
  scene.add(light);
  
  let pos = [[-10, 30], [0, 10], [30, -10], [40, -30], [50, -50]];
  for (let p of pos) {
    cl.push(gb.Cylinder(p, [1, 1],
      new THREE.MeshLambertMaterial({color: 0xff0000})));
  }
  
  curve = new THREE.CatmullRomCurve3(cl.map((handle) => handle.position));
  curve.curveType = "centripetal";
  
  const points = curve.getPoints(50);
  
  
  for (let i = 0; i < count; ++i) {
    tans.push(gb.Segments(null, false, 0xff000));
    tans[i].rotation.z = Math.PI / 2;
    scene.add(tans[i]);
  }
  left = gb.Segments(null, false, 0xff0000);
  right = gb.Segments(null, false, 0x0000ff);
  updateCurve();
  
  line = gb.Segments(points)
  
  ARCH.refreshSelection(scene);
  
}

function initDrag() {
  drag = new DragControls(cl, camera, renderer.domElement);
  drag.addEventListener('hoveron', function (event) {
    // console.log(event)
    let o = event.object;
    if (o.type === 'Cylinder') o.toInfoCard();
    controller.enabled = false;
  });
  drag.addEventListener('hoveroff', function () {
    controller.enabled = true;
  });
  
  drag.addEventListener('dragend', function (event) {
    let o = event.object;
    if (o.type === 'Cylinder') o.toInfoCard();
  });
  drag.addEventListener('drag', function () {
    const points = curve.getPoints(500);
    line.geometry.setFromPoints(points);
    updateCurve();
  
  })
}

function updateCurve() {
  for (let i = 0; i < count; ++i) {
    const t = i * (1. / count);
    let point = curve.getPointAt(t);
    let tangent = curve.getTangentAt(t);
    
    let e1 = tangent.clone().multiplyScalar(3);
    let e2 = tangent.clone().multiplyScalar(-3);
    
    tans[i].geometry.setFromPoints([e1, e2]);
    tans[i].position.copy(point);
  }
  const l = [];
  const r = [];
  for (let i = 0; i < count; ++i) {
    let normal = tans[i].geometry.clone();
    normal.applyMatrix4(tans[i].matrix);
    let position = normal.attributes.position;
    l.push(new THREE.Vector3(position.getX(0), position.getY(0), position.getZ(0)));
    r.push(new THREE.Vector3(position.getX(1), position.getY(1), position.getZ(1)));
  }
  
  let cv = new THREE.CatmullRomCurve3(l);
  left.geometry.setFromPoints(cv.getPoints(100))
  cv = new THREE.CatmullRomCurve3(r);
  right.geometry.setFromPoints(cv.getPoints(100))
}

function updateObject(uuid, position) {
  const o = scene.getObjectByProperty('uuid', uuid);
  o.position.copy(position);
  gb.Curve(window.objects);
}

function draw() {

}

function main() {
  const viewport = new ARCH.Viewport();
  renderer = viewport.renderer;
  
  scene = viewport.scene;
  gui = viewport.gui;
  controller = viewport.controller;
  
  camera = viewport.to2D();
  
  initScene();
  initDrag();
  
  viewport.draw = draw;
}

export {
  main,
  updateObject,
  
}
