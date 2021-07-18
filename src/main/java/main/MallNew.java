package main;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import guo_cam.CameraController;
import processing.core.PApplet;
import processing.core.PFont;
import render.JtsRender;
import wblut.processing.WB_Render;

import java.util.ArrayList;

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
    private ControlP5 cp5;
    private int cp5H;

    // pointer from screen
    private double[] pointer;

    // main
    private final ImportData input = new ImportData();
    private MallGenerator mallGenerator;
    private MallInteract mallInteract;
//    private MallParam mallParam;

    // stats
    public int FLOOR_NUM = 2;
    private String currFloorStats = "指标显示……"
//            +"\n" +
//            "\n" + "本层面积：" +
//            "\n" + "可租赁面积：" +
//            "\n" + "动线长度：" +
//            "\n" + "得铺率：" +
//            "\n" + "小铺率"
            ;

    // edit switches
    public int EDIT_STATUS = -1;

    /* ------------- settings ------------- */

    public void settings() {
        size(1600, 900, P3D);
    }

    /* ------------- setup ------------- */

    public void setup() {
        // utils
        this.gcam = new CameraController(this);
        gcam.top();

        this.render = new WB_Render(this);
        this.jtsRender = new JtsRender(this);

        PFont font = createFont("./src/main/resources/simhei.ttf", 32);
        textFont(font);
        textAlign(CENTER, CENTER);
        textSize(18);

        this.cp5 = new ControlP5(this);
        this.cp5H = (int) (height * 0.5 / 9);
        initCp5Button();

        // import
        String importPath = "./src/main/resources/0310.3dm";
        this.input.loadData(importPath);
        // initialize generator and interact
        this.mallInteract = new MallInteract();
        this.mallGenerator = new MallGenerator();
//        this.mallParam = new MallParam();
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(33, 40, 48);

        gcam.begin3d();
        mallGenerator.displayLocal(this, render, jtsRender, EDIT_STATUS, FLOOR_NUM);
        mallInteract.displayLocal(this, render, jtsRender, EDIT_STATUS);


        gcam.begin2d();
        pushStyle();
        showGUI();
        showStats();
        popStyle();
    }

    /**
     * display GUI
     */
    public void showGUI() {
        // background rectangle
        noStroke();
        fill(55, 103, 171, 100);
        rect(0, 0, MallConst.STATUS_W, height);

        // select rectangle
        if (EDIT_STATUS > -1) {
            noFill();
            stroke(83, 192, 231);
            strokeWeight(3);
            rect(0, cp5H * EDIT_STATUS, MallConst.STATUS_W, cp5H);
        }
    }

    /**
     * display shopping mall statistics
     */
    public void showStats() {
        textAlign(LEFT);

//        stroke(255);
//        strokeWeight(3);
//        line(0, height * 0.5f, 300, height * 0.5f);
        fill(255);
        text(currFloorStats, 20, height * 0.5f + 50);
//        text("stats start here", 40, height * 0.5f + 50);
    }

    /* ------------- interact ------------- */

    /**
     * key interact
     *
     * @param
     * @return void
     */
    public void keyPressed() {
//        // initialize
//        if (key == '`') {
//            mallGenerator.setBufferCurve_receive(FLOOR_NUM, mallInteract.getBufferCurve_interact());
//            mallGenerator.generateEvacuation2();
//            mallGenerator.generateSubdivision(FLOOR_NUM);
//
//            this.currFloorStats = mallGenerator.getFloorStats(FLOOR_NUM);
//        }
//        if (key == '.') {
//            mallInteract.clickUpdateShop();
//            mallGenerator.setShopCells_receive(FLOOR_NUM, mallInteract.getCellPolys_interact());
//        }

        // floor switches
        if (key == '=') {
            FLOOR_NUM = (FLOOR_NUM % MallConst.FLOOR_TOTAL) + 1;
//            generatorGraphBuffer();
            println("current floor: " + FLOOR_NUM);
        }
        if (key == '-') {
            if (FLOOR_NUM == 1) {
                FLOOR_NUM = MallConst.FLOOR_TOTAL;
            } else {
                FLOOR_NUM--;
            }
//            generatorGraphBuffer();
            println("current floor: " + FLOOR_NUM);
        }

        // add & remove control points
//        if (key == 'q' || key == 'Q') {
//            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
//            mallInteract.addInnerNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
//        }
        if (key == 'w' || key == 'W') {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            mallInteract.removeInnerNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
        }
//        if (key == 'e' || key == 'E') {
//            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
//            mallInteract.addEntryNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
//        }
        if (key == 'r' || key == 'R') {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
            mallInteract.removeEntryNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
        }


        if (key == '3') {
            println("edit curve shape");
        }
//        if (key == '4') {
//            mallInteract.setCellPolys_interact(mallGenerator.getShopCells(FLOOR_NUM));
//            println("edit shop cells");
//        }
    }

    /**
     * mouse drag interact
     *
     * @param
     * @return void
     */
    public void mouseDragged() {
        if (mouseButton == LEFT) {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);

            switch (EDIT_STATUS) {
                case MallConst.E_SITE_BOUNDARY:
                    mallInteract.dragUpdateBoundary(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
                case MallConst.E_MAIN_TRAFFIC:
                    mallInteract.dragUpdateNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
                case MallConst.E_RAW_ATRIUM:
                    mallInteract.dragUpdateAtrium(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
                case MallConst.E_STRUCTURE_GRID:
                    mallInteract.dragUpdateGrid(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
            }
        }
    }

    /**
     * mouse release interact
     *
     * @param
     * @return void
     */
    public void mouseReleased() {
        if (mouseButton == LEFT) {
            switch (EDIT_STATUS) {
                case MallConst.E_MAIN_TRAFFIC:
                    mallGenerator.setInnerNode_receive(mallInteract.getInnerNode_interact());
                    mallGenerator.setEntryNode_receive(mallInteract.getEntryNode_interact());
                    mallGenerator.updateTraffic(MallConst.TRAFFIC_BUFFER_DIST);
                    break;
                case MallConst.E_RAW_ATRIUM:
                    mallInteract.releaseUpdateAtrium();
                    break;
                case MallConst.E_STRUCTURE_GRID:
                    mallGenerator.updateGridByRect(mallInteract.getSelectedRectID(), mallInteract.getSelectedRect());
                    break;
            }
        }
    }

    /**
     * mouse click interact
     *
     * @param
     * @return void
     */
    public void mouseClicked() {
        if (mouseButton == LEFT) {
            if (mouseX > 300) {  // 不碰到GUI
                if (EDIT_STATUS == MallConst.E_RAW_ATRIUM) {
                    pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
                    mallInteract.clickUpdateAtrium(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                } else if (EDIT_STATUS == MallConst.E_STRUCTURE_GRID) {
                    pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
                    mallInteract.selectGridRect(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                }
            }

//            if (EDIT_CELL) {
//                pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);
//                mallInteract.selectShopCell(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
//            }
        }
    }

    /**
     * controlP5 event
     *
     * @param theEvent ControlEvent
     * @return void
     */
    public void controlEvent(ControlEvent theEvent) {
        int id = theEvent.getController().getId();
//        println("dsdsd " + id);

        switch (id) {
            // edit status
            case (MallConst.E_SITE_BOUNDARY):
                if (EDIT_STATUS >= MallConst.E_SITE_BOUNDARY - 1) {
                    // 编辑外轮廓
                    if (EDIT_STATUS == MallConst.E_SITE_BOUNDARY - 1) {
                        mallInteract.initSiteBoundary(
                                input.getInputSite(),
                                input.getInputBoundary(),
                                MallConst.SITE_REDLINE_DIST,
                                MallConst.SITE_BUFFER_DIST
                        );
                    } else {
                        mallInteract.updateSiteBoundary(
                                mallGenerator.getSite_receive(),
                                mallGenerator.getBoundary_receive()
                        );
                    }

                    mallInteract.updateStatus0GUI(cp5, 0);
                    this.EDIT_STATUS = MallConst.E_SITE_BOUNDARY;
                    println("edit boundary");
                }
                break;
            case (MallConst.E_MAIN_TRAFFIC):
                if (EDIT_STATUS >= MallConst.E_MAIN_TRAFFIC - 1) {
                    // 外轮廓不可编辑，编辑主路径
                    mallGenerator.setSite_receive(mallInteract.getSite());
                    mallGenerator.setBoundary_receive(mallInteract.getBoundary());
                    mallGenerator.initTraffic(MallConst.TRAFFIC_BUFFER_DIST);
                    mallInteract.setInnerNode_interact(mallGenerator.getTrafficInnerNodes());
                    mallInteract.setEntryNode_interact(mallGenerator.getTrafficEntryNodes());

                    mallInteract.updateStatus1GUI(cp5, cp5H);
                    this.EDIT_STATUS = MallConst.E_MAIN_TRAFFIC;
                    println("edit traffic");
                }
                break;
            case (MallConst.E_RAW_ATRIUM):
                if (EDIT_STATUS >= MallConst.E_RAW_ATRIUM - 1) {
                    // 开始添加原始中庭形状并编辑
                    mallInteract.setMainTraffic_interact(mallGenerator.getMainTrafficBuffer());
                    mallInteract.setRawAtriums(new ArrayList<>());

                    mallInteract.updateStatus2GUI(cp5, cp5H * 2);
                    this.EDIT_STATUS = MallConst.E_RAW_ATRIUM;
                    println("edit raw atrium");
                }
                break;
            case (MallConst.E_PUBLIC_SPACE):
                if (EDIT_STATUS >= MallConst.E_PUBLIC_SPACE - 1) {
                    mallGenerator.setRawAtrium_receive(mallInteract.getRawAtriumShapes());
                    mallGenerator.updatePublicSpace();

                    mallInteract.updateStatus3GUI(cp5, cp5H * 3);
                    this.EDIT_STATUS = MallConst.E_PUBLIC_SPACE;
                    println("edit public space");
                }
                break;
            case (MallConst.E_STRUCTURE_GRID):
                if (EDIT_STATUS >= MallConst.E_STRUCTURE_GRID - 1) {
                    mallGenerator.initGrid(MallConst.STRUCTURE_GRID_NUM, MallConst.STRUCTURE_DIST);
                    mallInteract.setRect_interact(mallGenerator.getGridRects());

                    mallInteract.updateStatus4GUI(cp5, cp5H * 4);
                    this.EDIT_STATUS = MallConst.E_STRUCTURE_GRID;
                    println("edit structure grid");
                }
                break;
            case (MallConst.E_SHOP_EDIT):
                if (EDIT_STATUS >= MallConst.E_SHOP_EDIT - 1) {
                    mallGenerator.initShopCells(FLOOR_NUM);

                    mallInteract.updateStatus5GUI(cp5, cp5H * 5);
                    this.EDIT_STATUS = MallConst.E_SHOP_EDIT;
                    println("edit shop cells");
                }
                break;
            case (MallConst.E_MAIN_CORRIDOR):
                if (EDIT_STATUS >= MallConst.E_MAIN_CORRIDOR - 1) {

                }
                break;
            case (MallConst.E_ESCALATOR):
                if (EDIT_STATUS >= MallConst.E_ESCALATOR - 1) {

                }
                break;
            case (MallConst.E_EVACUATION):
                if (EDIT_STATUS >= MallConst.E_EVACUATION - 1) {

                }
                break;

            // function controllers
            case (MallConst.BUTTON_SWITCH_BOUNDARY):
                mallInteract.switchBoundary(MallConst.SITE_BUFFER_DIST);
                break;
            case (MallConst.SLIDER_REDLINE_DIST):
                float redLine = theEvent.getController().getValue();
                float buffer = cp5.getController("SITE BUFFER DIST").getValue();
                mallInteract.initSiteBoundary(
                        input.getInputSite(),
                        input.getInputBoundary(),
                        redLine,
                        buffer
                );
                break;
            case (MallConst.SLIDER_SITE_BUFFER):
                float buffer_ = theEvent.getController().getValue();
                float redLine_ = cp5.getController("SITE REDLINE DIST").getValue();
                mallInteract.initSiteBoundary(
                        input.getInputSite(),
                        input.getInputBoundary(),
                        redLine_,
                        buffer_
                );
                break;

            case (MallConst.BUTTON_DELETE_INNERNODE):
                break;
            case (MallConst.BUTTON_DELETE_ENTRYNODE):
                break;

            case (MallConst.BUTTON_CURVE_ATRIUM):
                mallInteract.changeAtriumCurve();
                break;
            case (MallConst.BUTTON_DELETE_ATRIUM):
                mallInteract.removeAtrium();
                break;
            case (MallConst.SLIDER_ATRIUM_ANGLE):
                float angle = theEvent.getController().getValue();
                double anglePI = 2 * Math.PI * (angle / 360);
                mallInteract.rotateAtrium(anglePI);
                break;
            case (MallConst.SLIDER_ATRIUM_AREA):
                float area = theEvent.getController().getValue();
                mallInteract.changeAtriumArea(area);
                break;
            case (MallConst.LIST_ATRIUM_FACTORY):
                int atriumTypeNum = (int) theEvent.getController().getValue();
                mallInteract.setSelectedAtriumType(atriumTypeNum);
                break;
        }
    }

    /* ------------- member function ------------- */

    /**
     * initialize 9 edit status buttons
     *
     * @return void
     */
    private void initCp5Button() {
        int cp5H = (int) (height * 0.5 / 9);
        cp5.addButton("SITE & BOUNDARY")
                .setPosition(0, 0)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_SITE_BOUNDARY)
        ;
        cp5.addButton("MAIN TRAFFIC")
                .setPosition(0, cp5H)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_MAIN_TRAFFIC)
        ;
        cp5.addButton("RAW ATRIUM")
                .setPosition(0, cp5H * 2)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_RAW_ATRIUM)
        ;
        cp5.addButton("PUBLIC SPACE")
                .setPosition(0, cp5H * 3)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_PUBLIC_SPACE)
        ;
        cp5.addButton("STRUCTURE GRID")
                .setPosition(0, cp5H * 4)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_STRUCTURE_GRID)
        ;
        cp5.addButton("SHOP EDIT")
                .setPosition(0, cp5H * 5)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_SHOP_EDIT)
        ;
        cp5.addButton("MAIN CORRIDOR")
                .setPosition(0, cp5H * 6)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_MAIN_CORRIDOR)
        ;
        cp5.addButton("ESCALATOR")
                .setPosition(0, cp5H * 7)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_ESCALATOR)
        ;
        cp5.addButton("EVACUATION")
                .setPosition(0, cp5H * 8)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_EVACUATION)
        ;
    }
}
