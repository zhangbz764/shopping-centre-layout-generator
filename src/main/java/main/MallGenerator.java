package main;

import advancedGeometry.ZCatmullRom;
import advancedGeometry.ZSkeleton;
import advancedGeometry.rectCover.ZRectCover;
import basicGeometry.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import converter.WB_Converter;
import geometry.Segments;
import geometry.Vertices;
import mallElementNew.StructureGrid;
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
 * main generator
 * catch data from frontend
 * convert data to frontend
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project archijson
 * @date 2021/3/10
 * @time 17:55
 */
public class MallGenerator {
    // site & boundary
    private WB_Polygon site_receive;                // 场地（共用）
    private WB_Polygon boundary_receive;            // 轮廓（共用）

    // main traffic
    private ZCatmullRom mainTrafficCurve;           // 主路径轴线（共用）
    private Polygon mainTrafficBuffer;              // 主路径区域（共用）
    private List<WB_Point> innerNode_receive;       // 内部控制点（共用）
    private List<WB_Point> entryNode_receive;       // 轮廓控制点（共用）

    // raw atriums & public space
    private List<WB_Polygon> rawAtrium_receive;     // 中庭多边形（共用）
    private Polygon publicSpace;                 // 收敛后的中央交通空间轮廓

    // structure grid
    private StructureGrid[] grids;                  // 结构轴网（共用）
    private boolean validGrids = true;

    // shop cells

    private List<List<LineString>> bufferCurve_receive;   // 动线边界曲线（不同层）
    private List<List<WB_Polygon>> cellPolys_receive;     // 商铺剖分多边形（不同层）

    // core generate result: floors
    private MallFloor[] floors;

    /* ------------- constructor ------------- */

    public MallGenerator() {
        // setup floors
        this.floors = new MallFloor[MallConst.FLOOR_TOTAL];
//        for (int i = 0; i < floors.length; i++) {
//            floors[i] = new MallFloor(i + 1, boundary_receive);
//            floors[i].setStatus(0);
//
////            cellPolys_receive.add(new ArrayList<WB_Polygon>());
//        }
    }

//    public void init(int gridNum) {
//        // setup structure grids
//        ZRectCover zrc = new ZRectCover(boundary_receive, gridNum);
//        List<Polygon> rects = zrc.getBestRects();
//        this.grids = new StructureGrid[gridNum];
//        for (int i = 0; i < rects.size(); i++) {
//            grids[i] = new StructureGrid(rects.get(i), MallConst.SHOP_SPAN_THRESHOLD);
//        }
//
//        // setup floors
//        this.floors = new MallFloor[MallConst.FLOOR_TOTAL];
//
//        this.bufferCurve_receive = new ArrayList<>();
//        bufferCurve_receive.add(new ArrayList<LineString>());
//        bufferCurve_receive.add(new ArrayList<LineString>());
//        this.cellPolys_receive = new ArrayList<>();
//        for (int i = 0; i < floors.length; i++) {
//            floors[i] = new MallFloor(i + 1, boundary_receive);
//            floors[i].setStatus(0);
//
//            cellPolys_receive.add(new ArrayList<WB_Polygon>());
//        }
//    }

    /* ------------- generating ------------- */

