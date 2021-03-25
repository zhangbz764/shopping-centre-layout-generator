/* eslint-disable no-unused-vars,no-case-declarations */
import * as THREE from 'three'

/**
 *      ___           ___           ___           ___                       ___           ___           ___
 *     /\  \         /\  \         /\  \         /\__\          ___        /\__\         /\  \         /\  \
 *    /::\  \       /::\  \       /::\  \       /:/  /         /\  \      /:/ _/_       /::\  \       /::\  \
 *   /:/\:\  \     /:/\:\  \     /:/\:\  \     /:/__/          \:\  \    /:/ /\__\     /:/\:\  \     /:/\:\  \
 *  /::\~\:\  \   /::\~\:\  \   /:/  \:\  \   /::\  \ ___      /::\__\  /:/ /:/ _/_   /::\~\:\  \   /::\~\:\__\
 * /:/\:\ \:\__\ /:/\:\ \:\__\ /:/__/ \:\__\ /:/\:\  /\__\  __/:/\/__/ /:/_/:/ /\__\ /:/\:\ \:\__\ /:/\:\ \:|__|
 * \/__\:\/:/  / \/_|::\/:/  / \:\  \  \/__/ \/__\:\/:/  / /\/:/  /    \:\/:/ /:/  / \:\~\:\ \/__/ \:\~\:\/:/  /
 *      \::/  /     |:|::/  /   \:\  \            \::/  /  \::/__/      \::/_/:/  /   \:\ \:\__\    \:\ \::/  /
 *      /:/  /      |:|\/__/     \:\  \           /:/  /    \:\__\       \:\/:/  /     \:\ \/__/     \:\/:/  /
 *     /:/  /       |:|  |        \:\__\         /:/  /      \/__/        \::/  /       \:\__\        \::/__/
 *     \/__/         \|__|         \/__/         \/__/                     \/__/         \/__/         ~~
 *
 *
 *
 * Copyright (c) 2020-present, Inst.AAA.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 *
 * Date: 2020-11-12
 * Author: Yichen Mo
 */

