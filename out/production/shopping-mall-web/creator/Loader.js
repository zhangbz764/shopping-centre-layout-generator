import * as THREE from 'three';
// import { DXFLoader } from 'three-dxf-loader';
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader";
import {ColladaLoader} from "three/examples/jsm/loaders/ColladaLoader";
import {DRACOLoader} from "three/examples/jsm/loaders/DRACOLoader";
import {OBJLoader} from "three/examples/jsm/loaders/OBJLoader";
import {Rhino3dmLoader} from "three/examples/jsm/loaders/3DMLoader";
import {ThreeMFLoader} from "three/examples/jsm/loaders/3MFLoader";
import {FBXLoader} from "three/examples/jsm/loaders/FBXLoader";
import {sceneAddMesh, sceneMesh} from "@/creator/GeometryFactory";
import {refreshSelection} from "@/creator/AssetManager";
import {Geometry} from "three/examples/jsm/deprecated/Geometry"
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
 * loader option with store mode
 * |label| |物件可选|材质双面|朝向相机|映射Y至Z|阴影|边线|
 * |:----|:----|:----|:----|:----|:----|:----|:----|
 * | |value|selectable|doubleSide|toCamera|ZtoY|shadow|edge|
 * |成组|grouped|TRUE|TRUE|FALSE|TRUE|TRUE|TRUE|
 * |融合|merged|TRUE|TRUE|TRUE|TRUE|TRUE|TRUE|
 * |原始|raw|TRUE|TRUE|FALSE|FALSE|TRUE|FALSE|
 * @type {{}}
 */
const loaderOption = {
  status: "merged", // ["grouped", "merged", "raw"],
  selectable: true,
  doubleSide: true,
  toCamera: false,
  ZtoY: false,
  shadow: true,
  edge: false
}

