package main;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import controlP5.ControlP5;
import controlP5.DropdownList;
import mallElementNew.AtriumFactory;
import mallElementNew.AtriumNew;
import mallElementNew.Shop;
import math.ZGeoMath;
import org.locationtech.jts.geom.*;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * geometries to interact on the local canvas
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/5/12
 * @time 11:20
 */
public class MallInteract {
    private String[] activeControllers = new String[]{};

    // 场地&建筑轮廓
    private WB_Polygon site;                // 场地轮廓
    private WB_Polygon redLine;             // 场地红线
    private WB_Polygon boundary;            // 建筑轮廓
    private int boundaryBase = 0;           // 生成建筑轮廓的场地基点序号
    private List<WB_Point> boundaryNode_interact; // 建筑轮廓控制点

    // 主路径
    private List<WB_Point> innerNode_interact;       // 动线中部控制点
    private List<WB_Point> entryNode_interact;       // 动线端头控制点

    // 中庭
    private int selectedAtriumType = -1;
    private Polygon mainTraffic_interact;
    private List<AtriumNew> rawAtriums;
    private AtriumNew selectedAtrium;

    private List<LineString> bufferCurve_interact;   // 动线边界曲线（不同层）
    private List<WB_Point> bufferCurveControl_interact;

    private List<Polygon> cellPolys_interact;     // 商铺剖分多边形（不同层）
    private List<Polygon> cellPolys_selected;

    /* ------------- constructor ------------- */

    public MallInteract() {

    }

    /* ------------- controlP5 GUI ------------- */

    /**
     * remove current GUIs
     *
     * @param cp5 controlP5
     * @return void
     */
    private void removeGUI(ControlP5 cp5) {
        for (String s : activeControllers) {
            cp5.remove(s);
        }
    }

    /**
     * update status 0 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus0GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        cp5.addButton("SWITCH BOUNDARY")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_SWITCH_BOUNDARY)
        ;
        cp5.addSlider("SITE REDLINE DIST")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_REDLINE_DIST)
                .setRange(0, MallConst.SITE_REDLINE_DIST * 2)
                .setValue(MallConst.SITE_REDLINE_DIST)
        ;
        cp5.addSlider("SITE BUFFER DIST")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_SITE_BUFFER)
                .setRange(MallConst.SITE_BUFFER_MIN, MallConst.SITE_BUFFER_MAX)
                .setValue(MallConst.SITE_BUFFER_DIST)
        ;
        this.activeControllers = new String[]{
                "SWITCH BOUNDARY",
                "SITE REDLINE DIST",
                "SITE BUFFER DIST"
        };
    }

    /**
     * update status 1 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus1GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        cp5.addButton("DELETE INNER NODE")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_INNERNODE)
        ;
        cp5.addButton("DELETE ENTRY NODE")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_ENTRYNODE)
        ;
        cp5.addSlider("MAIN TRAFFIC WIDTH")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_TRAFFIC_WIDTH)
                .setRange(MallConst.TRAFFIC_BUFFER_DIST - 2, MallConst.TRAFFIC_BUFFER_DIST + 2)
                .setValue(MallConst.TRAFFIC_BUFFER_DIST)
        ;
        this.activeControllers = new String[]{
                "DELETE INNER NODE",
                "DELETE ENTRY NODE",
                "MAIN TRAFFIC WIDTH"
        };
    }

    /**
     * update status 2 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus2GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        // curve button
        cp5.addButton("CURVE ATRIUM")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_CURVE_ATRIUM)
        ;
        // delete button
        cp5.addButton("DELETE ATRIUM")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_ATRIUM)
        ;
        // angle slider
        cp5.addSlider("ATRIUM ANGLE")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ATRIUM_ANGLE)
                .setRange(0, 360)
                .setValue(0)
        ;
        // area slider
        cp5.addSlider("ATRIUM AREA")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 3)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_TRAFFIC_WIDTH)
                .setRange(MallConst.ATRIUM_AREA_MIN, MallConst.ATRIUM_AREA_MAX)
                .setValue(MallConst.ATRIUM_AREA_INIT)
        ;
        // atrium type DropdownList
        DropdownList ddl = cp5.addDropdownList("ATRIUM FACTORY")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 4)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H * 9)
                .setId(MallConst.LIST_ATRIUM_FACTORY);
        ddl.setItemHeight(MallConst.CONTROLLER_H);
        ddl.setBarHeight(MallConst.CONTROLLER_H);
        ddl.setCaptionLabel("ATRIUM FACTORY");
        ddl.getCaptionLabel().getStyle().marginTop = 3;
        ddl.getCaptionLabel().getStyle().marginLeft = 3;
        ddl.getValueLabel().getStyle().marginTop = 3;
        ddl.addItem("TRIANGLE", MallConst.ITEM_A_TRIANGLE);
        ddl.addItem("SQUARE", MallConst.ITEM_A_SQUARE);
        ddl.addItem("TRAPEZOID", MallConst.ITEM_A_TRAPEZOID);
        ddl.addItem("PENTAGON", MallConst.ITEM_A_PENTAGON);
        ddl.addItem("HEXAGON1", MallConst.ITEM_A_HEXAGON1);
        ddl.addItem("HEXAGON2", MallConst.ITEM_A_HEXAGON2);
        ddl.addItem("L-SHAPE", MallConst.ITEM_A_L_SHAPE);
        ddl.addItem("OCTAGON", MallConst.ITEM_A_OCTAGON);

        this.activeControllers = new String[]{
                "CURVE ATRIUM",
                "DELETE ATRIUM",
                "ATRIUM ANGLE",
                "ATRIUM AREA",
                "ATRIUM FACTORY"
        };
    }

    /**
     * update status 3 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus3GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        // buffer distance slider
        cp5.addSlider("BUFFER DIST")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_BUFFER_DIST)
                .setRange(0, 3)
                .setValue(0)
        ;
        this.activeControllers = new String[]{
                "BUFFER DIST"
        };
    }

    /**
     * update status 4 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus4GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        // grid number list
        cp5.addSlider("COLUMN DIST")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_BUFFER_DIST)
                .setRange(7, 9)
                .setValue(MallConst.STRUCTURE_DIST)
        ;
        cp5.addDropdownList("GRID NUM")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H * 5)
                .setId(MallConst.LIST_GRID_NUM)
                .setItemHeight(MallConst.CONTROLLER_H)
                .setBarHeight(MallConst.CONTROLLER_H)
                .setCaptionLabel("GRID NUM")
                .addItem("1", 1)
                .addItem("2", 2)
                .addItem("3", 3)
                .addItem("4", 4);
        this.activeControllers = new String[]{
                "GRID NUM"
        };
    }

    /* ------------- site boundary interact ------------- */