    /**
     * initialize main traffic
     *
     * @param bufferDist distance to buffer
     * @return void
     */
    public void initTraffic(double bufferDist) {
        // find ridges of skeleton
        ZSkeleton boundarySkel = new ZSkeleton(boundary_receive);
        List<ZLine> centralSegs = boundarySkel.getRidges();
        centralSegs.addAll(boundarySkel.getExtendedRidges());
        LineString centralLs = ZFactory.createLineString(centralSegs);

        // divide and add entries
        List<ZPoint> dividePts = ZGeoMath.splitPolyLineEdge(centralLs, 6);
        dividePts.remove(0);
        dividePts.remove(dividePts.size() - 1);
        WB_Point[] pts = new WB_Point[dividePts.size() + 2];
        for (int i = 0; i < dividePts.size(); i++) {
            pts[i + 1] = dividePts.get(i).toWB_Point();
        }
        WB_PolyLine boundLS = ZTransform.WB_PolygonToWB_PolyLine(boundary_receive);
        WB_Point entryP1 = WB_GeometryOp.getClosestPoint2D(dividePts.get(0).toWB_Point(), boundLS);
        WB_Point entryP2 = WB_GeometryOp.getClosestPoint2D(dividePts.get(dividePts.size() - 1).toWB_Point(), boundLS);
        pts[0] = entryP1;
        pts[pts.length - 1] = entryP2;

        // build traffic curve, and make buffer polygon
        this.mainTrafficCurve = new ZCatmullRom(pts, 10, false);
        this.mainTrafficBuffer = (Polygon) mainTrafficCurve.getAsLineString().buffer(bufferDist);
    }

    /**
     * update main traffic
     *
     * @param bufferDist distance to buffer
     * @return void
     */
    public void updateTraffic(double bufferDist) {
        WB_Point[] generatePts = new WB_Point[innerNode_receive.size() + entryNode_receive.size()];
        generatePts[0] = entryNode_receive.get(0);
        for (int i = 0; i < innerNode_receive.size(); i++) {
            generatePts[i + 1] = innerNode_receive.get(i);
        }
        generatePts[generatePts.length - 1] = entryNode_receive.get(entryNode_receive.size() - 1);
        // build traffic curve, and make buffer polygon
        this.mainTrafficCurve = new ZCatmullRom(generatePts, 10, false);
        this.mainTrafficBuffer = (Polygon) mainTrafficCurve.getAsLineString().buffer(bufferDist);
    }

    /**
     * update public traffic space
     *
     * @return void
     */
    public void updatePublicSpace() {
        if (rawAtrium_receive == null || rawAtrium_receive.size() == 0) {
            WB_Point[] generatePtsTemp = innerNode_receive.toArray(new WB_Point[0]);
            ZCatmullRom curveTemp = new ZCatmullRom(generatePtsTemp, 10, false);
            Polygon bufferTemp = (Polygon) curveTemp.getAsLineString().buffer(MallConst.TRAFFIC_BUFFER_DIST);
            this.publicSpace = bufferTemp;
        } else {
            WB_Polygon bufferOut = ZFactory.wbgf.createBufferedPolygons2D(rawAtrium_receive, 50).get(0);
            WB_Polygon bufferIn = ZFactory.wbgf.createBufferedPolygons2D(bufferOut, -50).get(0);
            this.publicSpace = ZTransform.WB_PolygonToPolygon(bufferIn);
        }
    }

    /**
     * setup structure grids
     *
     * @param gridNum number of structure grids
     * @param dist    column distance
     * @return void
     */
    public void initGrid(int gridNum, double dist) {
        ZRectCover zrc = new ZRectCover(boundary_receive, gridNum);
        List<Polygon> rects = zrc.getBestRects();
        this.grids = new StructureGrid[gridNum];
        for (int i = 0; i < rects.size(); i++) {
            grids[i] = new StructureGrid(rects.get(i), dist);
        }
    }

    /**
     * update structure grid given a new rectangle
     *
     * @param gridIndex index of changed grid
     * @param rect      new rectangle
     * @return void
     */
    public void updateGridByRect(int gridIndex, Polygon rect) {
        if (gridIndex > -1 && rect != null) {
            grids[gridIndex].updateRect(rect);
            Geometry union = grids[0].getRect();
            for (int i = 1; i < grids.length; i++) {
                union = union.union(grids[i].getRect());
            }
            union = union.buffer(1);
            this.validGrids = union.contains(ZTransform.WB_PolygonToPolygon(boundary_receive));
        }
    }

    public void updateGridByDist(int gridIndex, double dist) {

    }

