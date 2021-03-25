import * as THREE from 'three'
import {BufferGeometry, Float32BufferAttribute, ShapeUtils} from 'three'
import {LineMaterial} from "three/examples/jsm/lines/LineMaterial";
import {WireframeGeometry2} from "three/examples/jsm/lines/WireframeGeometry2";
import {Wireframe} from "three/examples/jsm/lines/Wireframe";
import * as earcut from "earcut";


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
import {setMaterialDoubleSide, setPolygonOffsetMaterial} from "@/creator/MaterialFactory";

const GeometryFactory = function (_scene) {
  
  
  // Box Basic
  const boxGeometry = new THREE.BoxBufferGeometry(1, 1, 1);
  boxGeometry.translate(0, 0, 0.5);
  
  // Cylinder Basic
  const cylinderGeometry = new THREE.CylinderBufferGeometry(1, 1, 1, 32)
  cylinderGeometry.rotateX(Math.PI / 2);
  cylinderGeometry.translate(0, 0, 0.5);
  
  const planeGeometry = new THREE.PlaneBufferGeometry(1, 1);
  const sphereGeometry = new THREE.SphereGeometry(1, 16, 16);
  
  // const scope = this;
  // API
  this.Plane = function ([x, y, z] = [0, 0, 0], [w, d] = [1, 1], material, showEdge = true) {
    let mesh = new THREE.Mesh(planeGeometry, material);
    sceneAddMesh(_scene, mesh, showEdge);
    
    mesh.type = 'Plane';
    mesh.scale.set(w, d, 1);
    mesh.position.set(x, y, z);
    
    publicProperties(mesh);
    return mesh;
  }
  
  this.Cuboid = function ([x, y, z] = [0, 0, 0], [w, d, h] = [1, 1, 1], material, showEdge = true) {
    if (!material) material = new THREE.MeshPhongMaterial({color: 0xdddddd, specular: 0x111111, shininess: 1});
    let mesh = new THREE.Mesh(boxGeometry, material);
    sceneAddMesh(_scene, mesh, showEdge)
    
    mesh.type = 'Cuboid';
    mesh.scale.set(w, d, h);
    mesh.position.set(x, y, z);
    
    publicProperties(mesh);
    
    return mesh;
  }
  
  this.Sphere = function ([x, y, z] = [0, 0, 0], r = 1, material) {
    if (!material) material = new THREE.MeshPhongMaterial({color: 0xdddddd, specular: 0x111111, shininess: 1});
    let mesh = new THREE.Mesh(sphereGeometry, material);
    sceneAddMesh(_scene, mesh, false);
    
    mesh.type = 'Sphere';
    mesh.scale.set(r, r, r);
    mesh.position.set(x, y, z);
    
    publicProperties(mesh);
    return mesh;
  }
  
  this.Cylinder = function ([x, y, z] = [0, 0, 0], [r, h] = [1, 1], material, showEdge = false) {
    if (!material) material = new THREE.MeshPhongMaterial({color: 0xdddddd, specular: 0x111111, shininess: 1});
    let mesh = new THREE.Mesh(cylinderGeometry, material);
    sceneAddMesh(_scene, mesh, showEdge);
  
    mesh.type = 'Cylinder';
    mesh.scale.set(r, r, h);
    mesh.position.set(x, y, z);
  
    publicProperties(mesh);
    return mesh;
  }
  
  this.Vertices = function (points, size = 10) {
    const pointsMaterial = new THREE.PointsMaterial({color: 0xff0000, size: size,});
    let mesh = new THREE.Points(new BufferGeometry(), pointsMaterial);
    sceneAddMesh(_scene, mesh, false, false)
    
    mesh.type = 'Vertices';
    if (points) {
      mesh.geometry.setFromPoints(points);
      mesh.size = mesh.geometry.getAttribute('position').itemSize;
      mesh.coordinates = Array.from(mesh.geometry.getAttribute('position').array);
    }
    
    publicProperties(mesh);
    return mesh;
  }
  
  this.Segments = function (points, closed = false, color = 0x000, filled = false) {
    let segments;
    
    if (filled && closed) {
      const fill = new THREE.BufferGeometry();
      const coords = pointsToCoordinates(points);
      fill.setAttribute('position', new Float32BufferAttribute(coords, 3));
      fill.setIndex(triangulatePolygon(coords));
      fill.computeVertexNormals();
  
      segments = new THREE.Mesh(fill, new THREE.MeshPhongMaterial({color: color}));
  
      /* ---------- translate ---------- */
      segments.center = getPointsCenter(points);
      segments.geometry.translate(-segments.center.x, -segments.center.y, -segments.center.z);
      segments.position.copy(segments.center);
      segments.points = points;
      segments.size = segments.geometry.getAttribute('position').itemSize;
      segments.coordinates = Array.from(segments.geometry.getAttribute('position').array);
  
      sceneAddMesh(_scene, segments, true);
    } else {
      if (closed) {
        segments = new THREE.LineLoop(
          new THREE.BufferGeometry(),
          new THREE.LineBasicMaterial({color: color})
        );
  
      } else {
        segments = new THREE.Line(
          new THREE.BufferGeometry(),
          new THREE.LineBasicMaterial({color: color})
        );
      }
      sceneAddMesh(_scene, segments, false, false, []);
  
      if (points) {
        segments.geometry.setFromPoints(points);
        segments.points = points;
        segments.size = segments.geometry.getAttribute('position').itemSize;
        segments.coordinates = Array.from(segments.geometry.getAttribute('position').array);
      }
    }
  
    segments.type = 'Segments';
    segments.closed = closed;
  
    publicProperties(segments);
    return segments;
  }
  
  
  /**
   * 2D shape to extruded geometry, set extruded = 0.0 to get a 2d polygon
   *
   * @param segments
   * @param material
   * @param height
   * @param extruded
   * @param showEdge
   * @returns {Mesh<ExtrudeGeometry, *>}
   * @constructor
   */
  this.Prism = function (segments, material, height = 0.0, extruded = 0.0, showEdge = false) {
    const shape = new THREE.Shape().setFromPoints(segments.points)
    
    const mesh = new THREE.Mesh(
      new THREE.ExtrudeGeometry(shape, {
        depth: 1.,
        bevelEnabled: false
      }),
      material
    );
    
    mesh.type = 'Prism';
    mesh.segments = segments;
    mesh.center = getPointsCenter(shape.getPoints());
    mesh.geometry.translate(-mesh.center.x, -mesh.center.y, 0);
    mesh.position.x = mesh.center.x;
    mesh.position.y = mesh.center.y;
    mesh.position.z = height;
    mesh.scale.z = extruded;
    sceneAddMesh(_scene, mesh, showEdge)
  
    publicProperties(mesh);
    return mesh;
  }
  
  this.Mesh = function (vertices, faces, material) {
    if (!material) material = new THREE.MeshPhongMaterial({color: 0xdddddd, side: THREE.DoubleSide, flatShading: true});
    const geometry = new THREE.BufferGeometry();
  
    geometry.setAttribute('position', new Float32BufferAttribute(vertices.coordinates, vertices.size))
    geometry.setIndex(triangulateFace(coordinatesToPoints(vertices.coordinates, vertices.size), faces))
  
    geometry.computeBoundingBox();
    geometry.computeVertexNormals();
    geometry.normalsNeedUpdate = true;
  
    const mesh = new THREE.Mesh(geometry, material);
    mesh.type = 'Mesh';
  
    mesh.center = getPointsCenter(coordinatesToPoints(vertices.coordinates, vertices.size));
    mesh.geometry.translate(-mesh.center.x, -mesh.center.y, 0);
    mesh.position.x = mesh.center.x;
    mesh.position.y = mesh.center.y;
  
    sceneAddMesh(_scene, mesh);
  
    mesh.vertices = vertices;
    mesh.faces = faces;
    publicProperties(mesh);
    return mesh;
  }
  
  
  function getPointsCenter(points) {
    const v = new THREE.Vector3();
    points.forEach((pt) => {
      v.add(pt);
    })
    v.divideScalar(points.length);
    return v;
  }
  
  
  function updateModel(self, modelParam) {
    switch (self.type) {
      case 'Plane':
        self.scale.x = modelParam['w'];
        self.scale.y = modelParam['h'];
        break;
      case 'Cuboid' :
        self.scale.x = modelParam['w'];
        self.scale.y = modelParam['h'];
        self.scale.z = modelParam['d'];
        break;
      case 'Cylinder' :
        self.scale.x = modelParam['r'];
        self.scale.y = modelParam['r'];
        self.scale.z = modelParam['d'];
        break;
      case 'Sphere':
        self.scale.x = modelParam['r'];
        self.scale.y = modelParam['r'];
        self.scale.z = modelParam['r'];
        break;
      case 'Prism':
        self.segments = modelParam['segments'];
        self.shape = new THREE.Shape().setFromPoints(
          coordinatesToPoints(self.segments.coordinates, self.segments.size));
        self.geometry = new THREE.ExtrudeGeometry(self.shape, {depth: 1., bevelEnabled: false});
        self.position.z = modelParam['height'];
        self.scale.z = modelParam['extruded'];
        self.children[0] = createMeshWireframe(self, 0xffff00, 0.005)
        self.children[0].visible = false;
  
        self.center = getPointsCenter(self.shape.getPoints());
        self.geometry.translate(-self.center.x, -self.center.y, 0);
        self.position.x = self.center.x;
        self.position.y = self.center.y;
  
        break;
      case 'Segments':
        self.size = modelParam['size'];
        self.points = coordinatesToPoints(modelParam['coordinates'], self.size);
        self.closed = modelParam['closed'];
        self.geometry.setFromPoints(self.points);
        break;
      default:
        break;
    }
  }
  
  function modelParam(self) {
    switch (self.type) {
      case 'Vertices':
        self.size = self.geometry.getAttribute('position').itemSize
        self.coordinates = Array.from(self.geometry.getAttribute('position').array)
        return {size: self.size, coordinates: self.coordinates}
      case 'Segments':
        self.size = self.geometry.getAttribute('position').itemSize
        self.coordinates = Array.from(self.geometry.getAttribute('position').array)
        return {size: self.size, coordinates: self.coordinates, closed: self.closed};
      case 'Plane':
        return {w: self.scale.x, h: self.scale.y};
      case 'Cuboid':
        return {w: self.scale.x, h: self.scale.y, d: self.scale.z};
      case 'Cylinder':
        return {r: self.scale.x, d: self.scale.z};
      case 'Prism':
        return {segments: modelParam(self.segments), height: self.position.z, extrude: self.scale.z};
      case 'Sphere':
        return {r: self.scale.x};
      case 'Mesh':
        return {
          vertices: self.vertices, faces: self.faces
        }
      default:
        return {};
    }
  }
  
  
  function fromArchiJSON(self, element) {
    let m, scale, position, quaternion;
  
    switch (element.type) {
      case 'Cuboid':
      case 'Cylinder':
      case 'Plane':
      case 'Sphere':
        m = new THREE.Matrix4().fromArray(element.matrix);
        scale = new THREE.Vector3();
        position = new THREE.Vector3();
        quaternion = new THREE.Quaternion();
  
        m.decompose(position, quaternion, scale);
        self.quaternion.copy(quaternion);
        self.position.copy(position);
        self.scale.copy(scale);
        break;
      case 'Segments':
    
    }
  }
  
  function publicProperties(mesh) {
    
    mesh.updateModel = updateModel;
    mesh.modelParam = modelParam;
    mesh.fromArchiJSON = fromArchiJSON;
    
    mesh.exchange = true;
    mesh.toArchiJSON = function () {
      return Object.assign({
          type: mesh.type,
          matrix: mesh.matrix.elements,
          uuid: mesh.uuid,
          position: mesh.position
        },
        
        mesh.modelParam(mesh));
    }
    
    mesh.toInfoCard = function () {
      let o = mesh;
      window.InfoCard.info.uuid = o.uuid;
      window.InfoCard.info.position = o.position;
      window.InfoCard.info.model = o.modelParam(o);
      window.InfoCard.info.properties = {
        type: o.type, material:
          JSON.stringify({
            type: o.material.type,
            uuid: o.material.uuid,
            color: o.material.color,
            opacity: o.material.opacity
          })
        , matrix: o.matrix.elements
      };
    }
  }
  
  /* ---------- Geometry Operator ---------- */
  function pointsToCoordinates(points) {
    const coords = []
    points.forEach((p) => coords.push(p.x, p.y, p.z));
    return coords;
  }
  
  function coordinatesToPoints(array, size) {
    const points = []
    const cnt = array.length / size;
    if (size === 2) {
      for (let i = 0; i < cnt; ++i) {
        points.push(new THREE.Vector2(array[i * 2], array[i * 2 + 1]))
      }
      
    } else if (size === 3) {
      for (let i = 0; i < cnt; ++i) {
        points.push(new THREE.Vector3(array[i * 3], array[i * 3 + 1], array[i * 3 + 2]))
      }
    }
    return points;
  }
  
  function triangulateFace(points, faces) {
    const index = [];
    if (!faces.size) faces.size = [3];
    if (!faces.count) faces.count = [faces.index.length / 3];
    
    let cur = 0;
    for (let k in faces.count) {
      let cnt = faces.count[k];
      let size = faces.size[k];
      for (let i = 0; i < cnt; ++i) {
        const pts = [];
        const idx = [];
        for (let j = 0; j < size; ++j) {
          const id = faces.index[cur + i * size + j];
          idx.push(id)
          pts.push(points[id])
        }
        // index.push(triangulatePolygon(pointsTocoordinates(pts)));
        const fs = triangulatePolygon(pointsToCoordinates(pts));
        fs.forEach((f) => index.push(idx[f]));
      }
      cur += cnt * size;
    }
    return index;
  }
  
  /**
   *
   * @param coordinates
   * @returns index a list of face
   */
  function triangulatePolygon(coordinates) {
    // let index = earcut(coordinates, null, 3);
    // if(index.length === 0) {
    // console.log(JSON.parse(JSON.stringify(coordinates)))
    let pts = coordinatesToPoints(coordinates, 3);
    let n = pts.length;
    let id = 0;
    for (let i = 0; i < n; ++i) {
      if (pts[i].y > pts[id].y) {
        id = i;
      } else if (Math.abs(pts[i].y - pts[id].y) < 1e-6 && pts[i].z > pts[id].z) {
        id = i;
      }
    }
  
    const v0 = new THREE.Vector3().subVectors(pts[(id - 1 + n) % n], pts[id]);
    const v1 = new THREE.Vector3().subVectors(pts[(id + 1) % n], pts[id]);
    const normal = v0.cross(v1).normalize();
    const z = new THREE.Vector3(0, 0, 1);
    const axis = new THREE.Vector3().crossVectors(normal, z);
    const theta = normal.angleTo(z);
    // console.log(normal, axis, theta)
    let m;
    if (axis.length() < 1e-6)
      m = new THREE.Matrix4().makeRotationAxis(new THREE.Vector3(1, 0, 0), Math.PI - theta);
    else m = new THREE.Matrix4().makeRotationAxis(axis, -theta);
    pts.forEach((pt) => pt.applyMatrix4(m));
    // console.log(pts)
    const index = earcut(pointsToCoordinates(pts), null, 3);
    return index.flat();
  }
  
  
  function pointsInsideSegments(segments, area) {
    const face = triangulatePolygon(pointsToCoordinates(segments.points));
    const ret = []
    let cnt = face.length / 3;
    for (let i = 0; i < cnt; ++i) {
      let tri = [];
      for (let j = 0; j < 3; ++j) {
        tri.push(segments.points[face[i * 3 + j]]);
      }
      let totArea = Math.abs(ShapeUtils.area(tri));
      pointsInsideTriangle(tri, parseInt(totArea / area), ret)
    }
    return ret;
  }
  
  function pointsInsideTriangle(triangle, num, pts) {
    for (let k = 0; k < num; ++k) {
      let coeff = []
      coeff.push(Math.random());
      coeff.push(Math.random() * (1 - coeff[0]));
      coeff.push(1 - coeff[0] - coeff[1])
      let pt = new THREE.Vector3();
      for (let i = 0; i < 3; ++i) {
        let v = new THREE.Vector3().copy(triangle[i]).multiplyScalar(coeff[i])
        pt.add(v)
      }
      pts.push(pt);
    }
  }
  
  this.coordinatesToPoints = coordinatesToPoints;
  this.pointsInsideSegments = pointsInsideSegments;
  this.pointsInsideTriangle = pointsInsideTriangle;
  
}


