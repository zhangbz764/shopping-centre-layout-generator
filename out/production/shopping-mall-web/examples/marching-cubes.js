import * as THREE from 'three';
import * as ARCH from '@/archiweb'
import {MarchingCubes} from "three/examples/jsm/objects/MarchingCubes";


const clock = new THREE.Clock();
let scene, gui;
let effect, resolution;
let directLight, ambientLight;
let material;
let time = 0;

const materialFactory = new ARCH.MaterialFactory();
let reflectionCube, refractionCube, texture;

function loadTextures() {
  const path = "textures/cube/SwedishRoyalCastle/";
  const format = '.jpg';
  const urls = [
    path + 'px' + format, path + 'nx' + format,
    path + 'py' + format, path + 'ny' + format,
    path + 'pz' + format, path + 'nz' + format,
  ];
  const cubeTextureLoader = new THREE.CubeTextureLoader();
  
  reflectionCube = cubeTextureLoader.load(urls);
  refractionCube = cubeTextureLoader.load(urls);
  refractionCube.mapping = THREE.CubeRefractionMapping;
  
  texture = new THREE.TextureLoader().load("textures/uv_grid_opengl.jpg");
  texture.wrapS = THREE.RepeatWrapping;
  texture.wrapT = THREE.RepeatWrapping;
}

function initScene() {
  
  // scene.rotation.x = Math.PI/2;
  
  material = materialFactory.Doubled(0xffffff);
  resolution = effectController.resolution;
  
  effect = new MarchingCubes(resolution, material, true, true);
  effect.position.set(0, 0, 0);
  effect.rotateX(Math.PI / 2);
  effect.scale.set(700, 700, 700);
  
  effect.enableUvs = false;
  effect.enableColors = false;
  
  scene.add(effect);
  
  
}

function updateCubes(object, time, numblobs, wally, wallx, wallz) {
  
  object.reset();
  
  const subtract = 12;
  const strength = 1.2 / ((Math.sqrt(numblobs) - 1) / 4 + 1);
  
  for (let i = 0; i < numblobs; i++) {
    
    const ballx = Math.sin(i + 1.26 * time * (1.03 + 0.5 * Math.cos(0.21 * i))) * 0.27 + 0.5;
    const bally = Math.abs(Math.cos(i + 1.12 * time * Math.cos(1.22 + 0.1424 * i))) * 0.77; // dip into the floor
    const ballz = Math.cos(i + 1.32 * time * 0.1 * Math.sin((0.92 + 0.53 * i))) * 0.27 + 0.5;
    
    
    object.addBall(ballx, bally, ballz, strength, subtract);
    
  }
  
  if (wally) object.addPlaneY(2, 12);
  if (wallz) object.addPlaneZ(2, 12);
  if (wallx) object.addPlaneX(2, 12);
  
}

const effectController = {
  
  speed: 1.0,
  numBlobs: 10,
  resolution: 20,
  isolation: 80,
  
  wallx: true,
  wally: true,
  wallz: true,
  
  material: 'Doubled',
  color: 0xdddddd,
  
};

function initGUI() {
  const h = gui.addFolder("Simulation");
  h.add(effectController, "speed", 0.1, 8.0, 0.05);
  h.add(effectController, "numBlobs", 1, 50, 1);
  h.add(effectController, "resolution", 14, 100, 1);
  h.add(effectController, "isolation", 10, 300, 1);
  
  h.add(effectController, "wallx");
  h.add(effectController, "wally");
  h.add(effectController, "wallz");
  h.open();
  const m = gui.addFolder("Material");
  m.add(effectController, "material", Object.keys(materialFactory)).onChange(() => {
    let mat;
    console.log(effectController.material)
    effect.enableUvs = false;
    switch (effectController.material) {
      case "Liquid":
        mat = new materialFactory[effectController.material](effectController.color, refractionCube);
        break;
      
      case "Chrome" :
      case "Shiny":
        mat = new materialFactory[effectController.material](effectController.color, reflectionCube);
        break;
      
      case "Textured":
        effect.enableUvs = true;
        mat = new materialFactory[effectController.material](effectController.color, texture);
        break;
      
      case "Dotted":
      case "Hatching":
      case "Toon":
        mat = new materialFactory[effectController.material](effectController.color, directLight, ambientLight);
        break;
      
      default:
        mat = new materialFactory[effectController.material](effectController.color);
    }
    effect.material = mat;
    
  });
  m.addColor(effectController, "color").onChange(() => {
    ARCH.setMaterialColor(effect.material, effectController.color);
  });
  
  m.open();
}

function draw() {
  const delta = clock.getDelta();
  
  time += delta * effectController.speed * 0.5;
  
  // marching cubes
  if (effectController.resolution !== resolution) {
    resolution = effectController.resolution;
    effect.init(Math.floor(resolution));
  }
  
  if (effectController.isolation !== effect.isolation) {
    effect.isolation = effectController.isolation;
  }
  
  updateCubes(effect, time, effectController.numBlobs, effectController.wally, effectController.wallx, effectController.wallz);
  
}

// Exporter
function main() {
  loadTextures();
  
  const viewport = new ARCH.Viewport();
  scene = viewport.scene;
  gui = viewport.gui.gui;
  
  initGUI();
  initScene();
  viewport.draw = draw;
  
  const sceneBasic = new ARCH.SceneBasic(scene, viewport.renderer);
  sceneBasic.addGUI(gui);
  sceneBasic.floor.visible = false;
  directLight = sceneBasic.directLight;
  ambientLight = sceneBasic.ambientLight;
  
}

export {
  main
}