    /**
     * description
     *
     * @param floorNum
     * @return void
     */
    public void initShopCells(int floorNum) {
        if (floorNum == 1) {
            List<LineString> publicSpaceLS = new ArrayList<>(ZTransform.PolygonToLineString(publicSpace));
            floors[floorNum - 1] = new MallFloor(floorNum, boundary_receive);
            floors[floorNum - 1].setStatus(0);
            this.floors[floorNum - 1].updateSubdivision(publicSpaceLS, grids);
        } else {
            List<LineString> publicSpaceLS = new ArrayList<>(ZTransform.PolygonToLineString(publicSpace));
            floors[floorNum - 1] = new MallFloor(floorNum, boundary_receive);
            floors[floorNum - 1].setStatus(0);
            Point verify = publicSpace.getInteriorPoint();
            floors[floorNum - 1].setVerify(verify);
            this.floors[floorNum - 1].updateSubdivision(publicSpaceLS, grids);
        }
    }

    /* ------------- setter & getter ------------- */

    public void setSite_receive(WB_Polygon site_receive) {
        this.site_receive = site_receive;
    }

    public WB_Polygon getSite_receive() {
        return site_receive;
    }

    public void setBoundary_receive(WB_Polygon boundary_receive) {
        this.boundary_receive = boundary_receive;
    }

    public WB_Polygon getBoundary_receive() {
        return boundary_receive;
    }

    public ZCatmullRom getMainTrafficCurve() {
        return mainTrafficCurve;
    }

    public Polygon getMainTrafficBuffer() {
        return mainTrafficBuffer;
    }

    public List<WB_Point> getTrafficInnerNodes() {
        List<WB_Point> nodes = new ArrayList<>();
        List<ZPoint> controls = mainTrafficCurve.getCurveControlPts();
        for (int i = 1; i < controls.size() - 1; i++) {
            nodes.add(controls.get(i).toWB_Point());
        }
        return nodes;
    }

    public List<WB_Point> getTrafficEntryNodes() {
        List<WB_Point> nodes = new ArrayList<>();
        List<ZPoint> controls = mainTrafficCurve.getCurveControlPts();
        nodes.add(controls.get(0).toWB_Point());
        nodes.add(controls.get(controls.size() - 1).toWB_Point());
        return nodes;
    }

    public void setInnerNode_receive(List<WB_Point> innerNode_receive) {
        this.innerNode_receive = innerNode_receive;
    }

    public void setEntryNode_receive(List<WB_Point> entryNode_receive) {
        this.entryNode_receive = entryNode_receive;
    }

    public void setRawAtrium_receive(List<WB_Polygon> rawAtrium_receive) {
        this.rawAtrium_receive = rawAtrium_receive;
    }

    public Polygon[] getGridRects() {
        Polygon[] rects = new Polygon[grids.length];
        for (int i = 0; i < grids.length; i++) {
            rects[i] = grids[i].getRect();
        }
        return rects;
    }

    /* ------------- draw ------------- */

    public void displayLocal(PApplet app, WB_Render render, JtsRender jtsRender, int status, int floorNum) {
        app.pushStyle();
        switch (status) {
            case -1:
            case 0:
                break;
            case 1:
                displaySiteBoundaryLocal(app, render);
                displayTrafficLocal(app, jtsRender);
                break;
            case 2:
                displaySiteBoundaryLocal(app, render);
                displayTrafficLocal(app, jtsRender);
                break;
            case 3:
                displaySiteBoundaryLocal(app, render);
                displayPublicSpaceLocal(app, jtsRender);
                break;
            case 4:
                displaySiteBoundaryLocal(app, render);
                displayPublicSpaceLocal(app, jtsRender);
                displayGridLocal(app);
                break;
            case 5:
                displaySiteBoundaryLocal(app, render);
                displayPublicSpaceLocal(app, jtsRender);
                displayGridLocal(app);
                displayShopCellsLocal(floorNum, app, jtsRender);
                break;
        }
        app.popStyle();
    }

    public void displaySiteBoundaryLocal(PApplet app, WB_Render render) {
        // draw boundary and site
        app.noFill();
        app.stroke(255);
        app.strokeWeight(6);
        render.drawPolygonEdges(boundary_receive);
        app.stroke(255, 0, 0);
        app.strokeWeight(3);
        render.drawPolygonEdges(site_receive);
    }

