package main;

import Guo_Cam.CameraController;
import advancedGeometry.ZCatmullRom;
import basicGeometry.ZPoint;
import org.locationtech.jts.geom.LineString;
import processing.core.PApplet;
import processing.core.PFont;
import render.JtsRender;
import wblut.geom.WB_Coord;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * main PApplet entry for shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/5/9
 * @time 16:21
 */
public class MallNew extends PApplet {
    // utils
    private CameraController gcam;
    private WB_Render render;
    private JtsRender jtsRender;
    public PFont font;
    // pointer from screen
    private double[] pointer;

    // main
    private final ImportData input = new ImportData();
    private MallGenerator mallGenerator;
    private MallInteract mallInteract;

    // stats
    private static final int FLOOR_TOTAL = 4;
    public int FLOOR_NUM = 1;
    private String currFloorStats = "";

    // edit switches
    public boolean EDIT_NODE = false;
    public boolean EDIT_ATRIUM = false;
    public boolean EDIT_CURVE = false;
    public boolean EDIT_CELL = false;
    // display switches
    public boolean SHOW_GRID = false;
    public boolean SHOW_GRAPH = false;
    public boolean SHOW_PARTI = true;
    public boolean SHOW_EVAC = false;

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    public void setup() {
        // utils
        this.gcam = new CameraController(this);
        gcam.top();
        this.render = new WB_Render(this);
        this.jtsRender = new JtsRender(this);
        this.font = createFont("./src/main/resources/simhei.ttf", 32);
        textFont(font);
        textAlign(CENTER, CENTER);
        textSize(18);

        // import
        String importPath = "./src/main/resources/0310.3dm";
        this.input.loadData(importPath);

        // initialize generator and interact
        init();
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(33, 40, 48);

        gcam.begin3d();
        if (SHOW_GRID) {
            mallGenerator.displayGridLocal(this, render, jtsRender);
        }
        if (SHOW_GRAPH) {
            mallGenerator.displayGraphLocal(FLOOR_NUM, this, render);
        }
        if (SHOW_PARTI) {
            mallGenerator.displayPartitionLocal(FLOOR_NUM, this, jtsRender);
        }
        if (SHOW_EVAC) {
            mallGenerator.displayEvacuationLocal(this, jtsRender);
        }

        mallInteract.drawBoundaryAndNode(this, render);
        mallInteract.drawBufferCurve(this, jtsRender);
        mallInteract.drawAtrium(this, render);

        if (EDIT_CELL) {
            mallInteract.drawCellSelected(this, jtsRender);
        }

        gcam.begin2d();
        showStats();
    }

    public void showStats() {
        pushStyle();
        textAlign(LEFT);
        fill(55, 103, 171, 100);
        rect(20, 20, 300, 240);
        fill(255);
        text(currFloorStats, 40, 50);
        popStyle();
    }

    /* ------------- interact ------------- */

