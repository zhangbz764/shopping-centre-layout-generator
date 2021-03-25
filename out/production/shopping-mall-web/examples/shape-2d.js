import * as ARCH from '@/archiweb'
import * as THREE from 'three'

let scene, gui, renderer;
let camera, gb, mf;
let DRAWMODE = false;
const raycaster = new THREE.Raycaster();
const xoy = new THREE.Plane(new THREE.Vector3(0, 0, 1), 0);

let b, arr, p, path;

const lines = [];
const shapes = [];

function getRandomInt(num) {
  return Math.floor(Math.random() * Math.floor(num));
}

const controls = new function () {
  this.draw = function () {
    DRAWMODE = !DRAWMODE;
    renderer.domElement.style.cursor = DRAWMODE ? 'pointer' : 'auto';
    if (DRAWMODE) arr = [];
  }
  this.shape = function () {
    let segs = gb.Segments(path.getPoints());
    lines.push(segs);
    shapes.push(gb.Prism(segs, mf.Matte(), 0, getRandomInt(20), true))
    arr = [];
  }
  this.clear = function () {
    lines.forEach((item) => {
      scene.remove(item);
    })
    p.geometry.setFromPoints([new THREE.Vector3()]);
    arr = [];
  }
};


function onClick(event) {
  console.log(event)
  if (DRAWMODE) {
    const mouse = new THREE.Vector2(
      (event.clientX / window.innerWidth) * 2 - 1,
      -(event.clientY / window.innerHeight) * 2 + 1
    )
    raycaster.setFromCamera(mouse, camera);
    let pt = raycaster.ray.intersectPlane(xoy, new THREE.Vector3());
    b.position.copy(pt);
    console.log(pt)
    arr.push(pt);
    
    path = new THREE.Path();
    path.moveTo(arr[0].x, arr[0].y)
    for (let i = 1; i < arr.length; ++i) {
      path.lineTo(arr[i].x, arr[i].y);
    }
    path.lineTo(arr[0].x, arr[0].y);
    
    p.geometry.setFromPoints(path.getPoints());
    
  }
}

function initScene() {
  mf = new ARCH.MaterialFactory();
  gb = new ARCH.GeometryFactory(scene);
  b = gb.Cuboid([2, 2, 0], [2, 2, 2]);
  p = gb.Segments(null, 0xaaaaaa);
  
  /* ---------- light ---------- */
  const light = new THREE.SpotLight(0xffffff, 1.5);
  light.position.set(0, 0, 1000);
  scene.add(light);
  const light2 = new THREE.SpotLight(0xffffff, 3);
  light2.position.set(100, -100, 1000);
  scene.add(light2);
  
  const helper = new THREE.PlaneHelper(xoy, 1, 0xffff00);
  scene.add(helper);
}

function main() {
  const viewport = new ARCH.Viewport();
  renderer = viewport.renderer;
  scene = viewport.scene;
  gui = viewport.gui.gui;
  camera = viewport.to2D();
  
  gui.add(controls, 'draw');
  gui.add(controls, 'shape');
  gui.add(controls, 'clear');
  renderer.domElement.addEventListener('click', onClick, false);
  
  initScene();
  
  
}

export {
  main
}