    public void displayTrafficLocal(PApplet app, JtsRender jtsRender) {
        // draw traffic route and buffer area
        app.stroke(255);
        app.strokeWeight(1);
        jtsRender.drawGeometry(mainTrafficCurve.getAsLineString());
        app.stroke(52, 170, 187);
        app.strokeWeight(3);
        jtsRender.drawGeometry(mainTrafficBuffer);
    }

    public void displayPublicSpaceLocal(PApplet app, JtsRender jtsRender) {
        // draw public space generated by raw atriums
        app.stroke(52, 170, 187);
        app.strokeWeight(3);
        jtsRender.drawGeometry(publicSpace);
    }

    public void displayGridLocal(PApplet app) {
        app.strokeWeight(0.5f);
        if (validGrids) {
            app.stroke(128);
        } else {
            app.stroke(255, 0, 0);
        }

        for (StructureGrid grid : grids) {
            for (ZLine l : grid.getAllLines()) {
                l.display(app);
            }
        }
    }

    public void displayShopCellsLocal(int floorNum, PApplet app, JtsRender jtsRender) {
        if (floors[floorNum - 1].getAllCells() != null) {
            app.strokeWeight(3);
            app.stroke(255);
            List<Shop> cells = floors[floorNum - 1].getAllCells();
            for (Shop s : cells) {
                jtsRender.drawGeometry(s.getShape());
            }
            app.noStroke();
            for (Shop s : floors[floorNum - 1].getAllCells()) {
                s.display(app, jtsRender);
            }

            app.fill(255);
            app.textSize(2);

            for (Shop s : floors[floorNum - 1].getAllCells()) {
                app.pushMatrix();
                app.scale(1, -1);
                app.translate(0, (float) (-2 * s.getCenter().getY()));
                s.displayText(app);
                app.popMatrix();
            }
        }
    }

    /* ------------- deprecated ------------- */

    public void setBufferCurve_receive(int floorNum, List<LineString> bufferCurve_receive) {
        if (floorNum == 1) {
            this.bufferCurve_receive.set(0, bufferCurve_receive);
        } else {
            this.bufferCurve_receive.set(1, bufferCurve_receive);
        }
    }

    public void setBufferCurve_receive(List<List<LineString>> bufferCurve_receive) {
        this.bufferCurve_receive = bufferCurve_receive;
    }

    public void setCellPolys_receive(int floorNum, List<WB_Polygon> cellPolys_receive) {
        this.cellPolys_receive.set(floorNum - 1, cellPolys_receive);
    }

    public void setShopCells_receive(int floorNum, List<Shop> shopCell_receive) {
        this.floors[floorNum - 1].setAllCells(shopCell_receive);
    }

    public void setCellPolys_receive(List<List<WB_Polygon>> cellPolys_receive) {
        this.cellPolys_receive = cellPolys_receive;
    }

    public MallFloor[] getFloors() {
        return floors;
    }

    public List<WB_Segment> getGraphSegments(int floorNum) {
        return floors[floorNum - 1].getGraph().toWB_Segments();
    }

    public List<List<WB_Coord>> getBufferControlPoints(int floorNum) {
        return floors[floorNum - 1].getBufferControlPoints();
    }

    public List<Shop> getShopCells(int floorNum) {
        return floors[floorNum - 1].getAllCells();
    }

