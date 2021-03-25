/* eslint-disable no-unused-vars,no-case-declarations */
"use strict";
import * as THREE from "three";
import * as ARCH from "@/archiweb"
import {DragControls} from "three/examples/jsm/controls/DragControls";
import {CircleGeometry} from "three";

let scene, renderer, gui, camera;   // basic components
let gf, mf;                         // factories
let am;                             // asset manager

let archijson;

let drag, controller;

/* ---------- main entry ---------- */

function main() {
    // basic components
    const viewport = new ARCH.Viewport();
    scene = viewport.scene;
    renderer = viewport.renderer;
    gui = viewport.gui;
    camera = viewport.to2D();

    am = viewport.enableAssetManager(); // 图层管理

    viewport.enableTransformer(); // 操作轴

    // initialize scene and GUI
    initScene();
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

/* ---------- create the scene ---------- */

let innerNodesDrag = [];
let controlCubesDrag = [];
let innerNodesSend = [];

let atriums = [];

/**
 * initialize scene and geometries
 *
 */
function initScene() {
    initSceneElements();

    // geometries: cylinders
    let pos = [[-66, -36], [0, -35], [43, -40], [44, 9], [39, 81]];
    for (let p of pos) {
        innerNodesDrag.push(gf.Cylinder(p, [2, 1],
            new THREE.MeshLambertMaterial({color: 0xff0000})));
    }

    // geometries: points to send (from cylinders)
    innerNodesSend = gf.Vertices();
    innerNodesSend.visible = false;

    // curve test
    const curve = new THREE.CatmullRomCurve3([
        new THREE.Vector3(-100, 0, 0),
        new THREE.Vector3(-50, 50, 0),
        new THREE.Vector3(0, 0, 0),
        new THREE.Vector3(50, -50, 0),
        new THREE.Vector3(100, 0, 0)
    ]);
    curve.closed = true;
    const points = curve.getPoints(50);
    let curveTest = gf.Segments(points);

    // set asset manager
    // am.refreshSelection(scene);
    // am.addSelection([b1, b2, b3, b4, s1], 1);
    // am.addSelection(atriums, 1);
    am.setCurrentID(1);

    // refresh global objects
    ARCH.refreshSelection(scene);
}

/**
 * initialize basic elements of the scene (axes, lights, etc.)
 *
 */
function initSceneElements() {
    // initialize factory
    gf = new ARCH.GeometryFactory(scene);
    mf = new ARCH.MaterialFactory();

    // initialize archijson
    archijson = new ARCH.ArchiJSON(scene, gf);

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

/* ---------- interact ---------- */

let EDIT_NODE = false;
let EDIT_POLYATRIUM = false;
const rayCaster = new THREE.Raycaster();
const xoy = new THREE.Plane(new THREE.Vector3(0, 0, 1), 0);

const atriumGeoProperties = {
    rectW: 30,
    rectD: 30,
    cirA: 20,
    cirB: 40,
    cirN: 24,
    polyN: 6,
    polyD: 20
}

const atriumTypeProperties = {
    type: ""
}

function addNode(event) {
    if (EDIT_NODE) {
        const mouse = new THREE.Vector2(
            (event.clientX / window.innerWidth) * 2 - 1,
            -(event.clientY / window.innerHeight) * 2 + 1
        )
        rayCaster.setFromCamera(mouse, camera);
        let pt = rayCaster.ray.intersectPlane(xoy, new THREE.Vector3());
        innerNodesDrag.push(gf.Cylinder([pt.x, pt.y, pt.z], [2, 2],
            new THREE.MeshLambertMaterial({color: 0xff0000})));
        updateGraph();
        send();
    }
}

/**
 * update atrium of the node
 * @param node
 */
function updateAtriumPos(node) {
    if (node.atriumID != null) {
        let currAtrium = searchSceneByUUID(node.atriumID);
        let currPos = node.position;
        currAtrium.position.set(currPos.x, currPos.y, currPos.z);
    }
}

/**
 * add atrium shape at the selected node
 * @param type atrium type
 */
function addAtrium(type) {
    let currPos = []
    let p = currentNode.position
    currPos.push(p.x, p.y, p.z)

    if (type === 'rect') {
        let rectAtrium = gf.Plane(currPos, [atriumGeoProperties.rectW, atriumGeoProperties.rectD]);
        setAtrium(rectAtrium);
    } else if (type === 'poly') {
        let polyPoints = [];
        let cubes = [];
        let theta = (Math.PI * 2) / atriumGeoProperties.polyN;
        let d = atriumGeoProperties.polyD;
        for (let i = 0; i < atriumGeoProperties.polyN; i++) {
            let angle = i * theta;

            // cubes.push(gf.Cuboid([p.x + d * Math.cos(angle), p.y + d * Math.sin(angle), 0]));
            polyPoints.push(new THREE.Vector3(p.x + d * Math.cos(angle), p.y + d * Math.sin(angle), 0));
        }
        // polyPoints.forEach((p) => cubes.push(gf.Cuboid(p, [1, 1, 1], mf.Flat(0xff0000))));
        // cubes.forEach((c) => polyPoints.push(c.position));
        // let polyAtrium = gf.Segments(polyPoints, true, null, true);
        // polyAtrium.controlCube = cubes;
        // cubes.forEach((c) => controlCubesDrag.push(c));
        // cubes.forEach((c) => c.parent = polyAtrium);

        let polyAtrium = gf.Segments(polyPoints, true, null, true);
        setAtrium(polyAtrium);
    } else if (type === 'circle') {
        let cirPoints = [];
        let theta = (Math.PI * 2) / atriumGeoProperties.cirN;
        let r = atriumGeoProperties.cirA;
        for (let i = 0; i < atriumGeoProperties.cirN; i++) {
            let angle = i * theta;
            cirPoints.push(new THREE.Vector3(p.x + r * Math.cos(angle), p.y + r * Math.sin(angle), 0));
        }
        let circleAtrium = gf.Segments(cirPoints, true, null, true);
        setAtrium(circleAtrium);
    }

    function setAtrium(atrium) {
        atriums.push(atrium);
        am.addSelection(atrium, 1);
        ARCH.refreshSelection(scene);
        currentNode.atriumType = type;
        currentNode.atriumID = atrium.uuid;
    }

    propertiesSend.atriumNum++;
}

/* ---------- send to backend ---------- */

let propertiesSend = {
    atriumNum: 0,
    bufferDist: 6
}

function send() {
    let objects = [];
    objects.push(innerNodesSend);
    for (let a of atriums) {
        objects.push(a);
    }

    archijson.sendArchiJSON('bts:sendGeometry', objects, propertiesSend);

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

function updateBuffer() {
    if (atriums.length > 0) {
        archijson.sendArchiJSON('bts:sendGeometry', atriums);
    }
}

/* ---------- GUI setup ---------- */

let currentNode;

/**
 * initialize the GUI
 *
 */
function initGUI() {
    const controls = new function () {
        this.color = 0x666600;
        this.editNode = false;
        this.editPolyAtrium = false;

        this.button = function () {
            // do something
        }
    }

    const testControl = {
        num: 100,

        update: function () {
            updateGraph();
            send();
            // updateBuffer();
        },

        clearCurrent: function () {
            currentNode = null;
        }
    }

    // // color picker
    // folder.addColor(controls, 'color');
    //
    // // select list
    // folder.add(testControl, 'num', 50, 200);

    // edit control folder
    const editControlFolder = gui.gui.addFolder('Edit Control');
    editControlFolder.add(controls, 'editNode').listen().onChange(() => {
        EDIT_NODE = controls.editNode;
    });
    editControlFolder.add(controls, 'editPolyAtrium').listen().onChange(() => {
        EDIT_POLYATRIUM = controls.editPolyAtrium;
        initDrag();
    });
    editControlFolder.add(propertiesSend, 'bufferDist', 1, 15).onChange(() => {
        send();
    });

    atriumFolder();

    gui.gui.add(testControl, 'update').name('send');

    gui.gui.add(testControl, 'clearCurrent');
}

function atriumFolder() {
    const atriumFolder = gui.gui.addFolder('Atrium');
    atriumFolder.add(atriumTypeProperties, 'type', ['rect', 'circle', 'poly']).listen().onChange(() => {
        if (currentNode != null) {
            addAtrium(atriumTypeProperties.type);
        }
    })
}

/* ---------- drag control ---------- */

function initDrag() {
    if (!EDIT_POLYATRIUM) {
        drag = new DragControls(innerNodesDrag, camera, renderer.domElement);
        drag.addEventListener('hoveron', function (event) {
            let o = event.object;
            console.log(o)
            if (o.hasOwnProperty('toInfoCard')) {
                o.toInfoCard();
                currentNode = o;
                controller.enabled = false;
                atriumTypeProperties.type = o.atriumType;
            }

        });
        drag.addEventListener('hoveroff', function () {
            controller.enabled = true;
        });

        drag.addEventListener('dragend', function (event) {
            let o = event.object;
            updateAtriumPos(o);
            updateGraph();
            send();
        });
        // drag.addEventListener('drag', function (event) {
        //     let o = event.object
        //
        //
        // })
    } else {
        // drag = new DragControls(controlCubesDrag, camera, renderer.domElement);
        // drag.addEventListener('dragend', function (event) {
        //     let o = event.object;
        //     let currPolyAtrium = o.parent;
        //     let polyPoints=[];
        //     currPolyAtrium.controlCube.forEach((c) => polyPoints.push(c.position));
        //     currPolyAtrium = gf.Segments(polyPoints, true, null, true);
        //
        //     send();
        // });
    }
}

/* ---------- animate per frame ---------- */

function draw() {

}

export {
    main,
    updateObject,
}