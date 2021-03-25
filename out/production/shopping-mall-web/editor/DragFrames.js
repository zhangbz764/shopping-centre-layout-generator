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

/**
 *
 * @param _camera
 * @param _scene
 * @param _renderer
 * @constructor
 */
const DragFrames = function (_renderer, _scene, _camera) {
  
  let _domElement = _renderer.domElement;
  let _dragInitX, _dragInitY;
  let _startPoint = new THREE.Vector3();
  let _endPoint = new THREE.Vector3();
  
  let _lineFrame, _geometry = null;
  let _selectDown = false;
  
  let _frustum = new THREE.Frustum();
  let _selected = [];
  
  let scene2D, camera2D;
  
  let scope = this;
  
  function minMax(a, b) {
    return [Math.min(a, b), Math.max(a, b)];
  }
  
  
  function drawLineFrame(initX, initY, X, Y) {
    scene2D.remove(_lineFrame);
    
    let material = (initX > X) ?
      new THREE.LineDashedMaterial({color: 0x000000, dashSize: 5, gapSize: 3}) :
      new THREE.LineBasicMaterial({color: 0x000000});
    
    let [l, r] = minMax(initX, X);
    let [b, t] = minMax(initY, Y);
    
    const points = [];
    points.push(new THREE.Vector3(l, b, 0));
    points.push(new THREE.Vector3(r, b, 0));
    points.push(new THREE.Vector3(r, t, 0));
    points.push(new THREE.Vector3(l, t, 0));
    points.push(new THREE.Vector3(l, b, 0));
    
    if (_geometry != null) _geometry.dispose();
    _geometry = new THREE.BufferGeometry().setFromPoints(points);
    _lineFrame = new THREE.Line(_geometry, material);
    _lineFrame.computeLineDistances();
    scene2D.add(_lineFrame);
  }
  
  
  function init() {
    
    let width = window.innerWidth;
    let height = window.innerHeight;
    
    scene2D = new THREE.Scene();
    
    camera2D = new THREE.OrthographicCamera(-width / 2, width / 2, -height / 2, height / 2, 1, 10);
    camera2D.position.x = width / 2;
    camera2D.position.y = height / 2;
    camera2D.position.z = 10;
    
    activate();
    
  }
  
  function activate() {
    _domElement.addEventListener('pointerdown', onDocumentPointerDown, false);
    _domElement.addEventListener('pointermove', onDocumentPointerMove, false);
    _domElement.addEventListener('pointerup', onDocumentPointerUp, false);
  }
  
  
  function deactivate() {
    _domElement.removeEventListener('pointerdown', onDocumentPointerDown, false);
    _domElement.removeEventListener('pointermove', onDocumentPointerMove, false);
    _domElement.removeEventListener('pointerup', onDocumentPointerUp, false);
  }
  
  function dispose() {
    deactivate();
  }
  
  function unSelected() {
    for (let i = 0; i < _selected.length; ++i) {
      _selected[i].material.emissive.set(0x000000);
      if (_selected[i].children.length > 0) {
        _selected[i].children[0].visible = false;
      }
    }
    _selected = [];
  }
  
  function onWindowResize(width, height) {
    camera2D.left = -width / 2;
    camera2D.right = width / 2;
    camera2D.top = -height / 2;
    camera2D.bottom = height / 2;
    camera2D.position.x = width / 2;
    camera2D.position.y = height / 2;
    camera2D.updateProjectionMatrix();
  }
  
  
  function onDocumentPointerDown(event) {
    
    if (scope.enabled && event.button === 0) {
      _startPoint.set(
        (event.clientX / window.innerWidth) * 2 - 1,
        -(event.clientY / window.innerHeight) * 2 + 1,
        0.5);
      
      _dragInitX = event.clientX;
      _dragInitY = event.clientY;
      
      _selectDown = true;
      scope.dispatchEvent({type: 'selectdown', object: _selected});
      
    }
  }
  
  
  function onDocumentPointerMove(event) {
    if (_selectDown && scope.enabled) {
      
      _endPoint.set(
        (event.clientX / window.innerWidth) * 2 - 1,
        -(event.clientY / window.innerHeight) * 2 + 1,
        0.5);
      
      _selected = select();
      
      scope.dispatchEvent({type: 'select', object: _selected});
      drawLineFrame(_dragInitX, _dragInitY, event.clientX, event.clientY);
      
    }
  }
  
  
  function onDocumentPointerUp(event) {
    scene2D.remove(_lineFrame);
    _selectDown = false;
    
    if (scope.enabled) {
      _endPoint.set(
        (event.clientX / window.innerWidth) * 2 - 1,
        -(event.clientY / window.innerHeight) * 2 + 1,
        0.5);
      
      _selected = select();
      
      scope.dispatchEvent({type: 'selectup', object: _selected});
    }
    
  }
  
  
  function select(startPoint, endPoint) {
    _startPoint = startPoint || _startPoint;
    _endPoint = endPoint || _endPoint;
    
    _selected = [];
    
    updateFrustum(_startPoint, _endPoint);
    searchChildInFrustum(_frustum, window.objects);
    
    return _selected;
  }
  
  
  function updateFrustum(startPoint, endPoint) {
    startPoint = startPoint || _startPoint;
    endPoint = endPoint || _endPoint;
    
    // Avoid invalid frustum
    if (startPoint.x === endPoint.x) {
      endPoint.x += Number.EPSILON;
    }
    
    if (startPoint.y === endPoint.y) {
      endPoint.y += Number.EPSILON;
    }
    
    scope.object.updateProjectionMatrix();
    scope.object.updateMatrixWorld();
  
    if (!scope.object.isPerspectiveCamera) {
      console.error('THREE.SelectionBox: Unsupported camera type.');
      return;
    }
  
    const tmpPoint = new THREE.Vector3();
    
    const vecNear = new THREE.Vector3();
    const vecTopLeft = new THREE.Vector3();
    const vecTopRight = new THREE.Vector3();
    const vecDownRight = new THREE.Vector3();
    const vecDownLeft = new THREE.Vector3();
    
    const vectemp1 = new THREE.Vector3();
    const vectemp2 = new THREE.Vector3();
    const vectemp3 = new THREE.Vector3();
    
    tmpPoint.copy(startPoint);
    tmpPoint.x = Math.min(startPoint.x, endPoint.x);
    tmpPoint.y = Math.max(startPoint.y, endPoint.y);
    endPoint.x = Math.max(startPoint.x, endPoint.x);
    endPoint.y = Math.min(startPoint.y, endPoint.y);
    
    vecNear.setFromMatrixPosition(scope.object.matrixWorld);
    vecTopLeft.copy(tmpPoint);
    vecTopRight.set(endPoint.x, tmpPoint.y, 0);
    vecDownRight.copy(endPoint);
    vecDownLeft.set(tmpPoint.x, endPoint.y, 0);
    
    vecTopLeft.unproject(scope.object);
    vecTopRight.unproject(scope.object);
    vecDownRight.unproject(scope.object);
    vecDownLeft.unproject(scope.object);
    
    vectemp1.copy(vecTopLeft).sub(vecNear);
    vectemp2.copy(vecTopRight).sub(vecNear);
    vectemp3.copy(vecDownRight).sub(vecNear);
    vectemp1.normalize();
    vectemp2.normalize();
    vectemp3.normalize();
    
    vectemp1.multiplyScalar(Number.MAX_VALUE);
    vectemp2.multiplyScalar(Number.MAX_VALUE);
    vectemp3.multiplyScalar(Number.MAX_VALUE);
    vectemp1.add(vecNear);
    vectemp2.add(vecNear);
    vectemp3.add(vecNear);
    
    const planes = _frustum.planes;
    
    planes[0].setFromCoplanarPoints(vecNear, vecTopLeft, vecTopRight);
    planes[1].setFromCoplanarPoints(vecNear, vecTopRight, vecDownRight);
    planes[2].setFromCoplanarPoints(vecDownRight, vecDownLeft, vecNear);
    planes[3].setFromCoplanarPoints(vecDownLeft, vecTopLeft, vecNear);
    planes[4].setFromCoplanarPoints(vecTopRight, vecDownRight, vecDownLeft);
    planes[5].setFromCoplanarPoints(vectemp3, vectemp2, vectemp1);
    planes[5].normal.multiplyScalar(-1);
  }
  
  function searchChildInFrustum(frustum, object) {
    if (object.isMesh || object.isLine || object.isPoints) {
    
      if (object.material !== undefined) {
      
        if (object.geometry.boundingSphere === null) object.geometry.computeBoundingSphere();
      
        const center = new THREE.Vector3();
      
        center.copy(object.geometry.boundingSphere.center);
        center.applyMatrix4(object.matrixWorld);
        
        if (frustum.containsPoint(center)) {
          _selected.push(object);
        }
        
      }
      
    }
  
    if (object.isGroup) {
      const box3 = new THREE.Box3().setFromObject(object);
      const center = new THREE.Vector3();
    
      box3.getCenter(center);
      center.applyMatrix4(object.matrixWorld);
    
      if (frustum.containsPoint(center)) {
        _selected.push(object);
      }
    }
  
  
    for (let x = 0; x < object.length; x++) {
      searchChildInFrustum(frustum, object[x]);
    }
  }
  
  function render() {
    _renderer.clearDepth();
    _renderer.render(scene2D, camera2D);
  }
  
  init();
  
  this.selected = _selected;
  
  this.enabled = true;
  
  this.object = _camera;
  this.activate = activate;
  this.deactivate = deactivate;
  this.dispose = dispose;
  this.clear = unSelected;
  this.render = render;
  this.onWindowResize = onWindowResize;
};

DragFrames.prototype = Object.create(THREE.EventDispatcher.prototype);

export {DragFrames}