    public String getFloorStats(int floorNum) {
        MallFloor floor = floors[floorNum - 1];

        WB_Polygon bufferOut = ZFactory.wbgf.createBufferedPolygons2D(boundary_receive, MallConst.EVACUATION_WIDTH).get(0);
        double bufferArea = Math.abs(bufferOut.getSignedArea());

//        double totalArea = Math.abs(boundary_receive.getSignedArea());
//        double shopArea = 0;
//        for (Polygon p : floor.getShopBlocks()) {
//            shopArea += p.getArea();
//        }
        double shopArea = newBound.getArea();
        for (Polygon ep : evacPoly) {
            shopArea -= ep.getArea();
        }

        int shopNum = getShopCells(floorNum).size();
        double trafficLength = 0;
        for (ZEdge e : floor.getGraph().getAllEdges()) {
            trafficLength += e.getLength();
        }
        double shopRatio = shopArea / bufferArea;

        return "本层建筑面积 : " + String.format("%.2f", bufferArea) + " ㎡"
                + "\n" + "可租赁面积 : " + String.format("%.2f", shopArea) + " ㎡"
                + "\n" + "商铺总数量 : " + shopNum
                + "\n" + "分层得铺率 : " + String.format("%.2f", shopRatio * 100) + " %"
                + "\n" + "小铺率 : "
                + "\n" + "动线长度 : " + String.format("%.2f", trafficLength) + " m";
    }
    /* ------------- deprecated ------------- */

    /**
     * update traffic graph and buffer of current floor
     *
     * @param floorNum    current floor number
     * @param dist        buffer distance
     * @param curvePtsNum curve subdivision number
     * @return void
     */
    public void generateGraphAndBuffer(int floorNum, double dist, int curvePtsNum) {
        if (floorNum == 1) {
            this.floors[floorNum - 1].updateGraph(innerNode_receive, entryNode_receive);
        } else {
            this.floors[floorNum - 1].updateGraph(innerNode_receive, new ArrayList<WB_Point>());
        }
        this.floors[floorNum - 1].updateBuffer(rawAtrium_receive, dist, curvePtsNum);

        this.floors[floorNum - 1].disposeSplit();
        this.floors[floorNum - 1].disposeSubdivision();
        this.floors[floorNum - 1].disposeEvacuation();
        this.floors[floorNum - 1].setStatus(1);
    }

    /**
     * update block split and subdivision
     *
     * @param floorNum current floor number
     * @return void
     */
    public void generateSubdivision(int floorNum) {
        if (floorNum == 1) {
            this.floors[floorNum - 1].updateSubdivision(bufferCurve_receive.get(0), grids);
        } else {
            this.floors[floorNum - 1].updateSubdivision(bufferCurve_receive.get(1), grids);
        }

        this.floors[floorNum - 1].disposeEvacuation();
        this.floors[floorNum - 1].setStatus(2);
    }

    /**
     * generate positions of evacuate stairways
     *
     * @param floorNum current floor number
     * @return void
     */
    public void generateEvacuation(int floorNum) {
        this.floors[floorNum - 1].updateEvacuation(cellPolys_receive.get(floorNum - 1));

        this.floors[floorNum - 1].setStatus(3);
    }

    private List<Polygon> evacRectTemp;
    private List<Polygon> evacPoly;

    private Polygon newBound;