function createMeshEdge(mesh, color = 0x000000) {
  if (!mesh.geometry) return;
  
  setPolygonOffsetMaterial(mesh.material);
  
  const matLine = new THREE.LineBasicMaterial({color: color});
  const geoLine = new THREE.EdgesGeometry(mesh.geometry);
  return new THREE.LineSegments(geoLine, matLine);
}

/**
 * create mesh wireframe with linewidth, must use specific LineMaterial in three@r0.121
 * @param mesh
 * @param color
 * @param linewidth
 * @returns {Wireframe}
 */
function createMeshWireframe(mesh, color = 0xffff00, linewidth) {
  
  setPolygonOffsetMaterial(mesh.material);
  
  const matLine = new LineMaterial({color: color, linewidth: linewidth});
  
  const geoLine = new WireframeGeometry2(mesh.geometry);
  const wireframe = new Wireframe(geoLine, matLine);
  wireframe.computeLineDistances();
  wireframe.scale.set(1, 1, 1);
  return wireframe;
}

function sceneMesh(object, shadow = true, doubleSide = false, layer = [0]) {
  object.traverseVisible((mesh) => {
    mesh.layer = layer;
    console.log(mesh)
    if (shadow) {
      mesh.castShadow = true;
      mesh.receiveShadow = true;
    }
    if (mesh.isMesh) {
      if (doubleSide) {
        setMaterialDoubleSide(mesh.material);
      }
      mesh.add(createMeshWireframe(mesh, 0xffff00, 0.005));
      mesh.children[0].visible = false;
      mesh.layer = [0];
    }
  });
}

/**
 * add a new mesh to a object3D (scene, group)
 * @param object
 * @param mesh
 * @param edge
 * @param shadow
 * @param layer
 */
function sceneAddMesh(object, mesh, edge = true, shadow = true, layer = [0]) {
  // show edge
  if (mesh.isMesh) {
    setPolygonOffsetMaterial(mesh.material);
    mesh.add(createMeshWireframe(mesh, 0xffff00, 0.005));
    mesh.children[0].visible = false;
  }
  
  if (edge.isLineSegments) {
    mesh.add(edge);
  } else if (edge === true) {
    mesh.add(createMeshEdge(mesh));
  }
  // show shadow
  if (shadow) {
    mesh.castShadow = true;
    mesh.receiveShadow = true;
  }
  
  // layer, default is [0]
  mesh.layer = layer;
  object.add(mesh);
}

export {
  GeometryFactory,
  sceneMesh,
  sceneAddMesh,
  createMeshWireframe,
  createMeshEdge
};