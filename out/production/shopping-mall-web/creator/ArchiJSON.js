import socket from "@/socket";
import * as THREE from "three";


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
 * You need to modify this file for specific usage
 * @param _scene
 * @constructor
 */
const ArchiJSON = function (_scene, _geoFty) {
    let scope = this;

    let boundarySegments = [];
    let treeSegments = [];
    let bufferCurve = [];
    let bufferCurveControl = [];

    let partitionResult = [];

    /* ---------- core: create geometries from json ---------- */

    /**
     * refresh the scene
     *
     */
    function clearScene1() {
        // 轮廓
        if (boundarySegments.length > 0) {
            boundarySegments.forEach((line) => {
                line.parent.remove(line);
            })
        }
        // 生成树
        if (treeSegments.length > 0) {
            treeSegments.forEach((line) => {
                line.parent.remove(line);
            })
        }
        // buffer曲线
        if (bufferCurve.length > 0) {
            bufferCurve.forEach((c) => {
                c.parent.remove(c);
            })
        }
        boundarySegments = [];
        treeSegments = [];
        bufferCurve = [];
        bufferCurveControl = []; // 不是物件，只是vec
    }

    function clearScene2(){
        // 剖分结果
        if (partitionResult.length > 0) {
            partitionResult.forEach((poly) => {
                poly.parent.remove(poly);
            })
        }
        partitionResult = [];
    }

    /**
     * filter different type of geometries
     * @param geometryElements
     */
    function segmentsFilter(geometryElements) {
        for (let e of geometryElements) {
            if (e.type === 'Segments') {
                const segs = _geoFty.Segments();
                if (e.properties.name === 'tree') {
                    segs.geometry.setAttribute('position', new THREE.Float32BufferAttribute(e.coordinates, e.size));
                    treeSegments.push(segs);
                } else if (e.properties.name === 'boundary') {
                    segs.geometry.setAttribute('position', new THREE.Float32BufferAttribute(e.coordinates, e.size));
                    boundarySegments.push(segs);
                }
            } else if (e.type === 'Vertices') {
                if (e.properties.name === 'bufferControl') {
                    let vectors = [];
                    let vecArray = [];
                    let num = e.coordinates.length / e.size;
                    for (let i = 0; i < num; i++) {
                        let coord = [];
                        for (let j = 0; j < e.size; j++) {
                            coord.push(e.coordinates[i * e.size + j]);
                        }
                        vectors.push(new THREE.Vector3(coord[0], coord[1], coord[2]));
                    }
                    const curve = new THREE.CatmullRomCurve3(vectors,false,'centripetal',0.8);
                    const points = curve.getPoints(50);

                    vectors.forEach((v) => vecArray.push([v.x, v.y, v.z]));
                    let curveTest = _geoFty.Segments(points);
                    bufferCurve.push(curveTest);
                    bufferCurveControl.push(vecArray);
                }
            }
        }
    }


    socket.on('stb:receiveGeometry', async function (message) {
        // get geometry
        scope.parseGeometry(message);
    });

    socket.on('btf:receivePartition', async function (message) {
        // get geometry
        scope.parseGeometry2(message);
    });

    this.sendArchiJSON = function (eventName, objects, properties = {}) {
        let geometries = [];
        for (let obj of objects) if (obj.exchange) {
            geometries.push(obj.toArchiJSON());
        }

        socket.emit(eventName, {geometryElements: geometries, properties: properties});
    }

    this.parseGeometry = function (geometryElements) {
        clearScene1();
        segmentsFilter(geometryElements);
    }

    this.parseGeometry2 = function (geometryElements) {
        clearScene2();
        for (let e of geometryElements) {
            if (e.type === 'Segments') {
                const segs = _geoFty.Segments();
                if(e.properties.name === 'shopCell'){
                    segs.geometry.setAttribute('position', new THREE.Float32BufferAttribute(e.coordinates, e.size));
                    partitionResult.push(segs);
                }
            }
        }
    }

    this.bufferCurve = function () {
        return bufferCurve;
    }

    this.bufferCurveControl = function (){
        return bufferCurveControl;
    }
}

export {ArchiJSON};