    public void generateEvacuation2() {
        WB_Polygon bufferBoundary = ZFactory.wbgf.createBufferedPolygons2D(boundary_receive, MallConst.SHOP_SPAN_THRESHOLD[0] * -0.5).get(0);
        WB_Polygon validBuffer = ZTransform.validateWB_Polygon(bufferBoundary);
        List<ZPoint> dividePoints = ZGeoMath.splitPolyLineByThreshold(validBuffer, MallConst.EVACUATION_DIST, MallConst.EVACUATION_DIST - 10);

        this.evacRectTemp = new ArrayList<>();
        this.evacPoly = new ArrayList<>();

        for (ZPoint p : dividePoints) {
            for (StructureGrid g : grids) {
                Polygon rect = g.getRect();
                if (rect.contains(p.toJtsPoint())) {
                    double distTo10 = WB_GeometryOp.getDistance2D(p.toWB_Point(), g.getLat12().get(0).toWB_Segment());
                    double distTo12 = WB_GeometryOp.getDistance2D(p.toWB_Point(), g.getLon10().get(0).toWB_Segment());

                    if (distTo10 < g.getLengthUnit12()) {
                        // 靠近10边
                        int n = (int) (distTo12 / g.getLengthUnit10());
                        Coordinate[] coords = new Coordinate[5];
                        if (n < 1) {
                            coords[0] = g.getGridNodes()[0][0].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[0][1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[2][1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[2][0].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else if (n > g.getLon10().size() - 2) {
                            coords[0] = g.getGridNodes()[n][0].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[n][1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[n + 2][1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[n + 2][0].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else {
                            coords[0] = g.getGridNodes()[n - 1][0].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[n - 1][1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[n + 1][1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[n + 1][0].toJtsCoordinate();
                            coords[4] = coords[0];
                        }
                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
                    } else if (distTo10 <= g.getLength12() && distTo10 > g.getLength12() - g.getLengthUnit12()) {
                        // 靠近23边
                        int size12 = g.getLat12().size();
                        int n = (int) (distTo12 / g.getLengthUnit10());
                        Coordinate[] coords = new Coordinate[5];
                        if (n < 1) {
                            coords[0] = g.getGridNodes()[0][size12 - 2].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[0][size12 - 1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[2][size12 - 1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[2][size12 - 2].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else if (n > g.getLon10().size() - 2) {
                            coords[0] = g.getGridNodes()[n][size12 - 2].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[n][size12 - 1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[n + 2][size12 - 1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[n + 2][size12 - 2].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else {
                            coords[0] = g.getGridNodes()[n - 1][size12 - 2].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[n - 1][size12 - 1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[n + 1][size12 - 1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[n + 1][size12 - 2].toJtsCoordinate();
                            coords[4] = coords[0];
                        }
                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
                    } else if (distTo12 < g.getLengthUnit10()) {
                        // 靠近12边
                        int n = (int) (distTo10 / g.getLengthUnit12());
                        Coordinate[] coords = new Coordinate[5];
                        if (n < 1) {
                            coords[0] = g.getGridNodes()[1][0].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[0][0].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[0][2].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[1][2].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else if (n > g.getLat12().size() - 2) {
                            coords[0] = g.getGridNodes()[1][n - 1].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[0][n - 1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[0][n + 1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[1][n + 1].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else {
                            coords[0] = g.getGridNodes()[1][n].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[0][n].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[0][n + 2].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[1][n + 2].toJtsCoordinate();
                            coords[4] = coords[0];
                        }
                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
                    } else if (distTo12 <= g.getLength10() && distTo12 > g.getLength10() - g.getLengthUnit10()) {
                        // 靠近30边
                        int size10 = g.getLon10().size();
                        int n = (int) (distTo10 / g.getLengthUnit12());
                        Coordinate[] coords = new Coordinate[5];
                        if (n < 1) {
                            coords[0] = g.getGridNodes()[size10 - 1][0].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[size10 - 2][0].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[size10 - 2][2].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[size10 - 1][2].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else if (n > g.getLat12().size() - 2) {
                            coords[0] = g.getGridNodes()[size10 - 1][n - 1].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[size10 - 2][n - 1].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[size10 - 2][n + 1].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[size10 - 1][n + 1].toJtsCoordinate();
                            coords[4] = coords[0];
                        } else {
                            coords[0] = g.getGridNodes()[size10 - 1][n].toJtsCoordinate();
                            coords[1] = g.getGridNodes()[size10 - 2][n].toJtsCoordinate();
                            coords[2] = g.getGridNodes()[size10 - 2][n + 2].toJtsCoordinate();
                            coords[3] = g.getGridNodes()[size10 - 1][n + 2].toJtsCoordinate();
                            coords[4] = coords[0];
                        }
                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
                    }
                    break;
                }
            }
        }

        // 布尔运算
        Polygon bound = ZTransform.WB_PolygonToPolygon(boundary_receive);
        for (Polygon rect : evacRectTemp) {
            Geometry intersect = bound.intersection(rect);
            if (intersect instanceof Polygon) {
                evacPoly.add((Polygon) intersect);
            }
        }
        Geometry difference = bound;
        for (Polygon rect : evacRectTemp) {
            difference = difference.difference(rect);
        }
        if (difference.getGeometryType().equals("MultiPolygon")) {
            double area = 0;
            for (int i = 0; i < difference.getNumGeometries(); i++) {
                Geometry g = difference.getGeometryN(i);
                if (g instanceof Polygon && g.getArea() > area) {
                    this.newBound = (Polygon) g;
                }
            }
        }
        System.out.println("diff: " + difference.getGeometryType());
    }

    public void displayGraphLocal(int floorNum, PApplet app, WB_Render render) {
        app.pushStyle();
        app.stroke(255);
        app.strokeWeight(1);
        for (WB_Segment seg : getGraphSegments(floorNum)) {
            render.drawSegment(seg);
        }
        app.popStyle();
    }

    public void displayPartitionLocal(int floorNum, PApplet app, JtsRender jtsRender) {
        app.pushStyle();
        app.strokeWeight(3);
        app.stroke(255);
        if (floors[floorNum - 1].getAllCells() != null) {
            List<Shop> cells = floors[floorNum - 1].getAllCells();
            for (Shop s : cells) {
                jtsRender.drawGeometry(s.getShape());
            }
        }
//        if (floors[floorNum - 1].getAllSubLines() != null) {
//            for (LineString l : floors[floorNum - 1].getAllSubLines()) {
//                jtsRender.drawGeometry(l);
//            }
//        }

        if (floors[floorNum - 1].getAllCells() != null) {
            app.noStroke();
            for (Shop s : floors[floorNum - 1].getAllCells()) {
                s.display(app, jtsRender);
            }

            app.fill(255);
            app.textSize(2);

            for (Shop s : floors[floorNum - 1].getAllCells()) {
                app.pushMatrix();
                app.scale(1, -1);
                app.translate(0, (float) (-2 * s.getCenter().getY()));
                s.displayText(app);
                app.popMatrix();
            }
        }
        app.popStyle();
    }

    public void displayEvacuationLocal(PApplet app, JtsRender jtsRender) {
        app.pushStyle();

        if (evacPoly != null) {
            app.stroke(255);
            app.fill(80);
            for (Polygon p : evacPoly) {
                jtsRender.drawGeometry(p);
            }
        }

//        if (newBound != null) {
//            app.stroke(255);
//            app.pushMatrix();
//            app.translate(500, 0);
//            jtsRender.drawGeometry(newBound);
//            app.popMatrix();
//        }

        app.popStyle();
    }

    /* ------------- JSON converting ------------- */

    /**
     * converting status 1 geometries to JsonElement
     *
     * @param floorNum current floor
     * @param elements list of JsonElement
     * @param gson     Gson
     * @return void
     */
    private void convertingStatus1(int floorNum, List<JsonElement> elements, Gson gson) {
        // preparing data
        List<WB_Segment> graphSegments = getGraphSegments(floorNum);
        List<List<WB_Coord>> controlPoints = getBufferControlPoints(floorNum);

        // converting to json
        for (WB_Segment seg : graphSegments) {
            Segments segments = WB_Converter.toSegments(seg);
            JsonObject prop1 = new JsonObject();
            prop1.addProperty("name", "treeEdges");
            segments.setProperties(prop1);
            elements.add(gson.toJsonTree(segments));
        }
        for (List<WB_Coord> splitPointsEach : controlPoints) {
            Vertices bufferControlPointsEach = WB_Converter.toVertices(splitPointsEach, 3);
            JsonObject prop2 = new JsonObject();
            prop2.addProperty("name", "bufferControl");
            bufferControlPointsEach.setProperties(prop2);
            elements.add(gson.toJsonTree(bufferControlPointsEach));
        }
    }

    /**
     * converting status 2 geometries to JsonElement
     *
     * @param floorNum current floor
     * @param elements list of JsonElement
     * @param gson     Gson
     * @return void
     */
    private void convertingStatus2(int floorNum, List<JsonElement> elements, Gson gson) {
        // preparing data
        List<Shop> allShops = floors[floorNum - 1].getAllCells();
        List<WB_Polygon> allCells = new ArrayList<>();
        for (Shop s : allShops) {
            allCells.add(s.getShapeWB());
        }

        // converting to json
        for (WB_Polygon p : allCells) {
            Segments cell = WB_Converter.toSegments(p);
            JsonObject prop = new JsonObject();
            prop.addProperty("name", "shopCell");

            double area = Math.abs(p.getSignedArea());
            if (area > 2000) {
                prop.addProperty("shopType", "anchor");
            } else if (area > 400 && area <= 2000) {
                prop.addProperty("shopType", "subAnchor");
            } else if (area > 80 && area <= 400) {
                prop.addProperty("shopType", "ordinary");
            } else {
                prop.addProperty("shopType", "invalid");
            }

            cell.setProperties(prop);
            elements.add(gson.toJsonTree(cell));
        }
    }

    /**
     * converting status 3 geometries to JsonElement
     *
     * @param floorNum current floor
     * @param elements list of JsonElement
     * @param gson     Gson
     * @return void
     */
    private void convertingStatus3(int floorNum, List<JsonElement> elements, Gson gson) {
        // preparing data
        List<WB_Point> evacuationPoints = floors[floorNum - 1].getEvacuationPoint();

        // converting to json
        Vertices evacuation = WB_Converter.toVertices(evacuationPoints, 3);
        JsonObject prop = new JsonObject();
        prop.addProperty("name", "evacuation");
        evacuation.setProperties(prop);
        elements.add(gson.toJsonTree(evacuation));
    }

    /* ------------- JSON sending ------------- */

    /**
     * convert backend geometries to ArchiJSON
     * graph segments, buffer control points
     *
     * @param floorNum current floor number
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSONGraphAndBuffer(int floorNum, String clientID, Gson gson) {
        // initializing
        ArchiJSON json = new ArchiJSON();
        json.setId(clientID);
        List<JsonElement> elements = new ArrayList<>();

        convertingStatus1(floorNum, elements, gson);

        // setup json
        json.setGeometryElements(elements);
        return json;
    }

    /**
     * convert backend geometries to ArchiJSON
     * first-level subdivision cells
     *
     * @param floorNum current floor number
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSONSubdivision(int floorNum, String clientID, Gson gson) {
        // initializing
        ArchiJSON json = new ArchiJSON();
        json.setId(clientID);
        List<JsonElement> elements = new ArrayList<>();

        convertingStatus2(floorNum, elements, gson);

        // setup json
        json.setGeometryElements(elements);
        return json;
    }

    /**
     * convert backend geometries to ArchiJSON
     * evacuation points & segments
     *
     * @param floorNum current floor number
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSONEvacuation(int floorNum, String clientID, Gson gson) {
        // initializing
        ArchiJSON json = new ArchiJSON();
        json.setId(clientID);
        List<JsonElement> elements = new ArrayList<>();

        convertingStatus3(floorNum, elements, gson);

        // setup json
        json.setGeometryElements(elements);
        return json;
    }

    /**
     * switch floor num: convert all geometries in one floor
     *
     * @param floorNum current floor number
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSONFloor(int floorNum, String clientID, Gson gson) {
        // initializing
        ArchiJSON json = new ArchiJSON();
        json.setId(clientID);
        List<JsonElement> elements = new ArrayList<>();

        // preparing data
        int currentStatus = floors[floorNum - 1].getStatus();
        int count = 1;
        if (count <= currentStatus) {
            convertingStatus1(floorNum, elements, gson);
            count++; // ==2
            if (count <= currentStatus) {
                convertingStatus2(floorNum, elements, gson);
                count++; // ==3
                if (count <= currentStatus) {
                    convertingStatus3(floorNum, elements, gson);
                }
            }
        } else {
            System.out.println("this floor hasn't been initialized due to some error");
        }

        // setup json
        json.setGeometryElements(elements);
        return json;
    }
}