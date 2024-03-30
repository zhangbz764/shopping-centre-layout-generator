package main;

import basicGeometry.ZLine;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import guo_cam.CameraController;
import igeo.ICurve;
import igeo.IG;
import igeo.IVec;
import mallIO.ImportData;
import mallParameters.MallConst;
import mallParameters.MallParam;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import processing.core.PFont;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Point;
import wblut.processing.WB_Render;

/**
 * main PApplet entry for shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/5/9
 * @time 16:21
 */
public class Mall extends PApplet {
    // utils
    private CameraController gcam;      // camera
    private WB_Render render;           // render for HE_Mesh
    private JtsRender jtsRender;        // render for jts
    private ControlP5 cp5;              // controlP5
    private int cp5H;                   // controlP5 bar height

    // pointer from screen
    private double[] pointer;           // pointer xy of mouse

    // main
    private MallGUI mallGUI;                // GUI of controlP5
    private ImportData importData;          // import data from local files
    private MallGenerator mallGenerator;    // generate function manager
    private MallInteract mallInteract;      // interact objects manager
    private MallParam mallParam;            // interact parameters manager

    // edit switches
    public int EDIT_STATUS = -1;            // current edit status (0 ~ 8)
    private boolean DARK_MODE = false;      // render mode

    // TODO: 2022/6/16 18:25 by zhangbz 
    // different floor
    public int FLOOR_NUM = 2;               // current floor number

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

        PFont font = createFont("./src/main/resources/simhei.ttf", 32);
        textFont(font);
        textAlign(CENTER, CENTER);
        textSize(14);

        // import
        this.importData = new ImportData();
        String importPath = "./src/main/resources/20220406.3dm";
        importData.loadDataShaoxing(importPath);

        // initialize core managers
        this.mallInteract = new MallInteract();
        this.mallGenerator = new MallGenerator();
        this.mallGUI = new MallGUI(font);
        this.mallParam = new MallParam();

        // GUI
        this.cp5 = new ControlP5(this);
        this.cp5H = (int) (height * 0.5 / MallConst.STATUS_NUM);
        mallGUI.initGUI(cp5, cp5H, mallParam, this);
    }

    /* ------------- draw ------------- */

    public void draw() {
        // dark mode
        background(45, 45, 45);

        // main
        gcam.begin3d();
        mallGenerator.displayLocal(this, render, jtsRender, EDIT_STATUS);
        mallInteract.displayLocal(this, render, jtsRender, EDIT_STATUS);

        // stats and info
        gcam.begin2d();
        pushStyle();
        popStyle();
        // TODO: 2022/6/17 10:37 by zhangbz
        // dark and light mode
        if (DARK_MODE) {

        } else {

        }
    }

//    /**
//     * display GUI
//     */
//    public void showGUI() {
//        // background rectangle
//        noStroke();
//        fill(55, 103, 171, 100);
//        rect(0, 0, MallConst.STATUS_W, height);
//
//        // select rectangle
//        if (EDIT_STATUS > -1) {
//            noFill();
//            stroke(83, 192, 231);
//            strokeWeight(3);
//            rect(0, cp5H * EDIT_STATUS, MallConst.STATUS_W, cp5H);
//        }
//
//        // info right-top
//
//    }

