/* eslint-disable no-unused-vars,no-case-declarations */
"use strict";
import * as THREE from "three";
import * as ARCH from "@/archiweb"
import {DragControls} from "three/examples/jsm/controls/DragControls";
import {CircleGeometry} from "three";

let scene, renderer, gui, camera;
let gf, mf;
let am;
let tf;

let archijson;

let drag, controller;

/* ---------- main entry ---------- */

function main() {
    // initialize basic components
    const viewport = new ARCH.Viewport();
    scene = viewport.scene;
    renderer = viewport.renderer;
    gui = viewport.gui;
    camera = viewport.to2D();

    // initialize factory
    gf = new ARCH.GeometryFactory(scene);
    mf = new ARCH.MaterialFactory();

    // 图层管理
    am = viewport.enableAssetManager();

    // 操作轴
    tf = viewport.enableTransformer();
    tf.control.showZ = false;
    tf.objectChanged = objectChanged;
    tf.draggingChanged = draggingChanged;
    tf.deleteChanged = deleteChanged;

    // initialize archijson
    archijson = new ARCH.ArchiJSON(scene, gf);

    initScene();

    initGeo();
    // const sceneBasic = new ARCH.SceneBasic(scene, renderer);
    // sceneBasic.addGUI(gui.gui);
    initGUI();

    // initialize drag
    renderer.domElement.addEventListener('click', addNode, false);

    controller = viewport.controller;
    // initDrag();

    // viewport.draw = draw;
}

function updateObject(uuid, position) {
    const o = scene.getObjectByProperty('uuid', uuid);
    o.position.copy(position);
}

/**
 * search object in the scene by its uuid
 * @param uuid
 * @returns {this}
 */
window.searchSceneByUUID = function (uuid) {
    return scene.getObjectByProperty('uuid', uuid);
}

/* ---------- scene and GUI ---------- */

/**
 * initialize scene and geometries
 *
 */
function initScene() {
    // axes
    const axes = new THREE.AxesHelper(50)
    scene.add(axes);

    // background
    let controls = {
        color: 0xfafafa
    };
    scene.background = new THREE.Color(controls.color);
    gui.gui.addColor(controls, 'color').onChange(function () {
        scene.background = new THREE.Color(controls.color);
    });

    // light
    const light = new THREE.SpotLight(0xffffff, 1.5);
    light.position.set(0, 0, 1000);
    scene.add(light);
}

const editSwitches = new function () {
    this.EDIT_NODE = false;
    this.EDIT_ATRIUM = false;
    this.EDIT_CURVE = false;
}

let editProperties = new function () {
    this.bufferCurveTension = 0.5;
}

/**
 * initialize the GUI
 *
 */
function initGUI() {
    const updateButtons = {
        update: function () {
            updateGraph();
            sendGraph();
            // updateBuffer();
        },

        clearCurrent: function () {
            currentTreeNode = undefined;
        }
    }

    // edit control folder
    const editControlFolder = gui.gui.addFolder('Edit Control');
    editControlFolder.add(editSwitches, 'EDIT_NODE').name('edit node').listen().onChange(() => {
    });
    editControlFolder.add(editSwitches, 'EDIT_ATRIUM').name('edit atrium').listen().onChange(() => {
        if (editSwitches.EDIT_ATRIUM) {
            enableAtriumEdit();
        } else {
            disableEdit();
        }
    });
    editControlFolder.add(editSwitches, 'EDIT_CURVE').name('edit curve').listen().onChange(() => {
        if (editSwitches.EDIT_CURVE) {
            editSwitches.EDIT_ATRIUM = false;
            disableEdit();
            enableCurveEdit();
        } else {
            disableEdit();
        }
    });
    editControlFolder.add(propertiesSend, 'bufferDist', 1, 15).onChange(() => {
        sendGraph();
    });
    editControlFolder.add(editProperties, 'bufferCurveTension', 0, 1);

    const atriumFolder = gui.gui.addFolder('Atrium');
    atriumFolder.add(atriumTypeProperties, 'type', ['rect', 'circle', 'poly', 'curve']).listen().onChange(() => {
        if (currentTreeNode !== undefined) {
            if (currentTreeNode.atriumID === undefined) {
                addAtrium(atriumTypeProperties.type);
            } else {
                let currAtrium = searchSceneByUUID(currentTreeNode.atriumID);
                let index = atriums.indexOf(currAtrium);

                // clear current
                atriums.splice(index, 1);
                scene.remove(currAtrium);
                propertiesSend.atriumNum--;

                addAtrium(atriumTypeProperties.type);
            }
        }
    })

    gui.gui.add(updateButtons, 'update').name('send');

    gui.gui.add(updateButtons, 'clearCurrent');
}

