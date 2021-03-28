import * as THREE from 'three';
import * as gui from '@/gui'
import {OrbitControls} from "three/examples/jsm/controls/OrbitControls";

import {AssetManager, DragFrames, MultiCamera, Transformer} from "@/archiweb";

const Viewport = function () {
  
  const renderer = new THREE.WebGLRenderer({antialias: true, alpha: true, preserveDrawingBuffer: true});
  const scene = new THREE.Scene();
  
  const camera = new MultiCamera(renderer.domElement);
  const controller = new OrbitControls(camera.camera, renderer.domElement);
  
  let drag;
  let transformer;
  let assetManager;
  
  let scope = this;
  
  function init() {
    window.layer = 0;
    window.objects = [];
    window.searchSceneByUUID = function (uuid) {
      return scene.getObjectByProperty('uuid', uuid);
    }
    /* ---------- renderer ---------- */
    renderer.domElement.tabIndex = 0;
    renderer.autoClear = false;
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(window.innerWidth, window.innerHeight);
  
    /* ---------- dom ---------- */
    addToDOM();
  
    /* ---------- gui ---------- */
    gui.initGUI();
    addGUI(gui.gui);
  
    /* ---------- camera ---------- */
    camera.addGUI(gui.gui);
    camera.setController(controller);
  
    /* ---------- control ---------- */
    controller.enableKeys = false;
    controller.mouseButtons = {
      LEFT: THREE.MOUSE.PAN,
      RIGHT: THREE.MOUSE.ROTATE
    }
    controller.update();
    animate();
  }
  
  function onSelectDown(event) {
    window.highlighted = true;
    assetManager.highlightList(event.object);
  }
  
  function onSelectUp(event) {
    window.highlighted = false;
    if (event.object.length > 1000) {
      assetManager.unHighlightList(event.object);
      alert('too much selected');
    } else {
      assetManager.unHighlightList(event.object);
      transformer.setSelected(event.object);
    }
  }
  
  /**
   * Enable group/ungroup and highlight/unhighlight object with AssetManager
   * @returns {AssetManager}
   */
  function enableAssetManager() {
    assetManager = new AssetManager(scene);
    assetManager.addGUI(gui.util);
  
    return assetManager;
  }
  
  /**
   * Enable multiple select with DragFrame control
   * Highlight object during selection : {@link onSelectDown}
   * Unhighlight object and push to transformer ( if exist ) after selection : {@link onSelectUp}
   * @returns {DragFrames}
   */
  function enableDragFrames() {
    if (assetManager === undefined) enableAssetManager();
  
    drag = new DragFrames(renderer, scene, camera.camera);
  
    drag.addEventListener('selectdown', () => {
      transformer.clear()
    });
    drag.addEventListener('select', onSelectDown);
    drag.addEventListener('selectup', onSelectUp);
  
    camera.setDrag(drag);
  
    return drag;
  }
  
  /**
   * Transform tool derive from THREE.TransformControl, just like your familiar Rhino Gumball.
   * be careful to enable while it rewrite click event
   * @returns {Transformer}
   */
  function enableTransformer() {
    if (assetManager === undefined) enableAssetManager();
    
    transformer = new Transformer(scene, renderer, camera.camera);
    camera.setTransformer(transformer);
    
    transformer.addGUI(gui.gui);
    transformer._dragFrames = drag;
    transformer._assetManager = assetManager;
    assetManager.setTransformer(transformer);
    
    return transformer;
  }
  
  function animate() {
    controller.update();
    requestAnimationFrame(animate);
    render();
  }
  
  function addToDOM() {
    const container = document.getElementById('container');
    const canvas = container.getElementsByTagName('canvas');
    if (canvas.length > 0) {
      container.removeChild(canvas[0]);
    }
    container.appendChild(renderer.domElement);
    
    window.onresize = function () {
      windowResize(window.innerWidth, window.innerHeight);
    };
    renderer.domElement.addEventListener('keydown', onDocumentKeyDown, false);
    renderer.domElement.addEventListener('keyup', onDocumentKeyUp, false);
  }
  
  function windowResize(w, h) {
  
    if (drag) drag.onWindowResize(w, h);
    camera.onWindowResize(w, h);
    renderer.setSize(w, h);
    render();
  }
  
  function onDocumentKeyDown(event) {
    // console.log('viewport key down');
    switch (event.keyCode) {
      case 16: // Shift
        controller.enablePan = true;
        break;
      case 73: // I
        window.InfoCard.hideInfoCard(!window.InfoCard.show);
  
    }
  }
  
  function onDocumentKeyUp(event) {
    switch (event.keyCode) {
      case 16: // Shift
        controller.enablePan = false;
        break;
    }
  }
  
  
  function render() {
    
    scene.traverse((obj) => {
      if (obj.toCamera) {
        let v = new THREE.Vector3().subVectors(camera.camera.position, obj.position);
        let theta = -Math.atan2(v.x, v.y);
  
        obj.quaternion.set(0, 0, 0, 1);
        obj.rotateZ(theta);
      }
    });
  
    renderer.clear();
    renderer.render(scene, camera.camera);
  
    if (drag) drag.render();
    if (scope.draw) scope.draw();
  }
  
  /**
   * Enable 2D, pan with right mouse
   * @returns camera.camera
   */
  function to2D() {
    camera.top();
    camera.toggleOrthographic();
    controller.mouseButtons = {
      LEFT: THREE.MOUSE.ROTATE,
      RIGHT: THREE.MOUSE.PAN
    }
    controller.enablePan = true;
    controls.pan = true;
    
    controller.enableRotate = false;
    controls.rotate = false;
  
    return camera.camera;
  }
  
  /**
   * Enable 3D, rotate with right mouse, pan with shift + right mouse
   * @returns camera.camera
   */
  function to3D() {
    controller.mouseButtons = {
      LEFT: THREE.MOUSE.PAN,
      RIGHT: THREE.MOUSE.ROTATE
    }
    controller.enablePan = false;
    controls.pan = false;
    
    return camera.camera;
  }
  
  /**
   * Viewport controls, toggle rotate, pan and zoom
   * Working with {@link addGUI}
   */
  const controls = new function () {
    this.rotate = true;
    this.pan = false;
    this.zoom = true;
  }
  /* ---------- APIs ---------- */
  this.renderer = renderer;
  this.scene = scene;
  this.gui = gui;
  this.controller = controller;
  this.camera = to3D();
  this.draw = undefined;
  
  this.enableDragFrames = enableDragFrames;
  this.enableTransformer = enableTransformer;
  this.enableAssetManager = enableAssetManager;
  
  this.to2D = to2D;
  this.to3D = to3D;
  
  /* ---------- GUI ---------- */
  
  function addGUI(gui) {
    let viewport = gui.addFolder('Viewport');
    viewport.add(controls, 'rotate').listen().onChange(() => {
      controller.enableRotate = !controller.enableRotate;
    });
    viewport.add(controls, 'pan').listen().onChange(() => {
      controller.enablePan = !controller.enablePan;
    });
    viewport.add(controls, 'zoom').listen().onChange(() => {
      controller.enableZoom = !controller.enableZoom;
    });
  }
  
  init();
};

export {Viewport};