//    /**
//     * display shopping mall statistics
//     */
//    public void displayStats() {
//        textAlign(LEFT);
//
//        fill(255);
//        String stats = "本层面积" + "\n" + mallGenerator.getBoundaryAreaAsString() + "\n"
//                + "\n" + "可租赁面积" + "\n" + mallInteract.getRentArea() + "\n"
//                + "\n" + "动线长度" + "\n" + mallGenerator.getTrafficLength() + "\n" + mallGenerator.getInnerTrafficLength() + "\n"
//                ;
//        text(stats, 20, height * 0.5f + 50);
//    }

    /* ------------- interact ------------- */

    /**
     * key interact
     */
    public void keyPressed() {
        if (key == 'y') {
            DARK_MODE = !DARK_MODE;
        }

        // temp:中庭与动线
        if (key == 'k') {
            IG.init();
            ZTransform.PolygonToICurve(mallGenerator.getSiteBase().getBoundary()).layer("boundary");
            ZTransform.PolygonToICurve(mallGenerator.getMainTraffic().getMainTrafficBuffer()).layer("traffic");
            ZTransform.LineStringToICurve(mallGenerator.getMainTraffic().getMainTrafficCurve()).layer("traffic");
            for (WB_Point p : mallInteract.getTraffic_entryControllers()) {
                ZTransform.WB_CoordToIPoint(p).layer("trafficNodes");
            }
            for (WB_Point p : mallInteract.getTraffic_innerControllers()) {
                ZTransform.WB_CoordToIPoint(p).layer("trafficNodes");
            }
            for (int i = 0; i < mallInteract.getTraffic_innerControllers().size() - 1; i++) {
                new ICurve(new IVec[]{
                        new IVec(mallInteract.getTraffic_innerControllers().get(i).xf(), mallInteract.getTraffic_innerControllers().get(i).yf()),
                        new IVec(mallInteract.getTraffic_innerControllers().get(i + 1).xf(), mallInteract.getTraffic_innerControllers().get(i + 1).yf())
                }).layer("nodeLine");
            }
            new ICurve(new IVec[]{
                    new IVec(mallInteract.getTraffic_entryControllers().get(0).xf(), mallInteract.getTraffic_entryControllers().get(0).yf()),
                    new IVec(mallInteract.getTraffic_innerControllers().get(0).xf(), mallInteract.getTraffic_innerControllers().get(0).yf())
            }).layer("nodeLine");
            new ICurve(new IVec[]{
                    new IVec(mallInteract.getTraffic_entryControllers().get(1).xf(), mallInteract.getTraffic_entryControllers().get(1).yf()),
                    new IVec(mallInteract.getTraffic_innerControllers().get(mallInteract.getTraffic_innerControllers().size() - 1).xf(), mallInteract.getTraffic_innerControllers().get(mallInteract.getTraffic_innerControllers().size() - 1).yf())
            }).layer("nodeLine");

            for (Polygon a : mallInteract.getAtriumRawShapes()) {
                ZTransform.PolygonToICurve(a).layer("atrium");
            }

            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\atrium1.3dm");
        }
        if (key == 'l') {
            IG.init();

            ZTransform.PolygonToICurve(mallGenerator.getSiteBase().getBoundary()).layer("boundary");
            ZTransform.PolygonToICurve(mallGenerator.getMainTraffic().getMainTrafficBuffer()).layer("traffic");
            ZTransform.LineStringToICurve(mallGenerator.getMainTraffic().getMainTrafficCurve()).layer("traffic");
            for (WB_Point p : mallInteract.getTraffic_entryControllers()) {
                ZTransform.WB_CoordToIPoint(p).layer("trafficNodes");
            }
            for (WB_Point p : mallInteract.getTraffic_innerControllers()) {
                ZTransform.WB_CoordToIPoint(p).layer("trafficNodes");
            }
            for (int i = 0; i < mallInteract.getTraffic_innerControllers().size() - 1; i++) {
                new ICurve(new IVec[]{
                        new IVec(mallInteract.getTraffic_innerControllers().get(i).xf(), mallInteract.getTraffic_innerControllers().get(i).yf()),
                        new IVec(mallInteract.getTraffic_innerControllers().get(i + 1).xf(), mallInteract.getTraffic_innerControllers().get(i + 1).yf())
                }).layer("nodeLine");
            }
            new ICurve(new IVec[]{
                    new IVec(mallInteract.getTraffic_entryControllers().get(0).xf(), mallInteract.getTraffic_entryControllers().get(0).yf()),
                    new IVec(mallInteract.getTraffic_innerControllers().get(0).xf(), mallInteract.getTraffic_innerControllers().get(0).yf())
            }).layer("nodeLine");
            new ICurve(new IVec[]{
                    new IVec(mallInteract.getTraffic_entryControllers().get(1).xf(), mallInteract.getTraffic_entryControllers().get(1).yf()),
                    new IVec(mallInteract.getTraffic_innerControllers().get(mallInteract.getTraffic_innerControllers().size() - 1).xf(), mallInteract.getTraffic_innerControllers().get(mallInteract.getTraffic_innerControllers().size() - 1).yf())
            }).layer("nodeLine");

            for (Polygon a : mallInteract.getAtriumRawShapes()) {
                ZTransform.PolygonToICurve(a).layer("atrium");
            }

            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\atrium2.3dm");
        }

        if (key == 'j') {
            IG.init();
            ZTransform.PolygonToICurve(mallGenerator.getSiteBase().getBoundary()).layer("boundary");
            ZTransform.PolygonToICurve(mallGenerator.getPublicSpace().getPublicSpaceShape()).layer("publicSpace");
            for (int i = 0; i < mallGenerator.getPublicSpace().getAtriumCurrShapes().length; i++) {
                ZTransform.PolygonToICurve(mallGenerator.getPublicSpace().getAtriumCurrShapeN(i)).layer("atrium");
            }
            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\publicSpace.3dm");
        }

        if (key == 'u') {
            IG.init();
            ZTransform.PolygonToICurve(mallGenerator.getPublicSpace().getPublicSpaceShape()).layer("publicSpace");
            for (ZLine l : mallGenerator.getPublicSpace().getEscalatorShapes()) {
                l.createICurve().layer("shape");
            }
            for (Polygon bound : mallGenerator.getPublicSpace().getEscalatorBounds()) {
                ZTransform.PolygonToICurve(bound).layer("bound");
                IVec[] rVecs = new IVec[]{
                        new IVec(bound.getCentroid().getX(), bound.getCentroid().getY()),
                        new IVec(bound.getCentroid().getX() + MallConst.ESCALATOR_DIST_MAX, bound.getCentroid().getY())
                };
                new ICurve(rVecs).layer("radius");
            }
            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\escalator.3dm");
        }
    }

    /**
     * mouse drag interact
     *
     * @return void
     */
    public void mouseDragged() {
        if (mouseButton == LEFT) {
            pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);

            switch (EDIT_STATUS) {
                case MallConst.E_SITE_BOUNDARY:
                    mallInteract.dragUpdateBoundary(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
                case MallConst.E_TRAFFIC_ATRIUM:
                    mallInteract.dragUpdateTrafficAtriumRaw(pointer[0] + width * 0.5, pointer[1] + height * 0.5, mallGenerator.getSiteBase().getBoundary());
                    break;
                case MallConst.E_MAIN_CORRIDOR:
                    mallInteract.dragUpdateCorridor(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
                case MallConst.E_STRUCTURE_GRID:
                    mallInteract.dragUpdateGrid(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                    break;
                case (MallConst.E_EVAC_STAIRWAY):
                    mallInteract.dragUpdateEvacNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5, mallGenerator.getSiteBase().getBoundary());
                    break;
            }
        }
    }

    /**
     * mouse release interact
     *
     * @return void
     */
    public void mouseReleased() {
        if (mouseButton == LEFT) {
            if (mouseX > MallConst.CONTROLLER_W + MallConst.STATUS_W) {  // 不碰到GUI区域
                switch (EDIT_STATUS) {
                    case MallConst.E_SITE_BOUNDARY:
                        mallGenerator.updateBoundaryByNodes(mallInteract.getBoundary_controllers());
                        break;
                    case MallConst.E_TRAFFIC_ATRIUM:
                        if (mallInteract.getTrafficOrAtrium()) {
                            mallGenerator.updateTraffic(
                                    mallInteract.getTraffic_innerControllers(),
                                    mallInteract.getTraffic_entryControllers(),
                                    mallParam.trafficBufferDist
                            );
                            mallInteract.getAtriumRawManager().updateAtriumRawByTraffic(
                                    ZTransform.LineStringToWB_PolyLine(mallGenerator.getMainTraffic().getMainTrafficCurve())
                            );
                            mallInteract.getAtriumRawManager().validateAtriumRaw();
                            mallInteract.setMainTraffic_buffer(mallGenerator.getMainTraffic().getMainTrafficBuffer());
                        } else {
                            mallInteract.releaseUpdateAtriumRaw();
                        }
                        break;
                    case MallConst.E_MAIN_CORRIDOR:
                        mallGenerator.updateMainCorridorPos(mallInteract.getSelectedCorridorID(), mallInteract.getSelectedCorridorNode());
                        break;
                    case MallConst.E_STRUCTURE_GRID:
                        mallGenerator.updateGridByRect(mallInteract.getSelectedRectID(), mallInteract.getSelectedRect());
                        break;
                    case (MallConst.E_EVAC_STAIRWAY):
                        mallGenerator.updateEvacPosByID(mallInteract.getGeneratedEvacNodeIDs());
                        mallInteract.releaseUpdateEvacNode();
                        mallInteract.clearSelEvac();
                        break;
                }
            }
        }
    }

    /**
     * mouse click interact
     *
     * @return void
     */
    public void mouseClicked() {
        if (mouseButton == LEFT) {
            if (mouseX > MallConst.CONTROLLER_W + MallConst.STATUS_W) {  // 不碰到GUI
                pointer = gcam.getCoordinateFromScreenDouble(mouseX, mouseY, 0);

                switch (EDIT_STATUS) {
                    case MallConst.E_TRAFFIC_ATRIUM:
                        mallInteract.clickUpdateAtriumRaw(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                        break;
                    case MallConst.E_MAIN_CORRIDOR:
                        mallInteract.selectCorridorNode(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                        break;
                    case MallConst.E_PUBLIC_SPACE:
                        mallInteract.selectPublicSpaceNodeOrAtrium(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                        break;
                    case MallConst.E_STRUCTURE_GRID:
                        mallInteract.selectGridRect(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                        break;
                    case MallConst.E_SHOP_EDIT:
                        mallInteract.selectShopCell(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                        break;
                    case MallConst.E_ESCALATOR:
                        mallInteract.selectEscalator(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                        break;
                    case MallConst.E_EVAC_STAIRWAY:
                        mallInteract.selectStairwayShape(pointer[0] + width * 0.5, pointer[1] + height * 0.5);
                        break;
                }
            }
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

        // edit status events
        switch (id) {
            case (MallConst.E_SITE_BOUNDARY):
                if (EDIT_STATUS >= MallConst.E_SITE_BOUNDARY - 1) {
                    // edit site and boundary
                    if (EDIT_STATUS <= MallConst.E_SITE_BOUNDARY) {
                        // initialize site and boundary
                        mallGenerator.initSiteBoundary(
                                importData.getInputBlock(),
                                importData.getInputBoundary(),
                                0,
                                mallParam.siteRedLineDist,
                                mallParam.siteBufferDist
                        );
                    } else {
                        mallInteract.clearActiveEdit(EDIT_STATUS);
                    }
                    // set boundary controller points
                    mallInteract.setBoundary_controllers(mallGenerator.getBoundaryNodes());

                    this.EDIT_STATUS = MallConst.E_SITE_BOUNDARY;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit boundary");
                }
                break;
            case (MallConst.E_TRAFFIC_ATRIUM):
                if (EDIT_STATUS >= MallConst.E_TRAFFIC_ATRIUM - 1) {
                    if (EDIT_STATUS <= MallConst.E_TRAFFIC_ATRIUM) {
                        // edit main traffic and raw atriums
                        mallGenerator.initTraffic(mallParam.trafficBufferDist);

                        mallInteract.setTraffic_innerControllers(mallGenerator.getTrafficInnerNodes());
                        mallInteract.setTraffic_entryControllers(mallGenerator.getTrafficEntryNodes());

                        mallInteract.initAtriumRawManager(ZTransform.LineStringToWB_PolyLine(mallGenerator.getMainTraffic().getMainTrafficCurve()));
                        mallInteract.setMainTraffic_buffer(mallGenerator.getMainTraffic().getMainTrafficBuffer());
                    } else {
                        mallInteract.clearActiveEdit(EDIT_STATUS);
                    }
                    this.EDIT_STATUS = MallConst.E_TRAFFIC_ATRIUM;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit traffic and raw atrium");
                }
                break;
            case (MallConst.E_MAIN_CORRIDOR):
                if (EDIT_STATUS >= MallConst.E_MAIN_CORRIDOR - 1) {
                    // edit main corridor and atrium shapes
                    // make sure that raw atriums are valid (non-overlap)
                    if (mallInteract.getAtriumRawManager().getValidAtriumRaw()) {
                        if (EDIT_STATUS <= MallConst.E_MAIN_CORRIDOR) {
                            mallGenerator.setRawAtrium_receive(mallInteract.getAtriumRawShapes());
                            mallGenerator.initMainCorridor(mallParam.corridorWidth);
                            mallInteract.setCorridorNode_interact(mallGenerator.getCorridorNode());
                        } else {
                            mallInteract.clearActiveEdit(EDIT_STATUS);
                        }

                        this.EDIT_STATUS = MallConst.E_MAIN_CORRIDOR;
                        mallGUI.updateGUI(EDIT_STATUS, cp5);
                        println("STATUS " + EDIT_STATUS + ": edit main corridor");
                    }
                }
                break;
            case (MallConst.E_PUBLIC_SPACE):
                if (EDIT_STATUS >= MallConst.E_PUBLIC_SPACE - 1) {
                    if (EDIT_STATUS <= MallConst.E_PUBLIC_SPACE) {
                        mallGenerator.initPublicSpaceShape();
                        mallInteract.setPublicSpaceNode_interact(mallGenerator.getPublicSpace().getPublicSpaceShapeBufferCtrls());
                        mallInteract.setAtrium_interact(mallGenerator.getPublicSpace().getAtriumCurrShapes());
                    } else {

                    }

                    this.EDIT_STATUS = MallConst.E_PUBLIC_SPACE;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit public space");
                }
                break;
            case (MallConst.E_ESCALATOR):
                if (EDIT_STATUS >= MallConst.E_ESCALATOR - 1) {
                    mallGenerator.initEscalators();
                    mallInteract.setEscalatorBounds_interact(mallGenerator.getEscalatorBounds());
                    mallInteract.setEscalatorAtriumIDs(mallGenerator.getEscalatorAtriumIDs());

                    this.EDIT_STATUS = MallConst.E_ESCALATOR;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit escalators");
                }
                break;
            case (MallConst.E_STRUCTURE_GRID):
                if (EDIT_STATUS >= MallConst.E_STRUCTURE_GRID - 1) {
                    mallGenerator.initGrid(MallConst.STRUCTURE_GRID_NUM, MallConst.STRUCTURE_MODEL);
                    mallInteract.setRect_interact(mallGenerator.getGridRects());

                    this.EDIT_STATUS = MallConst.E_STRUCTURE_GRID;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit structure grid");
                }
                break;
            case (MallConst.E_SHOP_EDIT):
                if (EDIT_STATUS >= MallConst.E_SHOP_EDIT - 1) {
                    mallGenerator.initShopCells();
                    mallInteract.setShopCell_interact(mallGenerator.getShopCells());

                    this.EDIT_STATUS = MallConst.E_SHOP_EDIT;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit shop cells");
                }
                break;
            case (MallConst.E_EVAC_STAIRWAY):
                if (EDIT_STATUS >= MallConst.E_EVAC_STAIRWAY - 1) {
                    mallGenerator.initEvacStairwaysPos();
                    mallInteract.setGeneratedEvacNodes(
                            mallGenerator.getAuxiliarySpace().getSelGeneratorPos(),
                            mallGenerator.getAuxiliarySpace().getSelGeneratorIDs()
                    );
                    mallInteract.setAllEvacuationNodes(mallGenerator.getAuxiliarySpace().getAllGeneratorPos());

                    this.EDIT_STATUS = MallConst.E_EVAC_STAIRWAY;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit evacuations");
                }
                break;
            case (MallConst.E_WASHROOM):
                if (EDIT_STATUS >= MallConst.E_WASHROOM - 1) {
                    mallGenerator.initwashrooms();

                    this.EDIT_STATUS = MallConst.E_WASHROOM;
                    mallGUI.updateGUI(EDIT_STATUS, cp5);
                    println("STATUS " + EDIT_STATUS + ": edit washrooms");
                }
                break;
        }

        // events in each edit status
        switch (EDIT_STATUS) {
            case (MallConst.E_SITE_BOUNDARY):
                switch (id) {
                    // 0
                    case (MallConst.BUTTON_SWITCH_BOUNDARY):
                        mallInteract.switchBoundary(mallParam.siteBufferDist);
                        mallGenerator.updateSiteBaseL(
                                mallInteract.getBoundaryBase(),
                                mallParam.siteRedLineDist,
                                mallParam.siteBufferDist
                        );
                        mallInteract.setBoundary_controllers(mallGenerator.getBoundaryNodes());
                        break;
                    case (MallConst.SLIDER_REDLINE_DIST):
                    case (MallConst.SLIDER_OFFSET_DIST):
                        mallGenerator.updateSiteBaseL(
                                mallInteract.getBoundaryBase(),
                                mallParam.siteRedLineDist,
                                mallParam.siteBufferDist
                        );
                        mallInteract.setBoundary_controllers(mallGenerator.getBoundaryNodes());
                        break;
                }
                break;
            case (MallConst.E_TRAFFIC_ATRIUM):
                switch (id) {
                    // 1
//                    case (MallConst.BUTTON_DELETE_INNERNODE):
//                        break;
//                    case (MallConst.BUTTON_DELETE_ENTRYNODE):
//                        break;
                    case (MallConst.BUTTON_TRAFFIC_CONTROLLERS):
                        mallInteract.reverseTrafficOrAtriumRaw();
                        break;
                    case (MallConst.SLIDER_TRAFFIC_WIDTH):
                        if (mallInteract.getTrafficOrAtrium()) {
                            mallGenerator.updateTrafficWidth(mallParam.trafficBufferDist);
                            mallInteract.setMainTraffic_buffer(mallGenerator.getMainTraffic().getMainTrafficBuffer());
                        }
                        break;
                    case (MallConst.BUTTON_CURVE_ATRIUM):
                        mallInteract.changeAtriumRawCurve();
                        break;
                    case (MallConst.BUTTON_DELETE_ATRIUM):
                        mallInteract.removeAtriumRaw();
                        break;
                    case (MallConst.SLIDER_ATRIUM_ANGLE):
                        float angle = mallParam.atriumAngle;
                        double anglePI = 2 * Math.PI * (angle / 360);
                        mallInteract.rotateAtriumRaw(anglePI);
                        break;
                    case (MallConst.SLIDER_ATRIUM_AREA):
                        mallInteract.changeAtriumRawArea(mallParam.atriumArea);
                        break;
                    case (MallConst.LIST_ATRIUM_FACTORY):
                        int atriumTypeNum = (int) theEvent.getController().getValue();
                        mallInteract.setSelectedAtriumRawType(atriumTypeNum);
                        break;
                }
                break;
            case (MallConst.E_MAIN_CORRIDOR):
                switch (id) {
                    // 2
                    case (MallConst.SLIDER_CORRIDOR_WIDTH):
                        mallGenerator.updateMainCorridorWidth(mallInteract.getSelectedCorridorID(), mallParam.corridorWidth);
                        break;
                }
                break;
            case (MallConst.E_PUBLIC_SPACE):
                int atriumID = mallInteract.getSelectedAtriumID();
                switch (id) {
                    // 3
                    case (MallConst.BUTTON_DELETE_PUBLIC_NODE):
                        mallInteract.removePublicSpaceNode();
                        mallGenerator.updatePublicSpace(mallInteract.getPublicSpaceNode_interact());
                        break;
                    case (MallConst.SLIDER_BUFFER_DIST):
                        mallGenerator.updatePublicSpaceBuffer(mallParam.publicSpaceBufferDist);
                        mallInteract.setPublicSpaceNode_interact(mallGenerator.getPublicSpace().getPublicSpaceShapeBufferCtrls());
                        break;
                    case (MallConst.BUTTON_ATRIUM_ROUND):
                        if (atriumID > -1) {
                            mallGenerator.switchAtriumRoundType(atriumID);
                            mallInteract.setChangedAtrium(mallGenerator.getPublicSpace().getAtriumCurrShapeN(atriumID), atriumID);
                        }
                        break;
                    case (MallConst.SLIDER_ROUND_RADIUS):
                        if (atriumID > -1) {
                            mallGenerator.updateAtriumRoundRadius(atriumID, mallParam.atriumRoundRadius);
                            mallInteract.setChangedAtrium(mallGenerator.getPublicSpace().getAtriumCurrShapeN(atriumID), atriumID);
                        }
                        break;
                    case (MallConst.SLIDER_SMOOTH_TIMES):
                        if (atriumID > -1) {
                            mallGenerator.updateAtriumSmoothTimes(atriumID, mallParam.atriumSmoothTimes);
                            mallInteract.setChangedAtrium(mallGenerator.getPublicSpace().getAtriumCurrShapeN(atriumID), atriumID);
                        }
                        break;
                }
                break;
            case (MallConst.E_ESCALATOR):
                switch (id) {
                    // 6
                    case (MallConst.BUTTON_UPDATE_ESCALATOR):
                        mallGenerator.updateEscalatorPos(mallInteract.getSelectedEscalatorAtriumID());
                        mallInteract.setEscalatorBound_interact(
                                mallGenerator.getEscalatorBoundN(mallInteract.getSelectedEscalatorAtriumID())
                        );
                        break;
                }
                break;
            case (MallConst.E_STRUCTURE_GRID):
                switch (id) {
                    // 4
                    case (MallConst.BUTTON_GRID_MODEL):
                        mallGenerator.switchGridModulus();
                        break;
                    case (MallConst.LIST_GRID_NUM):
                        int gridNum = (int) theEvent.getController().getValue() + 1;
                        mallGenerator.initGrid(gridNum, MallConst.STRUCTURE_MODEL);
                        mallInteract.setRect_interact(mallGenerator.getGridRects());
                        mallInteract.unselectGridRect();
                        break;
                }
                break;
            case (MallConst.E_SHOP_EDIT):
                switch (id) {
                    // 5
                    case (MallConst.BUTTON_UNION_CELLS):
                        mallInteract.unionShopCell();
                        mallGenerator.setShopCells(mallInteract.getShopCell_interact());
                        break;
                    case (MallConst.BUTTON_SPLIT_CELLS):
                        mallGenerator.splitShopCell(mallInteract.getSelectedShopCellID());
                        mallInteract.setShopCell_interact(mallGenerator.getShopCells());
                        break;
                }
                break;
            case (MallConst.E_EVAC_STAIRWAY):
                switch (id) {
                    case (MallConst.BUTTON_EVAC_MODEL):
                        mallGenerator.generateEvacShape();
                        mallInteract.setStairway_interact(mallGenerator.getAuxiliarySpace().getStairwayShapePoly());
                        break;
                    case (MallConst.BUTTON_EVAC_DIR):
                        if (mallInteract.getSelectedStairwayID() > -1) {
                            mallGenerator.switchEvacDirection(mallInteract.getSelectedStairwayID());
                            mallInteract.clearSelecteStairway();
                        }
                        break;
                }
                break;
        }
    }
}