/* ---------- core: initialize geometries ---------- */

// drag-able elements
let innerNodesDrag = [];
let atriumControlsDrag = [];
let bufferControlDrag = [];

// geometries to send
let innerNodesSend = undefined;
let atriums = [];
let bufferCurve = [];

/**
 * initialize the beginning geometries in the scene
 *
 */
function initGeo() {
    // geometries: cylinders
    let pos = [[-66, -36], [0, -35], [43, -40], [44, 9], [39, 81]];
    for (let p of pos) {
        innerNodesDrag.push(gf.Cylinder(p, [2, 1],
            new THREE.MeshLambertMaterial({color: 0xff0000})));
    }

    // geometries: points to send (from cylinders)
    innerNodesSend = gf.Vertices();
    innerNodesSend.visible = false;

    // set asset manager
    am.setCurrentID(0);
    ARCH.refreshSelection(scene);
}

/* ---------- core: interact ---------- */

let currentTreeNode; // spanning tree node
let currentAtriumControl;

function addNode(event) {
    if (editSwitches.EDIT_NODE) {
        const rayCaster = new THREE.Raycaster();
        const xoy = new THREE.Plane(new THREE.Vector3(0, 0, 1), 0);

        const mouse = new THREE.Vector2(
            (event.clientX / window.innerWidth) * 2 - 1,
            -(event.clientY / window.innerHeight) * 2 + 1
        )
        rayCaster.setFromCamera(mouse, camera);
        let pt = rayCaster.ray.intersectPlane(xoy, new THREE.Vector3());
        innerNodesDrag.push(gf.Cylinder([pt.x, pt.y, pt.z], [2, 2],
            new THREE.MeshLambertMaterial({color: 0xff0000})));
        updateGraph();
        sendGraph();
    }
}

/**
 * update atrium of the node
 * @param node
 */
function updateAtriumPos(node) {
    if (node.atriumID !== undefined) {
        let currAtrium = searchSceneByUUID(node.atriumID);
        let currPos = node.position;
        currAtrium.position.set(currPos.x, currPos.y, currPos.z);
    }
}

const atriumGeoProperties = {
    rectW: 20, rectD: 20,
    cirR: 10, cirN: 24,
    polyN: 6, polyD: 10,
    curveN: 6, curveD: 10
}

const atriumTypeProperties = {
    type: ""
}

/**
 * add atrium shape at the selected node
 * @param type atrium type
 */
function addAtrium(type) {
    let currPos = [];
    let p = currentTreeNode.position;
    currPos.push(p.x, p.y, p.z);

    if (type === 'rect') {
        let rectAtrium = gf.Plane(currPos, [atriumGeoProperties.rectW, atriumGeoProperties.rectD]);
        setAtrium(rectAtrium);
    } else if (type === 'poly') {
        let n = atriumGeoProperties.polyN;
        let d = atriumGeoProperties.polyD;
        let vecList = createShape(p, n, d);
        let polyAtrium = gf.Segments(vecList, true);
        setAtrium(polyAtrium);
        polyAtrium.controlCoordinates = toControlPointArray(vecList);
    } else if (type === 'circle') {
        let n = atriumGeoProperties.cirN;
        let r = atriumGeoProperties.cirR;
        let vecList = createShape(p, n, r);
        let circleAtrium = gf.Segments(vecList, true, null, true);
        setAtrium(circleAtrium);
    } else if (type === 'curve') {
        let n = atriumGeoProperties.curveN;
        let d = atriumGeoProperties.curveD;
        let vecList = createShape(p, n, d);
        let points = new THREE.CatmullRomCurve3(vecList, true).getPoints(24);
        let curveAtrium = gf.Segments(points, true);
        setAtrium(curveAtrium);
        curveAtrium.controlCoordinates = toControlPointArray(vecList);
    }
    propertiesSend.atriumNum++;

    // inner function: setup an atrium
    function setAtrium(atrium) {
        // setup reference
        currentTreeNode.atriumType = type;       // 当前tree node的中庭类型
        atrium.atriumType = type;                // 当前中庭图元的类型
        currentTreeNode.atriumID = atrium.uuid;  // 当前tree node的中庭id
        atrium.parentTreeNode = currentTreeNode; // 当前中庭的父节点node

        // add to scene
        atriums.push(atrium);
        am.addSelection(atrium, 1);
        ARCH.refreshSelection(scene);
    }

    // inner function: setup the control point of an atrium
    function toControlPointArray(listOfVectors) {
        let pts = [];
        for (let v of listOfVectors) {
            pts.push([v.x, v.y, v.z]);
        }
        return pts;
    }

    // inner function: create the initial shape of an atrium
    function createShape(currPos, ptsNum, dist) {
        let pts = [];
        let theta = (Math.PI * 2) / ptsNum;
        let d = dist;
        for (let i = 0; i < ptsNum; i++) {
            let angle = i * theta;
            pts.push(new THREE.Vector3(
                currPos.x + d * Math.cos(angle),
                currPos.y + d * Math.sin(angle),
                0)
            );
        }
        return pts;
    }
}