const MultiCamera = function (domElement) {
  this.width = window.innerWidth;
  this.height = window.innerHeight;
  
  let cameraPersp, cameraOrtho;
  let scope = this;
  
  let _controller;
  let _transformer;
  let _dragFrames;
  
  
  /* ---------- Init Camera ---------- */
  function init() {
    
    initPerspectiveCamera(scope.width, scope.height);
    initOrthographicCamera(scope.width, scope.height);
    
    scope.camera = scope.isometric ? cameraOrtho : cameraPersp;
    domElement.addEventListener('keydown', onDocumentKeyDown, false);
  }
  
  function initOrthographicCamera(width, height) {
    cameraOrtho = new THREE.OrthographicCamera(width / -2, width / 2,
      height / 2, height / -2, scope.near, scope.far);
    cameraOrtho.position.set(scope.x, scope.y, scope.z);
    cameraOrtho.up = new THREE.Vector3(0, 0, 1);
  }
  
  function initPerspectiveCamera(width, height) {
    cameraPersp = new THREE.PerspectiveCamera(scope.fov, width / height, scope.near, scope.far);
    cameraPersp.position.set(scope.x, scope.y, scope.z);
    cameraPersp.up = new THREE.Vector3(0, 0, 1);
  }
  
  function togglePerspective() {
    const position = scope.camera.position.clone();
    cameraPersp.position.copy(position);
  
    scope.camera = cameraPersp;
    scope.isometric = false;
  
    _controller.object = cameraPersp;
  
    if (_dragFrames) _dragFrames.activate();
    if (_transformer) _transformer.control.camera = cameraPersp;
  }
  
  function toggleOrthographic() {
    const position = scope.camera.position.clone();
    cameraOrtho.position.copy(position);
  
    scope.camera = cameraOrtho;
    scope.isometric = true;
  
    _controller.object = cameraOrtho;
  
    // dragFrames enabled not work because of transformer enable it, so have to deactivate directly
    if (_dragFrames) _dragFrames.deactivate();
    if (_transformer) _transformer.control.camera = cameraOrtho;
  }
  
  
  /* ---------- zoom to objects ---------- */
  function getBoundingBox(objects) {
    const box = new THREE.Box3();
    if (objects instanceof Array) {
      for (const object of objects)
        box.expandByObject(object);
    } else {
      box.setFromObject(objects);
    }
    return box;
  }
  
  function zoomToObjects(objects, offset = 2) {
    
    const boundingBox = getBoundingBox(objects);
    
    const center = boundingBox.getCenter(new THREE.Vector3());
    const size = boundingBox.getSize(new THREE.Vector3());
    
    
    const maxSize = Math.max(size.x, size.y, size.z);
    const fitHeightDistance = maxSize / (2 * Math.atan(Math.PI * cameraPersp.fov / 360));
    const fitWidthDistance = fitHeightDistance / cameraPersp.aspect;
    const distance = offset * Math.max(fitHeightDistance, fitWidthDistance);
    
    
    const direction = _controller.target.clone()
      .sub(scope.camera.position)
      .normalize()
      .multiplyScalar(distance);
    
    
    cameraPersp.maxDistance = distance * 10;
    scope.camera.lookAt(center);
    
    scope.camera.near = distance / 100;
    scope.camera.far = distance * 100;
    scope.camera.updateProjectionMatrix();
    
    scope.camera.position.copy(_controller.target).sub(direction);
    
    _controller.target = center;
    _controller.update();
    
  }
  
  
  /* ---------- view position ---------- */
  
  
  function viewFrontLeft() {
    const position = _controller.target.clone();
    position.x -= 1200;
    position.y -= 1200;
    position.z += 1200;
    scope.camera.position.copy(position);
    
  }
  
  function viewFront() {
    const position = _controller.target.clone();
    position.y = -2000;
    scope.camera.position.copy(position);
  
  }
  
  function viewFrontRight() {
    const position = _controller.target.clone();
    position.x += 1200;
    position.y -= 1200;
    position.z += 1200;
    scope.camera.position.copy(position);
  
  }
  
  function viewLeft() {
    const position = _controller.target.clone();
    position.x = -2000;
    scope.camera.position.copy(position);
  }
  
  function viewUp() {
    const position = _controller.target.clone();
    position.z = 2000;
    scope.camera.position.copy(position);
  }
  
  function viewRight() {
    const position = _controller.target.clone();
    position.x = 2000;
    scope.camera.position.copy(position);
    
  }
  
  function viewBackLeft() {
    
    const position = _controller.target.clone();
    position.x -= 1200;
    position.y += 1200;
    position.z += 1200;
    scope.camera.position.copy(position);
  }
  
  function viewBack() {
    const position = _controller.target.clone();
    position.y = 2000;
    scope.camera.position.copy(position);
  }
  
  function viewBackRight() {
    const position = _controller.target.clone();
    position.x += 1200;
    position.y += 1200;
    position.z += 1200;
    scope.camera.position.copy(position);
    scope.camera.updateProjectionMatrix();
  
  }
  
  function onDocumentKeyDown(event) {
    switch (event.keyCode) {
      
      case 67: // C
        if (scope.camera.isPerspectiveCamera) toggleOrthographic();
        else togglePerspective();
        break;
  
      case 97:
      case 49: // 1
        viewFrontLeft();
        scope.view = 1;
        break;
      
      case 98:
      case 50: // 2
        viewFront();
        scope.view = 2;
        break;
      
      case 99:
      case 51: // 3
        viewFrontRight();
        scope.view = 3;
        break;
      
      case 100:
      case 52: // 4
        viewLeft();
        scope.view = 4;
        break;
      
      case 101:
      case 53: // 5
        viewUp();
        scope.view = 5;
        break;
      
      case 102:
      case 54: // 6
        viewRight();
        scope.view = 6;
        break;
      
      case 103:
      case 55: // 7
        viewBackLeft();
        scope.view = 7;
        break;
      
      case 104:
      case 56: // 8
        viewBack();
        scope.view = 8;
        break;
      
      case 105:
      case 57: // 9
        viewBackRight();
        scope.view = 9;
        break;
      
    }
    
  }
  
  /* ---------- APIs ---------- */
  this.isometric = false;
  this.fov = 45;
  
  /* ---------- detail parameters ---------- */
  this.near = 1;
  this.far = 10000;
  this.x = 1000;
  this.y = -1500;
  this.z = 1000;
  
  this.view = 0;
  this.camera = null;
  
  init();
  
  /* ---------- after init ---------- */
  this.toggleOrthographic = toggleOrthographic;
  this.togglePerspective = togglePerspective;
  this.top = viewUp;
  
  this.onWindowResize = function (w, h) {
    cameraPersp.aspect = w / h;
    cameraPersp.updateProjectionMatrix();
    
    
    cameraOrtho.left = cameraOrtho.bottom * w / h;
    cameraOrtho.right = cameraOrtho.top * w / h;
    cameraOrtho.updateProjectionMatrix();
    
  };
  
  this.setDrag = function (drag) {
    _dragFrames = drag;
  }
  
  this.setController = function (controller) {
    _controller = controller;
  }
  
  this.setTransformer = function (transformer) {
    _transformer = transformer;
  }
  
  this.zoomAll = function () {
    if (window.highlightObject.length === 0) {
      alert("⚠️ No object selected, use ARCH.refreshSelection(scene) or enable AssetManager in viewport.")
      return;
    }
    zoomToObjects(window.highlightObject);
  }
  
  this.addGUI = function (gui) {
    const camera = gui.addFolder("Camera");
    camera.add(scope, 'zoomAll').name('zoom');
    camera.add(scope, 'isometric')
      .listen().onChange(function () {
      if (scope.isometric) toggleOrthographic();
      else togglePerspective();
    });
    
    camera.add(scope, 'view', 0, 9, 1)
      .listen().onChange(function () {
      switch (scope.view) {
        case 1:
          viewFrontLeft();
          break;
        case 2:
          viewFront();
          break;
        case 3:
          viewFrontRight();
          break;
        case 4:
          viewLeft();
          break;
        case 5:
          viewUp();
          break;
        case 6:
          viewRight();
          break;
        case 7:
          viewBackLeft();
          break;
        case 8:
          viewBack();
          break;
        case 9:
          viewBackRight();
          break;
      }
    });
  
    const detail = camera.addFolder("Detail")
    
    detail.add(scope, 'fov', 0, 150, 1)
      .listen().onChange(function () {
      cameraPersp.fov = scope.fov;
      
      cameraOrtho.top = scope.fov * 600 / 45;
      cameraOrtho.bottom = -scope.fov * 600 / 45;
  
      scope.camera.updateProjectionMatrix();
    });
  
    detail.add(scope, 'near', 0.1, 100)
      .listen().onChange(function () {
      cameraOrtho.near = scope.near;
      cameraPersp.near = scope.near;
    
      scope.camera.updateProjectionMatrix();
    })
  
    detail.add(scope, 'far', 1000, 100000)
      .listen().onChange(function () {
      cameraOrtho.far = scope.far;
      cameraPersp.far = scope.far;
    
      scope.camera.updateProjectionMatrix();
    })
  }
  
};

export {MultiCamera};