    public void keyPressed() {
        // display switches
        if (key == '/') {
            SHOW_GRID = !SHOW_GRID;
        }
        if (key == '*') {
            SHOW_EVAC = !SHOW_EVAC;
        }

        // initialize
        if (key == '`') {
            mallGenerator.setBufferCurve_receive(FLOOR_NUM, mallInteract.getBufferCurve_interact());
            mallGenerator.generateEvacuation2();
            mallGenerator.generateSubdivision(FLOOR_NUM);

            this.currFloorStats = mallGenerator.getFloorStats(FLOOR_NUM);
        }
        if (key == '.') {
            mallInteract.clickUpdateShop();
            mallGenerator.setShopCells_receive(FLOOR_NUM, mallInteract.getCellPolys_interact());
        }

        // floor switches
        if (key == '=') {
            FLOOR_NUM = (FLOOR_NUM % FLOOR_TOTAL) + 1;
            generatorGraphBuffer();
            println("current floor: " + FLOOR_NUM);
        }
        if (key == '-') {
            if (FLOOR_NUM == 1) {
                FLOOR_NUM = FLOOR_TOTAL;
            } else {
                FLOOR_NUM--;
            }
            generatorGraphBuffer();
            println("current floor: " + FLOOR_NUM);
        }

        // edit switches
        if (key == '0') {
            // 停止所有可编辑物体
            EDIT_NODE = false;
            EDIT_ATRIUM = false;
            EDIT_CURVE = false;
            EDIT_CELL = false;
            println("disable all edit");
        }
        if (key == '1') {
            // graph node可编辑，可添加中庭，中庭可跟随平移
            EDIT_NODE = true;
            EDIT_ATRIUM = false;
            EDIT_CURVE = false;
            EDIT_CELL = false;
            SHOW_GRAPH = true;
            generatorGraphBuffer();
            mallInteract.disableAtriumEdit();
            println("edit traffic node");
        }
        if (key == '2') {
            // graph node不可编辑，显示中庭控制node，可编辑
            EDIT_NODE = false;
            EDIT_ATRIUM = true;
            EDIT_CURVE = false;
            EDIT_CELL = false;
            mallInteract.enableAtriumEdit();
            println("edit atrium shape");
        }
        if (key == '3') {
            EDIT_NODE = false;
            EDIT_ATRIUM = false;
            EDIT_CURVE = true;
            EDIT_CELL = false;
            println("edit curve shape");
        }
        if (key == '4') {
            EDIT_NODE = false;
            EDIT_ATRIUM = false;
            EDIT_CURVE = false;
            EDIT_CELL = true;
            mallInteract.setCellPolys_interact(mallGenerator.getShopCells(FLOOR_NUM));
            println("edit shop cells");
        }

        // add & remove control points
        if (key == 'q' || key == 'Q') {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            mallInteract.addInnerNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
        }
        if (key == 'w' || key == 'W') {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            mallInteract.removeInnerNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
        }
        if (key == 'e' || key == 'E') {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            mallInteract.addEntryNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
        }
        if (key == 'r' || key == 'R') {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            mallInteract.removeEntryNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
        }

        // add & remove atrium
        if (key == 'a' || key == 'A') {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            mallInteract.addOrRemoveAtrium(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
        }
    }

    public void mouseDragged() {
        if (mouseButton == RIGHT) {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);

            if (EDIT_NODE) {
                mallInteract.dragUpdateNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
            } else if (EDIT_ATRIUM) {
                mallInteract.dragUpdateAtrium(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
            } else if (EDIT_CURVE) {

            }
        }
    }

    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            if (EDIT_NODE) {
                generatorGraphBuffer();
            } else if (EDIT_ATRIUM) {
                generatorGraphBuffer();
            } else if (EDIT_CURVE) {

            }
        }
    }

    public void mouseClicked() {
        if (mouseButton == LEFT) {

            if (EDIT_CELL) {
                pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
                mallInteract.selectShopCell(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
            }
        }
    }

    /* ------------- member function ------------- */

    public void init() {
        this.mallGenerator = new MallGenerator();
        mallGenerator.setBoundary_receive(input.getInputBoundary());
        mallGenerator.setInnerNode_receive(null);
        mallGenerator.setEntryNode_receive(null);
        mallGenerator.setPolyAtrium_receive(null);
        mallGenerator.init();

        this.mallInteract = new MallInteract(
                input.getInputBoundary(),
                input.getInputInnerNodes(),
                input.getInputEntries()
        );
    }

    public void generatorGraphBuffer() {
        // settings
        mallGenerator.setInnerNode_receive(mallInteract.getInnerNode_interact());
        mallGenerator.setEntryNode_receive(mallInteract.getEntryNode_interact());
        mallGenerator.setPolyAtrium_receive(mallInteract.getAtrium_interact());

        // generating
        mallGenerator.generateGraphAndBuffer(FLOOR_NUM, MallConst.BUFFER_DIST, FLOOR_NUM == 1 ? MallConst.CURVE_POINTS_1 : MallConst.CURVE_POINTS_2);

        // converting results
        List<LineString> bufferCurves = new ArrayList<>();
        List<List<WB_Coord>> bufferControlPoints = mallGenerator.getBufferControlPoints(FLOOR_NUM);
        for (List<WB_Coord> coords : bufferControlPoints) {
            List<ZPoint> points = new ArrayList<>();
            for (WB_Coord c : coords) {
                points.add(new ZPoint(c));
            }

            ZCatmullRom catmullRom = new ZCatmullRom(points, 5, FLOOR_NUM != 1);
            bufferCurves.add(catmullRom.getAsLineString());
        }

        mallInteract.setBufferCurve_interact(bufferCurves);
    }
}