    /**
     * given site and boundary straightly or given a quad site and generate boundary automatically
     *
     * @param _site     input site
     * @param _boundary input boundary
     * @return void
     */
    public void initSiteBoundary(WB_Polygon _site, WB_Polygon _boundary, double redLineDist, double siteBufferDist) {
        this.site = ZTransform.validateWB_Polygon(ZGeoMath.polygonFaceUp(_site));
        if (_boundary != null) {
            this.boundary = ZTransform.validateWB_Polygon(ZGeoMath.polygonFaceUp(_boundary));
            this.boundaryNode_interact = new ArrayList<>();
            for (int i = 0; i < boundary.getNumberOfPoints(); i++) {
                boundaryNode_interact.add(boundary.getPoint(i));
            }
        } else {
            WB_Polygon redLineSite = ZFactory.wbgf.createBufferedPolygons2D(
                    _site, -1 * redLineDist
            ).get(0);
            this.redLine = ZGeoMath.polygonFaceUp(ZTransform.validateWB_Polygon(redLineSite));

            this.boundary = generateBoundary(redLine, boundaryBase, siteBufferDist);
            this.boundaryNode_interact = new ArrayList<>();
            for (int i = 0; i < boundary.getNumberOfPoints(); i++) {
                boundaryNode_interact.add(boundary.getPoint(i));
            }
        }
    }

    public void updateSiteBoundary(WB_Polygon _site, WB_Polygon _boundary) {
        this.site = _site;
        this.boundary = _boundary;
        this.boundaryNode_interact = new ArrayList<>();
        for (int i = 0; i < boundary.getNumberOfPoints(); i++) {
            boundaryNode_interact.add(boundary.getPoint(i));
        }
    }

