package main;

import Guo_Cam.CameraController;
import advancedGeometry.ZCatmullRom;
import basicGeometry.ZPoint;
import org.locationtech.jts.geom.LineString;
import processing.core.PApplet;
import processing.core.PFont;
import render.JtsRender;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
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
    private MallParam mallParam;

    // stats
    public int FLOOR_NUM = 2;
    private String currFloorStats = "";

    // edit switches
    public int EDIT_STATUS = -1;

    public boolean EDIT_BOUNDARY = false;
    public boolean EDIT_TRAFFIC = false;
    public boolean EDIT_ATRIUM = false;
    public boolean EDIT_CURVE = false;
    public boolean EDIT_CELL = false;
    public boolean EDIT_BRIDGE = false;

    /* ------------- settings ------------- */

    public void settings() {
        size(1920, 1080, P3D);
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
        this.mallInteract = new MallInteract();
        this.mallGenerator = new MallGenerator();
        this.mallParam = new MallParam();
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(33, 40, 48);

        gcam.begin3d();
        mallInteract.displayLocal(this, render, jtsRender, EDIT_STATUS);
        mallGenerator.displayLocal(this, render, jtsRender, EDIT_STATUS);

//        if (SHOW_SITE) {
//            mallInteract.drawSiteBoundary(this, render);
//        }
//        if (SHOW_GRID) {
//            mallGenerator.displayGridLocal(this, render, jtsRender);
//        }
//        if (SHOW_GRAPH) {
//            mallGenerator.displayGraphLocal(FLOOR_NUM, this, render);
//        }
//        if (SHOW_PARTI) {
//            mallGenerator.displayPartitionLocal(FLOOR_NUM, this, jtsRender);
//        }
//        if (SHOW_EVAC) {
//            mallGenerator.displayEvacuationLocal(this, jtsRender);
//        }

//        mallInteract.drawBufferCurve(this, jtsRender);
//        mallInteract.drawAtrium(this, render);

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
//        if (key == '/') {
//            SHOW_GRID = !SHOW_GRID;
//        }
//        if (key == '*') {
//            SHOW_EVAC = !SHOW_EVAC;
//        }

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
            FLOOR_NUM = (FLOOR_NUM % MallConst.FLOOR_TOTAL) + 1;
            generatorGraphBuffer();
            println("current floor: " + FLOOR_NUM);
        }
        if (key == '-') {
            if (FLOOR_NUM == 1) {
                FLOOR_NUM = MallConst.FLOOR_TOTAL;
            } else {
                FLOOR_NUM--;
            }
            generatorGraphBuffer();
            println("current floor: " + FLOOR_NUM);
        }


        // edit switches
        if (key == '0') {
            // 编辑外轮廓
            mallInteract.initSiteBoundary(
                    input.getInputSite(),
                    input.getInputBoundary(),
                    mallParam.SITE_REDLINE_DIST,
                    mallParam.SITE_BUFFER_DIST
            );
            this.EDIT_STATUS = 0;
            println("edit boundary");
        }
        if (key == 's') {
            mallInteract.switchBoundary(mallParam.SITE_BUFFER_DIST);
        }

        if (key == '1') {
            // 编辑主路径
            mallGenerator.setSite_receive(mallInteract.getSite());
            mallGenerator.setBoundary_receive(mallInteract.getBoundary());
            mallGenerator.initTraffic(mallParam.TRAFFIC_DIST);
            mallInteract.setInnerNode_interact(mallGenerator.getTrafficInnerNodes());
            mallInteract.setEntryNode_interact(mallGenerator.getTrafficEntryNodes());
            this.EDIT_STATUS = 1;
            println("edit traffic");
        }

        if (key == '2') {
            // graph node不可编辑，显示中庭控制node，可编辑
            EDIT_TRAFFIC = false;
            EDIT_ATRIUM = true;
            EDIT_CURVE = false;
            EDIT_CELL = false;
            mallInteract.enableAtriumEdit();
            this.EDIT_STATUS = 2;
            println("edit atrium shape");
        }
        if (key == '3') {
            EDIT_TRAFFIC = false;
            EDIT_ATRIUM = false;
            EDIT_CURVE = true;
            EDIT_CELL = false;
            println("edit curve shape");
        }
        if (key == '4') {
            EDIT_TRAFFIC = false;
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

            switch (EDIT_STATUS) {
                case 0:
                    mallInteract.dragUpdateBoundary(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
                case 1:
                    mallInteract.dragUpdateNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
            }


            if (EDIT_STATUS == 0) {
            } else if (EDIT_TRAFFIC) {
                mallInteract.dragUpdateNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
            } else if (EDIT_ATRIUM) {
                mallInteract.dragUpdateAtrium(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
            } else if (EDIT_CURVE) {

            }
        }
    }

    public void mouseReleased() {
        if (mouseButton == RIGHT) {
            switch (EDIT_STATUS) {
                case 0:
                    break;
                case 1:
                    mallGenerator.generateTraffic(mallInteract.getTrafficControls().toArray(new WB_Point[0]), mallParam.TRAFFIC_DIST);
            }


            if (EDIT_BOUNDARY) {

            } else if (EDIT_TRAFFIC) {
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

    public void generatorGraphBuffer() {
        // settings
        mallGenerator.setInnerNode_receive(mallInteract.getInnerNode_interact());
        mallGenerator.setEntryNode_receive(mallInteract.getEntryNode_interact());
        mallGenerator.setPolyAtrium_receive(mallInteract.getAtrium_interact());

        // generating
        mallGenerator.generateGraphAndBuffer(FLOOR_NUM, mallParam.TRAFFIC_DIST, FLOOR_NUM == 1 ? MallConst.CURVE_POINTS_1 : MallConst.CURVE_POINTS_2);

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