/**
 * add control cuboids to the atrium to edit shape
 *
 */
function enableAtriumEdit() {
    for (let a of atriums) {
        let type = a.atriumType;
        if (type === 'poly' || type === 'curve') {
            let pos = a.controlCoordinates;
            atriumControlsDrag.push(setupDragCuboid(a, pos));
        }
    }
    am.setCurrentID(2);

    // inner function: setup cuboids to drag from controlCoordinates
    function setupDragCuboid(atrium, pos) {
        let cuboidList = [];
        for (let p of pos) {
            let dragCuboid = gf.Cuboid(p, [2, 2, 1], new THREE.MeshLambertMaterial({color: 0xFFA500}));

            dragCuboid.parentAtrium = atrium;      // 当前拖拽点所属的中庭

            am.addSelection(dragCuboid, 2);
            ARCH.refreshSelection(scene);
            cuboidList.push(dragCuboid);
        }
        atrium.controlCuboidList = cuboidList;    // 当前中庭的控制点数组
        return cuboidList;
    }
}

/**
 * add control cuboids to the buffer curve to edit shape
 *
 */
function enableCurveEdit() {
    bufferCurve = archijson.bufferCurve();
    let bufferCurveControl = archijson.bufferCurveControl();

    for (let i = 0; i < bufferCurve.length; i++) {
        bufferControlDrag.push(setupDragCuboid(bufferCurve[i], bufferCurveControl[i]));
    }

    // inner function: setup cuboids to drag from bufferCurveControl in archijson
    function setupDragCuboid(curve, pos) {
        let cuboidList = [];
        for (let p of pos) {
            let dragCuboid = gf.Cuboid(p, [2, 2, 1], new THREE.MeshLambertMaterial({color: 0x3CB371}));

            dragCuboid.parentCurve = curve;      // 当前拖拽点所属的中庭

            am.addSelection(dragCuboid, 2);
            ARCH.refreshSelection(scene);
            cuboidList.push(dragCuboid);
        }
        curve.controlCuboidList = cuboidList;    // 当前中庭的控制点数组
        return cuboidList;
    }
}

/**
 * clear all control cuboid
 *
 */
function disableEdit() {
    for (let cuboidList of atriumControlsDrag) {
        // set final control coordinates
        let parentAtrium = cuboidList[0].parentAtrium;
        let lastVecList = [];
        cuboidList.forEach((c) => lastVecList.push([c.position.x, c.position.y, c.position.z]));
        parentAtrium.controlCoordinates = lastVecList;
        // remove
        cuboidList.forEach((c) => scene.remove(c));
    }
    for (let cuboidList of bufferControlDrag) {
        // set final control coordinates
        let parentCurve = cuboidList[0].parentCurve;
        let lastVecList = [];
        cuboidList.forEach((c) => lastVecList.push([c.position.x, c.position.y, c.position.z]));
        parentCurve.controlCoordinates = lastVecList;
        // remove
        cuboidList.forEach((c) => scene.remove(c));
    }
    am.setCurrentID(0);
}

/* ---------- core: Transformer interact & update ---------- */

/**
 * Transformer: select object (override)
 * @param o object to interact
 */
function objectChanged(o) {
    if (innerNodesDrag.includes(o)) {
        currentTreeNode = o;
        atriumTypeProperties.type = o.atriumType;
        am.highlightCurrent();
    }
}

/**
 * Transformer: drag object (override)
 * @param o object to interact
 * @param event
 */