    /**
     * switch 4 possible L-shape boundary
     *
     * @param siteBufferDist distance to buffer
     * @return void
     */
    public void switchBoundary(double siteBufferDist) {
        this.boundaryBase = (boundaryBase + 1) % 4;
        this.boundary = generateBoundary(redLine, boundaryBase, siteBufferDist);
        this.boundaryNode_interact = new ArrayList<>();
        for (int i = 0; i < boundary.getNumberOfPoints(); i++) {
            boundaryNode_interact.add(boundary.getPoint(i));
        }
    }

    /**
     * drag update to change the boundary shape manually
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void dragUpdateBoundary(double x, double y) {
        for (int i = 0; i < boundaryNode_interact.size() - 1; i++) {
            WB_Point p = boundaryNode_interact.get(i);
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.BOUNDARY_NODE_R) {
                p.set(x, y);
                if (i == 0) {
                    WB_Point last = boundaryNode_interact.get(boundaryNode_interact.size() - 1);
                    last.set(x, y);
                }
                break;
            }
        }
    }

    /**
     * generate a L-shape building boundary from a quad site
     *
     * @param validRedLine quad site red line
     * @param base         base index of point of validRedLine
     * @return wblut.geom.WB_Polygon
     */
    private WB_Polygon generateBoundary(WB_Polygon validRedLine, int base, double siteBufferDist) {
        assert validRedLine.getNumberOfPoints() == 5;
        WB_Coord[] boundaryPts = new WB_Coord[7];
        boundaryPts[0] = validRedLine.getPoint(base);
        boundaryPts[1] = validRedLine.getPoint((base + 1) % 4);
        boundaryPts[5] = validRedLine.getPoint((base + 3) % 4);
        boundaryPts[6] = boundaryPts[0];
        ZPoint vec01 = new ZPoint(
                boundaryPts[1].xd() - boundaryPts[0].xd(),
                boundaryPts[1].yd() - boundaryPts[0].yd()
        ).normalize();
        ZPoint vec05 = new ZPoint(
                boundaryPts[5].xd() - boundaryPts[0].xd(),
                boundaryPts[5].yd() - boundaryPts[0].yd()
        ).normalize();
        ZPoint bisector = vec01.add(vec05);
        double sin = Math.abs(vec01.cross2D(bisector));
        ZPoint move = bisector.scaleTo(siteBufferDist / sin);

        ZLine seg01_move = new ZLine(boundaryPts[0], boundaryPts[1]).translate2D(move);
        ZLine seg30_move = new ZLine(boundaryPts[5], boundaryPts[6]).translate2D(move);
        boundaryPts[3] = seg01_move.getPt0().toWB_Point();
        boundaryPts[2] = ZGeoMath.simpleLineElementsIntersect2D(
                seg01_move, "segment", new ZLine(validRedLine.getSegment((base + 1) % 4)), "segment"
        ).toWB_Point();
        boundaryPts[4] = ZGeoMath.simpleLineElementsIntersect2D(
                seg30_move, "segment", new ZLine(validRedLine.getSegment((base + 2) % 4)), "segment"
        ).toWB_Point();
        return new WB_Polygon(boundaryPts);
    }

    /* ------------- traffic interact ------------- */

    /**
     * drag update to change the main traffic shape
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void dragUpdateNode(double x, double y) {
        for (WB_Point p : innerNode_interact) {
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                WB_Point point = new WB_Point(x, y);
                if (WB_GeometryOp.contains2D(point, boundary) && WB_GeometryOp.getDistance2D(point, boundary) > MallConst.TRAFFIC_NODE_R) {
                    p.set(x, y);
                }
                return;
            }
        }
        for (WB_Point p : entryNode_interact) {
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                WB_Point point = new WB_Point(x, y);
                p.set(WB_GeometryOp2D.getClosestPoint2D(point, ZTransform.WB_PolygonToWB_PolyLine(boundary)));
                return;
            }
        }
    }

    /**
     * remove a inner node
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void removeInnerNode(double x, double y) {
        int size = innerNode_interact.size();
        for (int i = 0; i < size; i++) {
            WB_Point p = innerNode_interact.get(i);
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                innerNode_interact.remove(i);
                break;
            }
        }
    }

    /**
     * remove a entry node
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void removeEntryNode(double x, double y) {
        int size = entryNode_interact.size();
        for (int i = 0; i < size; i++) {
            WB_Point p = entryNode_interact.get(i);
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                entryNode_interact.remove(i);
                break;
            }
        }
    }

//    public void addInnerNode(double x, double y) {
//        WB_Point innerNode = new WB_Point(x, y);
//        if (WB_GeometryOp.contains2D(innerNode, boundary) && WB_GeometryOp.getDistance2D(innerNode, boundary) > MallConst.TRAFFIC_NODE_R) {
//            this.innerNode_interact.add(innerNode);
//        }
//    }
//
//    public void addEntryNode(double x, double y) {
//        WB_Point fixedNode = new WB_Point(x, y);
//        this.entryNode_interact.add(WB_GeometryOp2D.getClosestPoint2D(fixedNode, ZTransform.WB_PolygonToWB_PolyLine(boundary)));
//    }

    /* ------------- raw atrium interact ------------- */

