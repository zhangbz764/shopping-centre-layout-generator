import * as ARCH from "@/archiweb";
import * as THREE from "three";

let scene, gui;
let directLight, ambientLight;
const li = [];
const materialFactory = new ARCH.MaterialFactory();

const effectController = {
  material: 'Doubled',
}

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

function initGUI() {
  gui.add(effectController, 'material', Object.keys(materialFactory)).onChange(() => {
    scene.traverse((obj) => {
      let mat;
      if (obj.isMesh) {
        switch (effectController.material) {
          case "Liquid":
            mat = new materialFactory[effectController.material](effectController.color, refractionCube);
            break;
          
          case "Chrome" :
          case "Shiny":
            mat = new materialFactory[effectController.material](effectController.color, reflectionCube);
            break;
          
          case "Textured":
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
      }
      li.forEach((mesh) => {
        mesh.material = mat;
      });
    });
  });
}

function initScene() {
  
  const material = materialFactory.Doubled();
  
  const gf = new ARCH.GeometryFactory(scene);
  li.push(gf.Cuboid([900, 150, 0], [300, 300, 300], material));
  li.push(gf.Cuboid([1000, 0, 0], [300, 300, 100], material));
  
  const b = new THREE.BoxBufferGeometry(1, 1, 1);
  b.translate(0, 0, 0.5);
  const b3 = new THREE.Mesh(b, material);
  b3.scale.set(300, 300, 300);
  scene.add(b3);
  
  li.push(b3);
}

function main() {
  loadTextures();
  
  const viewport = new ARCH.Viewport();
  scene = viewport.scene;
  gui = viewport.gui.gui;
  
  const sceneBasic = new ARCH.SceneBasic(scene, viewport.renderer);
  sceneBasic.addGUI(gui);
  initScene();
  initGUI();
}

export {
  main
}