function draggingChanged(o, event) {
    if (editSwitches.EDIT_CURVE) {
        if (!event) {
            for (let controls of bufferControlDrag) {
                if (controls.includes(o)) {
                    let currCurve = o.parentCurve;
                    let vecList = [];
                    let controlCuboids = currCurve.controlCuboidList;
                    controlCuboids.forEach((cc) => vecList.push(cc.position));
                    let newPoints = new THREE.CatmullRomCurve3(vecList, false, 'centripetal', editProperties.bufferCurveTension).getPoints(24);
                    currCurve.geometry.setFromPoints(newPoints);
                    // sendBuffer();
                    break;
                }
            }
        }
    } else if (editSwitches.EDIT_ATRIUM) {
        if (!event) {
            for (let controls of atriumControlsDrag) {
                if (controls.includes(o)) {
                    let currAtrium = o.parentAtrium;
                    if (currAtrium.atriumType === 'poly') {
                        let controlCuboids = currAtrium.controlCuboidList;
                        currAtrium.geometry.setFromPoints(controlCuboids.map((c) => c.position));
                        sendGraph();
                    } else if (currAtrium.atriumType === 'curve') {
                        let vecList = [];
                        let controlCuboids = currAtrium.controlCuboidList;
                        controlCuboids.forEach((cc) => vecList.push(cc.position));
                        let newPoints = new THREE.CatmullRomCurve3(vecList, true).getPoints(24);
                        currAtrium.geometry.setFromPoints(newPoints);
                        sendGraph();
                    }
                    break;
                }
            }
        }
    } else {
        if (!event && innerNodesDrag.includes(o)) {
            updateAtriumPos(o);
            updateGraph();
            sendGraph();
        }
    }
}

/**
 *
 * @param o Transformer: select object (override)
 */
function deleteChanged(o) {
    if (editSwitches.EDIT_CURVE) {
        for (let controls of bufferControlDrag) {
            let index = controls.indexOf(o);
            if (index > -1) {
                let currCurve = o.parentCurve;
                controls.splice(index, 1);
                scene.remove(o);
                buildCurve(controls, currCurve);
                break;
            }
        }
    } else if (editSwitches.EDIT_ATRIUM) {
        for (let controls of atriumControlsDrag) {
            let index = controls.indexOf(o);
            if (index > -1) {
                let currAtrium = o.parentAtrium;
                controls.splice(index, 1);
                scene.remove(o);
                if (currAtrium.atriumType === 'poly') {
                    buildPoly(controls, currAtrium);
                } else if (currAtrium.atriumType === 'curve') {
                    buildCurve(controls, currAtrium);
                }
                break;
            }
        }
    } else {
        let index = innerNodesDrag.indexOf(o);
        if (index > -1) {
            innerNodesDrag.splice(index, 1);
            updateAtriumPos(o);
            updateGraph();
            sendGraph();
        }
    }
}

/**
 * build a new curve by a new list of dragging objects
 * @param dragList     new list of control-dragging objects
 * @param curve        original curve
 */
function buildCurve(dragList, curve) {
    let vecList = [];
    dragList.forEach((obj) => vecList.push(obj.position));
    let newPoints = new THREE.CatmullRomCurve3(
        vecList,
        false,
        'centripetal',
        editProperties.bufferCurveTension
    ).getPoints(24);
    curve.geometry.setFromPoints(newPoints);
}

/**
 * build a new polygon by a new list of dragging objects
 * @param dragList     new list of control-dragging objects
 * @param poly         original polygon
 */
function buildPoly(dragList, poly) {
    poly.geometry.setFromPoints(dragList.map((obj) => obj.position));
}

/* ---------- core: send to backend ---------- */

let propertiesSend = {
    atriumNum: 0,
    bufferDist: 6

}

/**
 * core function of packaging geometries, sending and receiving
 *
 */
function sendGraph() {
    let objects = [];

    // tree nodes
    objects.push(innerNodesSend);
    // atriums
    atriums.forEach((a) => objects.push(a));

    archijson.sendArchiJSON('bts:sendGeometry', objects, propertiesSend);
    console.log('yes')
}

function sendBuffer() {
    let objects = [];
    // buffer curves
    bufferCurve.forEach((c) => objects.push(c));
    archijson.sendArchiJSON('ftb:sendBuffer', objects, propertiesSend);
}

/**
 * update the basic traffic graph
 *
 */
function updateGraph() {
    let pos = []
    for (let c of innerNodesDrag) {
        let p = c.position
        pos.push(p.x, p.y, p.z)
    }
    innerNodesSend.geometry.setAttribute('position', new THREE.Float32BufferAttribute(pos, 3));
}

/* ---------- APIs & exports ---------- */

export {
    main,
    updateObject,
}