    /**
     * click update: add atrium or select atrium
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void clickUpdateAtrium(double x, double y) {
        WB_Point p = new WB_Point(x, y);
        if (selectedAtriumType > -1) {
            // add atrium
            if (mainTraffic_interact.contains(ZFactory.jtsgf.createPoint(new Coordinate(x, y)))) {
                switch (selectedAtriumType) {
                    case 0:
                        rawAtriums.add(AtriumFactory.createAtrium3(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                    case 1:
                        rawAtriums.add(AtriumFactory.createAtrium4(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                    case 2:
                        rawAtriums.add(AtriumFactory.createAtrium4_(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                    case 3:
                        rawAtriums.add(AtriumFactory.createAtrium5(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                    case 4:
                        rawAtriums.add(AtriumFactory.createAtrium6(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                    case 5:
                        rawAtriums.add(AtriumFactory.createAtrium6_(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                    case 6:
                        rawAtriums.add(AtriumFactory.createAtrium7(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                    case 7:
                        rawAtriums.add(AtriumFactory.createAtrium8(p, MallConst.ATRIUM_AREA_INIT, true));
                        break;
                }
                selectedAtriumType = -1;
            }
        } else {
            // select atrium
            if (selectedAtrium == null) {
                for (AtriumNew a : rawAtriums) {
                    WB_Polygon shape = a.getShape();
                    if (WB_GeometryOp.contains2D(p, shape)) {
                        this.selectedAtrium = a;
                        break;
                    }
                }
            } else {
                WB_Polygon shape = selectedAtrium.getShape();
                if (!WB_GeometryOp.contains2D(p, shape)) {
                    this.selectedAtrium = null;
                }
            }
        }
    }

    /**
     * remove the selected atrium
     *
     * @return void
     */
    public void removeAtrium() {
        if (selectedAtrium != null) {
            rawAtriums.remove(selectedAtrium);
            selectedAtrium = null;
        }
    }

    /**
     * change curve shape or polygon shape of the selected atrium
     *
     * @return void
     */
    public void changeAtriumCurve() {
        if (selectedAtrium != null) {
            selectedAtrium.reverseCurve();
            selectedAtrium.updateShape();
            selectedAtrium.updateArea();
        }
    }

    /**
     * rotate the selected atrium
     *
     * @param angle angle to rotate
     * @return void
     */
    public void rotateAtrium(double angle) {
        if (selectedAtrium != null) {
            WB_Transform2D transform2D = new WB_Transform2D();
            WB_Point[] ori = selectedAtrium.getOriginalShapePoints();
            WB_Point[] shapePoints = selectedAtrium.getShapePoints();
            WB_Point center = selectedAtrium.getCenter();
            transform2D.addRotateAboutPoint(angle, center);
            for (int i = 0; i < shapePoints.length; i++) {
                shapePoints[i].set(transform2D.applyAsPoint2D(ori[i]));
            }
            selectedAtrium.updateShape();
        }
    }

