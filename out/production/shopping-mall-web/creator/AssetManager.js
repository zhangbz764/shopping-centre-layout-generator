import * as THREE from 'three';

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

const AssetManager = function (_scene) {
  
  let scope = this;
  let max = 0;
  let transformer;
  let _gui = null;
  
  
  this.setCurrentID = function (id) {
    scope.id = id;
    window.layer = scope.id;
    
    refreshSelection(_scene);
  }
  
  scope.setCurrentID(0);
  
  this.addSelectedItem = function (item, id) {
    if (item.layer === undefined) {
      item.layer = [id];
    } else if (!~item.layer.indexOf(id)) {
      item.layer.push(id);
    }
  }
  
  this.addSelection = function (lists, id = 0) {
    id = Number.parseInt(id);
    if (lists.length === undefined) {
      scope.addSelectedItem(lists, id);
    } else {
      lists.forEach((item) => {
        scope.addSelectedItem(item, id);
      });
    }
    
    max = Math.max(max, id);
    
    if (_gui) {
      _gui.__controllers.forEach((item) => {
        if (item.property === 'id') {
          item.__max = max;
          item.updateDisplay();
        }
      });
    }
  }
  
  
  this.setGroup = function () {
    const obj = transformer.object;
    if (obj === undefined) {
      alert("⚠️ no object selected");
      return;
    }
    
    const group = new THREE.Group();
    _scene.add(group);
    transformer.applyTransform(obj);
    
    let layers = new Set();
    while (obj.children.length > 0) {
      obj.children.forEach((item) => {
        group.add(item);
        item.layer.forEach(layers.add, layers);
      });
    }
    transformer.control.detach();
  
    /*
     group layer set from union of layers
    */
    group.layer = Array.from(layers);
    refreshSelection(_scene);
  }
  
  this.unGroup = function () {
    let obj = transformer.object;
    if (obj === undefined || !obj.isGroup) {
      alert("⚠️ no group selected");
      return;
    }
  
    const parent = obj.parent;
    transformer.applyTransform(obj);
  
    while (obj.children.length > 0) {
      obj.children.forEach((item) => {
        parent.add(item);
      });
    }
  
    transformer.control.detach();
    obj.layer = undefined;
    refreshSelection(_scene);
  
  }
  
  this.highlightItem = function (item) {
    if (!item.isMesh) return;
    if (!item.material) return;
    let materials = item.material;
    if (materials.length) {
      for (let j = 0; j < materials.length; ++j) {
        materials[j].emissive.set(0x666600);
      }
    } else {
      materials.emissive.set(0x666600);
    }
  
    if (item.children.length > 0) {
      item.children[0].visible = true;
    }
    if (item.children.length > 1) {
      item.children[1].visible = false;
    }
  }
  
  this.unHighlightItem = function (item) {
    if (!item.isMesh) return;
    if (!item.material) return;
    let materials = item.material;
    if (materials.length) {
      for (let j = 0; j < materials.length; ++j) {
        materials[j].emissive.set(0x000000);
      }
    } else {
      materials.emissive.set(0x000000);
    }
  
    if (item.children.length > 0) {
      item.children[0].visible = false;
    }
    if (item.children.length > 1) {
      item.children[1].visible = true;
    }
  }
  
  this.highlightGroup = function (group) {
    
    group.children.forEach((item) => {
      if (item.isGroup) scope.highlightGroup(item);
      else scope.highlightItem(item);
    })
    
  }
  
  this.unHighlightGroup = function (group) {
    group.children.forEach((item) => {
      if (item.isGroup) scope.unHighlightGroup(item);
      else scope.unHighlightItem(item);
    })
  
  }
  
  this.highlightList = function (list) {
    list.forEach((item) => {
      if (item.isGroup) scope.highlightGroup(item);
      else scope.highlightItem(item);
    });
  }
  
  this.unHighlightList = function (list) {
    list.forEach((item) => {
      if (item.isGroup) scope.unHighlightGroup(item);
      else scope.unHighlightItem(item);
    });
  }
  
  
  this.highlightCurrent = function () {
    
    if (window.highlighted) {
      window.highlighted = false;
      scope.unHighlightList(window.highlightObject);
  
    } else {
      window.highlighted = true;
      scope.highlightList(window.highlightObject);
    }
  }
  
  this.setTransformer = function (controls) {
    transformer = controls;
  }
  
  this.addGUI = function (gui) {
    _gui = gui;
    gui.add(scope, 'setGroup').name('group');
    gui.add(scope, 'unGroup').name('unGroup');
    gui.add(scope, 'highlightCurrent').name('highlight');
    gui.add(scope, 'id', 0, max, 1).name('layer').listen().onChange(function () {
      window.layer = scope.id;
      refreshSelection(_scene);
    });
  }
  
  this.refreshSelection = refreshSelection;
}

function refreshSelection(scene) {
  window.objects = [];
  scene.children.forEach((obj) => {
    if (obj.layer !== undefined && ~obj.layer.indexOf(window.layer)) {
      window.objects.push(obj);
    }
  })
  window.highlightObject = window.objects;
}

export {AssetManager, refreshSelection};