const Loader = function (_scene) {
  
  const manager = new THREE.LoadingManager();
  let buffer;
  
  let scope = this;
  
  function loadModel(object) {
    
    /* ---------- raw ---------- */
  
    if (loaderOption.status === "raw") {
    
      // set shadow, doubleSide, layer
      sceneMesh(object, loaderOption.shadow, loaderOption.doubleSide, [0]);
    
      // clean nested group
      while (object.children.length === 1) {
        object = object.children[0];
      }
    
      sceneAddMesh(_scene, object, loaderOption.edge)
      object.toCamera = loaderOption.toCamera;
      return object;
    }
    
    
    /* ---------- merge ---------- */
  
    if (loaderOption.status === "merged") {
      const materials = new Set();
      const meshes = [];
    
    
      searchMaterials(object, materials);
      // console.log(materials)
    
      materials.forEach(function (material) {
        let meshGeometry = new Geometry();
        searchMaterialChild(material, object, meshGeometry);
        meshes.push(new THREE.Mesh(meshGeometry, material));
      });
    
      let lineGeometry = new THREE.BufferGeometry();
      buffer = new Float32Array();
      searchLines(object);
      // console.log(buffer);
      lineGeometry.setAttribute('position', new THREE.BufferAttribute(buffer, 3));
      const result = mergeMeshes(meshes);
      console.log(result)
    
      const line = new THREE.LineSegments(lineGeometry, new THREE.LineBasicMaterial({color: 0x000000}));
      sceneAddMesh(_scene, result, line);
    
      if (checkMaterial(result)) {
        result.material = new THREE.MeshLambertMaterial({
          color: 0x787774,
          side: THREE.DoubleSide,
          shadowSide: THREE.BackSide
        });
      }
    
      result.toCamera = loaderOption.toCamera;
      return result;
    }
  
    if (loaderOption.status === "grouped") {
      // clean nested group
      while (object.children.length === 1) {
        object = object.children[0];
      }
      const result = searchGroupedMesh(object);
    
      sceneMesh(object, loaderOption.shadow, loaderOption.doubleSide)
      sceneAddMesh(_scene, result, loaderOption.edge);
    
      result.toCamera = loaderOption.toCamera;
      return result;
    }
  
  }
  
  function searchGroupMaterials(object, materials) {
    if (!object.isGroup) return;
    object.children.forEach((obj) => {
      if (obj.isMesh) {
        if (obj.material.length > 0) {
          materials.add(obj.material[0]);
        } else {
          materials.add(obj.material);
        }
      }
    })
  }
  
  function searchGroupLines(object) {
    if (!object.isGroup) return;
    object.children.forEach((obj) => {
      if (obj.isLineSegments || obj.isLine) {
        const posArr = obj.geometry.getAttribute('position').array;
        buffer = Float32Concat(buffer, posArr);
      }
    })
  }
  
  function searchGroupMaterialChild(material, object, meshGeometry) {
    if (!object.isGroup) return;
    object.children.forEach((obj) => {
      if (obj.isMesh) {
        let omaterial = obj.material;
        if (omaterial.length > 0) {
          omaterial = omaterial[0]
        }
      
        if (omaterial === material) {
          if (obj.geometry.isBufferGeometry)
            obj.geometry = new Geometry().fromBufferGeometry(obj.geometry);
          meshGeometry.merge(obj.geometry, obj.matrix);
        }
      }
    })
  }
  
  //
  function searchGroupedMesh(object) {
    if (!object.isGroup) return;
    /* ---------- mesh ---------- */
    const materials = new Set();
    const meshes = [];
    searchGroupMaterials(object, materials)
  
    materials.forEach(function (material) {
      let meshGeometry = new Geometry();
      searchGroupMaterialChild(material, object, meshGeometry);
      meshes.push(new THREE.Mesh(meshGeometry, material));
    });
  
    const ret = new THREE.Group().copy(object);
    ret.children = [];
    ret.layer = [0];
    // ret.matrix = object.matrix;
  
    if (materials.size > 0) {
      const result = mergeMeshes(meshes);
    
      /* ---------- line ---------- */
      let lineGeometry = new THREE.BufferGeometry();
      buffer = new Float32Array();
      searchGroupLines(object);
      // console.log(buffer);
      lineGeometry.setAttribute('position', new THREE.BufferAttribute(buffer, 3));
      const line = new THREE.LineSegments(lineGeometry, new THREE.LineBasicMaterial({color: 0x000000}));
    
    
      sceneAddMesh(ret, result, line, loaderOption.shadow, [0]);
      console.log(materials);
      console.log(result);
    }
  
    /* ---------- group ---------- */
    object.children.forEach((obj) => {
      if (obj.isGroup) ret.add(searchGroupedMesh(obj));
    })
    return ret;
  }
  
  function searchMaterials(object, materials) {
    if (object.isMesh) {
      if (object.material.length > 0) {
        materials.add(object.material[0]);
      } else {
        materials.add(object.material);
      }
      return;
    }
    if (object.isGroup) {
      for (let i = 0; i < object.children.length; ++i) {
        searchMaterials(object.children[i], materials);
      }
    }
  }
  
  function mergeMeshes(meshes) {
    if (meshes.length === 1) {
      meshes[0].geometry = meshes[0].geometry.toBufferGeometry();
      return meshes[0];
    }
    
    let materials = [],
      mergedGeometry = new Geometry(),
      mergedMesh;
    
    meshes.forEach(function (mesh, index) {
      mesh.updateMatrix();
      mesh.geometry.faces.forEach(function (face) {
        face.materialIndex = 0;
      });
      mergedGeometry.merge(mesh.geometry, mesh.matrix, index);
      materials.push(mesh.material);
    });
    
    mergedGeometry.groupsNeedUpdate = true;
  
  
    mergedMesh = new THREE.Mesh(mergedGeometry.toBufferGeometry(), materials);
    mergedMesh.geometry.computeFaceNormals();
    mergedMesh.geometry.computeVertexNormals();
    
    return mergedMesh;
    
  }
  
  
  function Float32Concat(first, second) {
    let firstLength = first.length,
      result = new Float32Array(firstLength + second.length);
    
    result.set(first);
    result.set(second, firstLength);
    
    return result;
  }
  
  function searchLines(object, matrix) {
    if (matrix === undefined) matrix = new THREE.Matrix4();
    
    if (object.isLineSegments || object.isLine) {
      
      object.geometry.applyMatrix4(matrix);
      
      const posArr = object.geometry.getAttribute('position').array;
      buffer = Float32Concat(buffer, posArr);
      return;
    }
    
    if (object.isGroup) {
      for (let i = 0; i < object.children.length; ++i) {
        searchLines(object.children[i], object.matrix.premultiply(matrix));
      }
    }
  }
  
  
  function searchMaterialChild(material, object, meshGeometry, matrix) {
    if (matrix === undefined) matrix = new THREE.Matrix4();
    if (object.isMesh) {
      let omaterial = object.material;
      if (omaterial.length > 0) {
        omaterial = omaterial[0];
      }
      if (omaterial === material) {
        object.geometry = new Geometry().fromBufferGeometry(object.geometry);
        object.geometry.applyMatrix4(matrix);
        meshGeometry.merge(object.geometry, object.matrix);
      }
      return;
    }
    
    if (object.isGroup) {
      for (let i = 0; i < object.children.length; ++i) {
        searchMaterialChild(material, object.children[i], meshGeometry, object.matrix.premultiply(matrix));
      }
    }
  }
  
  function isGLTF1(contents) {
    
    var resultContent;
    
    if (typeof contents === 'string') {
      
      // contents is a JSON string
      resultContent = contents;
      
    } else {
      
      var magic = THREE.LoaderUtils.decodeText(new Uint8Array(contents, 0, 4));
      
      if (magic === 'glTF') {
        
        // contents is a .glb file; extract the version
        var version = new DataView(contents).getUint32(4, true);
        
        return version < 2;
        
      } else {
        
        // contents is a .gltf file
        resultContent = THREE.LoaderUtils.decodeText(new Uint8Array(contents));
        
      }
      
    }
    
    var json = JSON.parse(resultContent);
    
    return (json.asset !== undefined && json.asset.version[0] < 2);
    
  }
  
  function checkMaterial(mesh) {
    if (mesh.material.length > 0) {
      mesh.material.forEach((item) => {
        item.side = THREE.DoubleSide;
        item.shadowSide = THREE.BackSide;
      });
      return mesh.material[0].emissive === undefined;
    }
    mesh.material.side = THREE.DoubleSide;
    mesh.material.shadowSide = THREE.BackSide;
    return mesh.material.emissive === undefined;
  }
  
  function onOpen(obj) {
    if (!window.LoaderOption.dialog) {
      // Do something
      console.log(loaderOption);
      if (window.LoaderOption.load) {
        loadModel(obj)
        refreshSelection(_scene);
      }
    } else {
      // Wait and when window.listNodes.length === 3:
      setTimeout(() => onOpen(obj), 500);
    }
  }
  
  /*-------------------- API --------------------*/
  
  this.loadModel = function (filename, callback) {
    let extension = filename.split('.').pop().toLowerCase();
    let loader;
    const dracoLoader = new DRACOLoader();
    switch (extension) {
      case 'dae':
        loader = new ColladaLoader();
        loader.load(filename, function (collada) {
          
          callback(loadModel(collada.scene));
          
        });
        break;
      
      case 'gltf':
      case 'glb':
        loader = new GLTFLoader();
        dracoLoader.setDecoderPath('three/examples/js/libs/draco/gltf/');
        loader.setDRACOLoader(dracoLoader);
        loader.load(filename, function (gltf) {
          callback(loadModel(gltf.scene));
        });
        break;
      case 'obj':
        loader = new OBJLoader();
        loader.load(filename, function (obj) {
          obj.rotateX(Math.PI / 2);
          obj.scale.set(40, 40, 40);
          obj.updateMatrixWorld(true);
          callback(loadModel(obj));
        });
        break;
      case '3mf':
        loader = new ThreeMFLoader();
        loader.load(filename, function (obj) {
          callback(loadModel(obj));
        });
        break;
      case 'fbx':
        loader = new FBXLoader();
        loader.load(filename, function (obj) {
          callback(loadModel(obj));
        })
        break;
      
      // FIXME: not support 3d ?
      // case 'dxf':
      //   loader = new DXFLoader();
      //   loader.load(filename, function(obj) {
      //     console.log(obj)
      //     for(let i = 0; i < obj.entities.length; ++ i) {
      //       let line = obj.entities[i];
      //       line.applyMatrix4(line.modelViewMatrix);
      //       line.updateMatrixWorld(true);
      //       _scene.add(line);
      //     }
      //     // const line = new THREE.LineSegments(lineGeometry, new THREE.LineBasicMaterial({color: 0x000000}));
      //     //
      //     // mesh(line);
      //   });
      //   break;
      default:
        alert('file format not support');
    }
  }
  
  
  this.loadFile = function (file) {
    let filename = file.name;
    let extension = filename.split('.').pop().toLowerCase();
    let reader = new FileReader();
    reader.addEventListener('loadstart', function (event) {
      console.log(event);
      switch (extension) {
        case 'jpeg':
        case 'jpg':
        case 'png':
          break;
        default:
          window.LoaderOption.dialog = true;
      }
    })
  
    reader.addEventListener('progress', function (event) {
      
      let size = '(' + Math.floor(event.total / 1000) + ' KB)';
      let progress = Math.floor((event.loaded / event.total) * 100) + '%';
      
      console.log('Loading', filename, size, progress);
      
    });
    
    const dracoLoader = new DRACOLoader();
    switch (extension) {
      case 'dae':
        reader.addEventListener('load', function (event) {
          
          let contents = event.target.result;
          
          let loader = new ColladaLoader(manager);
          let collada = loader.parse(contents);
  
  
          onOpen(collada.scene);
          
        }, false);
        reader.readAsText(file);
        break;
      
      case 'glb':
        // FIXME: SyntaxError: Unexpected token g in JSON at position 0
        reader.addEventListener('load', function (event) {
          
          let contents = event.target.result;
          let loader = new GLTFLoader();
          dracoLoader.setDecoderPath('three/examples/js/libs/draco/gltf/');
          loader.setDRACOLoader(dracoLoader);
          let gltf = loader.parse(contents);
          
          onOpen(gltf.scene);
        }, false);
        reader.readAsText(file);
        break;
      
      case 'gltf':
        reader.addEventListener('load', function (event) {
          
          let contents = event.target.result;
          let loader = new GLTFLoader();
          if (isGLTF1(contents)) {
            
            alert('Import of glTF asset not possible. Only versions >= 2.0 are supported. Please try to upgrade the file to glTF 2.0 using glTF-Pipeline.');
            
          } else {
            dracoLoader.setDecoderPath('three/examples/js/libs/draco/gltf/');
            loader.setDRACOLoader(dracoLoader);
            loader.parse(contents, '', function (gltf) {
              
              onOpen(gltf.scene);
            });
            
          }
        }, false);
        reader.readAsText(file);
        break;
      case 'obj':
        reader.addEventListener('load', function (event) {
          let contents = event.target.result;
          let obj = new OBJLoader().parse(contents);
          obj.rotateX(Math.PI / 2);
          // obj.scale.set(40, 40, 40);
          obj.updateMatrixWorld(true);
          
          
          onOpen(obj);
        }, false);
        reader.readAsText(file);
        break;
      case '3dm':
        
        reader.addEventListener('load', function (event) {
          
          let contents = event.target.result;
          
          let loader = new Rhino3dmLoader();
          loader.setLibraryPath('three/examples/jsm/libs/rhino3dm/');
          loader.parse(contents, function (object) {
            
            onOpen(object);
          });
          
        }, false);
        reader.readAsText(file);
        
        break;
      case '3mf':
        reader.addEventListener('load', function (event) {
          
          let loader = new ThreeMFLoader();
          let object = loader.parse(event.target.result);
          
          onOpen(object);
          
        }, false);
        reader.readAsArrayBuffer(file);
        
        break;
      case 'fbx':
        reader.addEventListener('load', function (event) {
          
          let contents = event.target.result;
          
          let loader = new FBXLoader(manager);
          let object = loader.parse(contents);
          
          onOpen(object);
        }, false);
        reader.readAsArrayBuffer(file);
        
        break;
  
      case 'jpeg':
      case 'png':
      case 'jpg':
        reader.addEventListener('load', function (event) {
          let contents = event.target.result;
          let loader = new THREE.ImageLoader();
          loader.load(contents, function (image) {
      
            scope.dispatchEvent({type: 'load', object: image});
            // alert(''+image.width + ' ' + image.height);
          });
        });
  
        reader.readAsDataURL(file);
        break;
  
      // case 'ifc':
      //   reader.addEventListener('load', function (event) {
      //     let contents = event.target.result;
      //     let loader = new IFCLoader();
      //   });
      //   reader.readAsArrayBuffer(file);
      //   break;
      //
      default:
        alert('file format ' + extension + ' not support');
      
      
    }
  }
  
  
  this.addGUI = function (gui) {
    this.import = function () {
      fileInput.click();
    }
    
    gui.add(this, 'import');
    
    let form = document.createElement('form');
    form.style.display = 'none';
    document.body.appendChild(form);
    
    let fileInput = document.createElement('input');
    fileInput.multiple = true;
    fileInput.type = 'file';
    fileInput.addEventListener('change', function () {
  
      scope.loadFile(fileInput.files[0]);
      form.reset();
      
    });
    form.appendChild(fileInput);
  }
}


Loader.prototype = Object.create(THREE.EventDispatcher.prototype);

export {Loader, loaderOption};