    /**
     * drag to update an atrium's position or shape
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void dragUpdateAtrium(double x, double y) {
        if (selectedAtrium != null) {
            WB_Point center = selectedAtrium.getCenter();
            if (distance(center.xd(), center.yd(), x, y) <= MallConst.ATRIUM_POS_R) {
                WB_Point point = new WB_Point(x, y);
                if (mainTraffic_interact.contains(ZFactory.jtsgf.createPoint(new Coordinate(x, y)))) {
                    center.set(x, y);
                    selectedAtrium.updateShapeByCenter();
                    selectedAtrium.updateOriginalShapePoints();
                }
            } else {
                WB_Point[] shapePoints = selectedAtrium.getShapePoints();
                for (WB_Point p : shapePoints) {
                    if (distance(p.xd(), p.yd(), x, y) <= MallConst.ATRIUM_CTRL_R) {
                        p.set(x, y);
                        selectedAtrium.updateShape();
                        selectedAtrium.updateOriginalShapePoints();
                    }
                }
            }
        }
    }

    /* ------------- public space interact ------------- */


    /* ------------- structure grid interact ------------- */



    /* ------------- buffer curve shape interact ------------- */

    public void switchFloor(char floorKey) {

    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public void drawBufferCurve(PApplet app, JtsRender jtsRender) {
        app.pushStyle();

        // draw curve
        if (bufferCurve_interact != null) {
            app.stroke(25, 200, 202);
            app.strokeWeight(3);
            for (LineString ls : bufferCurve_interact) {
                jtsRender.drawGeometry(ls);
            }
        }

        app.popStyle();
    }

    public void drawBufferCurveControls(PApplet app, WB_Render render) {
        app.pushStyle();

        // draw Curve control points
        if (bufferCurveControl_interact != null) {
            app.fill(0, 0, 255);
            app.noStroke();
            for (WB_Point p : bufferCurveControl_interact) {
                render.drawPoint2D(p);
            }
        }

        app.popStyle();
    }

    /* ------------- shop cell interact ------------- */

    public void selectShopCell(double x, double y) {
        int size = cellPolys_interact.size();
        Point pointer = ZFactory.jtsgf.createPoint(new Coordinate(x, y));
        for (int i = 0; i < size; i++) {
            Polygon cell = cellPolys_interact.get(i);
            if (cell.contains(pointer)) {
                if (this.cellPolys_selected.contains(cell)) {
                    cellPolys_selected.remove(cell);
                } else {
                    cellPolys_selected.add(cell);
                }
                break;
            }
        }
    }

    public void clickUpdateShop() {
        if (cellPolys_selected.size() > 1) {
            cellPolys_interact.removeAll(cellPolys_selected);
//            Geometry union = cellPolys_selected.get(0);
//            for (int i = 1; i < cellPolys_selected.size(); i++) {
//                union = union.union(cellPolys_interact.get(i));
//            }

            Polygon[] polygons = new Polygon[cellPolys_selected.size()];
            for (int i = 0; i < cellPolys_selected.size(); i++) {
                polygons[i] = cellPolys_selected.get(i);
            }
            GeometryCollection collection = ZFactory.jtsgf.createGeometryCollection(polygons);
            Geometry union = collection.buffer(0);

            if (union instanceof Polygon) {
                cellPolys_interact.add((Polygon) union);
            } else if (union instanceof MultiPolygon) {
                for (int i = 0; i < union.getNumGeometries(); i++) {
                    cellPolys_interact.add((Polygon) union.getGeometryN(i));
                }
            } else {
                System.out.println(union.getGeometryType());
            }
        }
        cellPolys_selected.clear();
    }

    public void drawCellSelected(PApplet app, JtsRender jtsRender) {
        if (cellPolys_selected != null) {
            app.pushStyle();

            app.noFill();
            app.stroke(0, 255, 0);
            app.strokeWeight(5);
            for (Polygon p : cellPolys_selected) {
                jtsRender.drawGeometry(p);
            }

            app.popStyle();
        }
    }

    /* ------------- setter & getter ------------- */

    public WB_Polygon getSite() {
        return site;
    }

    public WB_Polygon getBoundary() {
        return boundary;
    }

    public void setInnerNode_interact(List<WB_Point> innerNode_interact) {
        this.innerNode_interact = innerNode_interact;
    }

    public void setEntryNode_interact(List<WB_Point> entryNode_interact) {
        this.entryNode_interact = entryNode_interact;
    }

    public List<WB_Point> getInnerNode_interact() {
        return innerNode_interact;
    }

    public List<WB_Point> getEntryNode_interact() {
        return entryNode_interact;
    }

    public List<WB_Point> getTrafficControls() {
        List<WB_Point> controls = new ArrayList<>();
        controls.add(entryNode_interact.get(0));
        controls.addAll(innerNode_interact);
        controls.add(entryNode_interact.get(entryNode_interact.size() - 1));
        return controls;
    }

    public void setMainTraffic_interact(Polygon mainTraffic_interact) {
        this.mainTraffic_interact = mainTraffic_interact;
    }

    public void setSelectedAtriumType(int selectedAtriumType) {
        this.selectedAtriumType = selectedAtriumType;
    }

    public void setRawAtriums(List<AtriumNew> rawAtriums) {
        this.rawAtriums = rawAtriums;
    }

    public List<WB_Polygon> getRawAtriumShapes() {
        List<WB_Polygon> atriums = new ArrayList<>();
        for (AtriumNew a : rawAtriums) {
            atriums.add(a.getShape());
        }
        return atriums;
    }


    public void setBufferCurve_interact(List<LineString> bufferCurve_interact) {
        this.bufferCurve_interact = bufferCurve_interact;
    }

    public void setCellPolys_interact(List<Shop> currentShops) {
        this.cellPolys_interact = new ArrayList<>();
        for (Shop shop : currentShops) {
            cellPolys_interact.add(shop.getShape());
        }
        this.cellPolys_selected = new ArrayList<>();
    }

    public List<LineString> getBufferCurve_interact() {
        return bufferCurve_interact;
    }

    public List<Shop> getCellPolys_interact() {
        System.out.println(cellPolys_interact.size());
        List<Shop> shopCells = new ArrayList<>();
        for (Polygon p : cellPolys_interact) {
            shopCells.add(new Shop(p));
        }
        return shopCells;
    }

    /* ------------- draw ------------- */

    public void displayLocal(PApplet app, WB_Render render, JtsRender jtsRender, int status) {
        app.pushStyle();

        switch (status) {
            case -1:
                break;
            case 0:
                displaySiteBoundary(app, render);
                break;
            case 1:
                displayTraffic(app, render);
                break;
            case 2:
                displayRawAtrium(app, render);
                if (selectedAtrium != null) {
                    displaySelectedAtrium(app, render);
                }
                break;
        }
        app.popStyle();
    }

    public void displaySiteBoundary(PApplet app, WB_Render render) {
        // draw boundary control nodes
        app.noStroke();
        app.fill(255, 97, 136);
        for (WB_Point p : boundaryNode_interact) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.BOUNDARY_NODE_R, (float) MallConst.BOUNDARY_NODE_R);
        }

        // draw boundary and site
        app.noFill();
        app.stroke(255);
        app.strokeWeight(6);
        render.drawPolygonEdges2D(boundary);
        app.stroke(255, 0, 0);
        app.strokeWeight(3);
        render.drawPolygonEdges2D(site);
    }

    public void displayTraffic(PApplet app, WB_Render render) {
        // draw traffic control nodes
        app.noStroke();
        app.fill(255, 97, 136);
        for (WB_Point p : innerNode_interact) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.TRAFFIC_NODE_R, (float) MallConst.TRAFFIC_NODE_R);
        }
        app.fill(128);
        for (WB_Point p : entryNode_interact) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.TRAFFIC_NODE_R, (float) MallConst.TRAFFIC_NODE_R);
        }
    }

    public void displayRawAtrium(PApplet app, WB_Render render) {
        // draw all raw atriums
        app.stroke(55, 103, 171);
        app.strokeWeight(2);
        app.noFill();
        for (AtriumNew an : rawAtriums) {
            render.drawPolygonEdges2D(an.getShape());
        }
    }

    public void displaySelectedAtrium(PApplet app, WB_Render render) {
        // draw center
        app.noStroke();
        app.fill(255, 97, 136);
        app.ellipse(selectedAtrium.getCenter().xf(), selectedAtrium.getCenter().yf(), (float) MallConst.ATRIUM_POS_R, (float) MallConst.ATRIUM_POS_R);

        // draw control points
        app.fill(169, 210, 118);
        for (WB_Point p : selectedAtrium.getShapePoints()) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.ATRIUM_CTRL_R, (float) MallConst.ATRIUM_CTRL_R);
        }

        // draw shape
        app.stroke(0, 255, 0);
        app.strokeWeight(4);
        app.noFill();
        render.drawPolygonEdges2D(selectedAtrium.getShape());
    }

}
