package main;

import advancedGeometry.rectCover.ZRectCover;
import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import mallElementNew.*;
import mallParameters.MallConst;
import math.ZGeoMath;
import math.ZMath;
import org.locationtech.jts.geom.*;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Circle;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
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
    private SiteBase siteBase;
    private double boundaryArea = 0;

    // main traffic & raw atriums
    private MainTraffic mainTraffic;
    private LineString innerTrafficCurve;          // 内部主路径轴线（除去入口）

    // main corridor
    private Polygon[] rawAtrium_receive;       // 中庭多边形（共用）
    private PublicSpace publicSpace;

    // structure grid
    private StructureGrid[] grids;                  // 结构轴网（共用）
    private boolean validGrids = true;              // 轴网是否覆盖平面
    private boolean gridModelSwitch = true;         // 8.4m 或 9m 切换

    // shop cells
    // core generate result: floors
    private MallFloor[] floors;

    // evacuation


    private List<List<LineString>> bufferCurve_receive;   // 动线边界曲线（不同层）
//    private List<List<WB_Polygon>> cellPolys_receive;     // 商铺剖分多边形（不同层）



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

    /* ------------- utils ------------- */

    public String getTrafficLength() {
        String s = "";
        if (mainTraffic != null) {
            s = s + String.format("%.2f", mainTraffic.getTrafficLength()) + "m";
        }
        return s;
    }

    public String getInnerTrafficLength() {
        String s = "";
        if (innerTrafficCurve != null) {
            s = s + String.format("%.2f", innerTrafficCurve.getLength()) + "m";
        }
        return s;
    }

    /* ------------- generating site & boundary ------------- */

    /**
     * initialize site and boundary
     *
     * @param _site     input site
     * @param _boundary input boundary (null allowed)
     * @param param1    input parameter 1 (int)
     * @param param2    input parameter 2 (double)
     * @param param3    input parameter 3 (double)
     * @return void
     */
    public void initSiteBoundary(WB_Polygon _site, WB_Polygon _boundary, int param1, double param2, double param3) {
        if (_boundary == null) {
            // init SiteBase_L
            this.siteBase = new SiteBase_L(
                    ZTransform.WB_PolygonToPolygon(_site),
                    param1, param2, param3
            );
        } else {
            this.siteBase = new SiteBase_Input(
                    ZTransform.WB_PolygonToPolygon(_site),
                    ZTransform.WB_PolygonToPolygon(_boundary)
            );
        }
        this.boundaryArea = siteBase.getBoundaryArea();
    }

    /**
     * description
     *
     * @param base
     * @param redLineDist
     * @param siteBufferDist
     * @return void
     */
    public void updateSiteBoundary(int base, double redLineDist, double siteBufferDist) {
        siteBase.updateByParams(base, redLineDist, siteBufferDist);
        this.boundaryArea = siteBase.getBoundaryArea();
        System.out.println(boundaryArea);
    }

    /**
     * update boundary by nodes
     *
     * @param boundaryNodes_receive nodes received from MallInteract
     * @return void
     */
    public void updateBoundaryByNodes(Coordinate[] boundaryNodes_receive) {
        this.siteBase.updateByNodes(boundaryNodes_receive);
    }

    /* ------------- generating main traffic ------------- */

    /**
     * initialize main traffic
     *
     * @param bufferDist distance to buffer
     * @return void
     */
    public void initTraffic(double bufferDist) {
        this.mainTraffic = new MainTraffic(siteBase.getBoundary(), bufferDist);
    }

    /**
     * update main traffic
     *
     * @param innerNode_receive received inner node
     * @param entryNode_receive received entry node
     * @param bufferDist        distance to buffer
     * @return void
     */
    public void updateTraffic(List<WB_Point> innerNode_receive, List<WB_Point> entryNode_receive, double bufferDist) {
        mainTraffic.updateTraffic(innerNode_receive, entryNode_receive, bufferDist);
    }

    /**
     * update main traffic
     *
     * @param bufferDist distance to buffer
     * @return void
     */
    public void updateTrafficWidth(double bufferDist) {
        mainTraffic.updateTrafficWidth(bufferDist);
    }

    /* ------------- generating main corridor ------------- */

    /**
     * initialize the main corridor
     *
     * @param corridorWidth width of corridor
     * @return void
     */
    public void initMainCorridor(double corridorWidth) {
        this.publicSpace = new PublicSpace(rawAtrium_receive, mainTraffic.getMainTrafficCurve());
    }

    /**
     * update main corridor position
     *
     * @param corridorID id of changed corridor
     * @param newNodes   new corridor nodes
     * @return void
     */
    public void updateMainCorridorPos(int corridorID, WB_Point[] newNodes) {
        if (corridorID > -1) {
            publicSpace.updateCorridorPos(corridorID, new ZLine(newNodes[0], newNodes[1]));
        }
    }

    /**
     * update main corridor width
     *
     * @param corridorID id of changed corridor
     * @param width      new width
     * @return void
     */
    public void updateMainCorridorWidth(int corridorID, double width) {
        if (corridorID > -1) {
            publicSpace.updateCorridorWidth(corridorID, width);
        }
    }

    /* ------------- generating public space ------------- */

    public void setPublicSpaceShapeTemp(Polygon publicSpaceShape) {
        this.publicSpace = new PublicSpace();
        this.publicSpace.setPublicSpaceShapeTemp(publicSpaceShape);
    }

    /**
     * initialize public traffic space
     *
     * @return void
     */
    public void initPublicSpace() {
        publicSpace.initPublicShape();
    }

    /**
     * update public space by a new set of control points
     *
     * @param publicSpaceNode_interact new control points
     * @return void
     */
    public void updatePublicSpace(List<WB_Point> publicSpaceNode_interact) {
        Coordinate[] ctrls = new Coordinate[publicSpaceNode_interact.size()];
        for (int i = 0; i < ctrls.length; i++) {
            ctrls[i] = ZTransform.WB_CoordToCoordinate(publicSpaceNode_interact.get(i));
        }
        publicSpace.updatePublicShape(ctrls);
    }

    /**
     * update public space by buffer distance
     *
     * @param dist buffer distance
     * @return void
     */
    public void updatePublicSpaceBuffer(double dist) {
        publicSpace.updatePublicShapeBuffer(dist);
    }

    /**
     * switch between round mode and smooth mode
     *
     * @param atriumID index of the atrium to switch
     * @return void
     */
    public void switchAtriumRoundType(int atriumID) {
        publicSpace.switchAtriumRoundType(atriumID);
    }

    /**
     * update the radius of rounding atrium
     *
     * @param atriumID index of atrium
     * @param radius   new radius
     * @return void
     */
    public void updateAtriumRoundRadius(int atriumID, double radius) {
        publicSpace.updateAtriumRoundRadius(atriumID, radius);
    }

    /**
     * update smooth times of the atrium
     *
     * @param atriumID index of atrium
     * @param times    smooth count times
     * @return void
     */
    public void updateAtriumSmoothTimes(int atriumID, int times) {
        publicSpace.updateAtriumSmoothTimes(atriumID, times);
    }

    /* ------------- generating structure grid ------------- */

    /**
     * setup structure grids
     *
     * @param gridNum number of structure grids
     * @param dist    column distance
     * @return void
     */
    public void initGrid(int gridNum, double dist) {
        ZRectCover zrc = new ZRectCover(siteBase.getBoundary(), gridNum);
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
            this.validGrids = union.contains(siteBase.getBoundary());
        }
    }

    /**
     * switch grid model between 8.4m and 9m
     *
     * @return void
     */
    public void switchGridModulus() {
        this.gridModelSwitch = !gridModelSwitch;
        if (gridModelSwitch) {
            for (StructureGrid g : grids) {
                g.updateModel(MallConst.STRUCTURE_MODEL);
            }
        } else {
            for (StructureGrid g : grids) {
                g.updateModel(MallConst.STRUCTURE_MODEL_2);
            }
        }
    }

    /**
    * description
    *
    * @param gridModulus
    * @return void
    */
    public void updateGridModulus(double gridModulus) {
        for (StructureGrid g : grids) {
            g.updateModel(gridModulus);
        }
    }

    /* ------------- generating shop cells ------------- */

    /**
     * initialize shop cells
     *
     * @param floorNum number of floor
     * @return void
     */
    public void initShopCells(int floorNum) {
        if (floorNum == 1) {
            List<LineString> publicSpaceLS = new ArrayList<>(ZTransform.PolygonToLineString(publicSpace.getPublicSpaceShape()));
            floors[floorNum - 1] = new MallFloor(floorNum, siteBase.getBoundary());
            floors[floorNum - 1].setStatus(0);
            this.floors[floorNum - 1].updateSubdivision(publicSpaceLS, grids);
        } else {
            // mainly here
            List<LineString> publicSpaceLS = new ArrayList<>(
                    ZTransform.PolygonToLineString(publicSpace.getPublicSpaceShape())
            );
            floors[floorNum - 1] = new MallFloor(floorNum, siteBase.getBoundary());
            floors[floorNum - 1].setStatus(0);
            Point verify = publicSpace.getPublicSpaceShape().getInteriorPoint();
            floors[floorNum - 1].setVerify(verify);
            this.floors[floorNum - 1].updateSubdivision(publicSpaceLS, grids);
        }
    }

    /* ------------- generating escalators ------------- */

    public void initEscalators() {
        publicSpace.initEscalators(mainTraffic.getMainTrafficCurve());
    }

    public void updateEscalatorPos(int atriumID) {
        publicSpace.updateEscalatorPos(atriumID);
    }

    /* ------------- generating evacuations ------------- */

    private List<Polygon> evacRectTemp;
    private List<Polygon> evacPoly;
    private Polygon newBound;
    private List<Stairway> stairways;

    public void initEvacuation2() {
        this.stairways = new ArrayList<>();
        // buffer
        Polygon bound = siteBase.getBoundary();
        WB_Polygon boundShape = ZTransform.PolygonToWB_Polygon(bound);
        WB_Polygon bufferBoundary = ZFactory.wbgf.createBufferedPolygons2D(boundShape, MallConst.STRUCTURE_MODEL * -0.5).get(0);
        WB_Polygon validBuffer = ZTransform.validateWB_Polygon(boundShape);

        // add start position
        List<ZPoint> generatePoints = new ArrayList<>();
        ZPoint start = new ZPoint(bound.getCoordinates()[0]);
        generatePoints.add(start);

        double recordDist = 0;
        double boundaryLength = ZGeoMath.getPolyLength(validBuffer);
        while (recordDist < boundaryLength - MallConst.EVACUATION_DIST) {
            ZPoint curr = generatePoints.get(generatePoints.size() - 1);
            WB_Point currP = curr.toWB_Point();
            int currEdgeIndex = ZGeoMath.pointOnWhichEdgeIndices(curr, validBuffer)[0];
            WB_Circle circle = new WB_Circle(curr.toWB_Point(), MallConst.EVACUATION_DIST);
            List<WB_Point> intersection = ZGeoMath.polylineCircleIntersection(validBuffer, circle);

            if (intersection.size() > 0) {
                double[] dists = new double[intersection.size()];
                for (int i = 0; i < intersection.size(); i++) {
                    WB_Point inter = intersection.get(i);
                    int[] onWhich = ZGeoMath.pointOnWhichEdgeIndices(new ZPoint(inter), validBuffer);
                    int interIndex = onWhich[0];
                    if (interIndex == currEdgeIndex) {
                        // on same edge
                        if (inter.getSqDistance2D(validBuffer.getPoint(interIndex)) >= currP.getSqDistance2D(validBuffer.getPoint(interIndex))) {
                            dists[i] = inter.getDistance2D(currP);
                        } else {
                            dists[i] = boundaryLength - inter.getDistance2D(currP);
                        }
                    } else {
                        // on different edge
                        int currIndex = currEdgeIndex;
                        double distAlong = 0;

                        currIndex = (currIndex + 1) % validBuffer.getNumberOfPoints();
                        distAlong += currP.getDistance2D(validBuffer.getPoint(currIndex));
                        while (currIndex != interIndex) {
                            distAlong += validBuffer.getPoint(currIndex).getDistance2D(validBuffer.getPoint((currIndex + 1) % validBuffer.getNumberOfPoints()));
                            currIndex = (currIndex + 1) % validBuffer.getNumberOfPoints();
                        }
                        distAlong += validBuffer.getPoint(currIndex).getDistance2D(inter);

                        dists[i] = distAlong;
                    }
                }

                int minIndex = ZMath.getMinIndex(dists);
                ZPoint p = new ZPoint(intersection.get(minIndex));
                generatePoints.add(p);
                recordDist += dists[minIndex];
            }
        }
        if (generatePoints.get(generatePoints.size() - 1).distance(generatePoints.get(0)) < MallConst.EVACUATION_DIST * 0.5) {
            generatePoints.remove(generatePoints.size() - 1);
        }
        System.out.println("final stairway num:  " + generatePoints.size());


//        BufferParameters parameters = new BufferParameters(0, 1, 2, 5.0D);
//        Geometry buffer = BufferOp.bufferOp(bound, MallConst.SHOP_SPAN_THRESHOLD[0] * -0.5, parameters);
////        WB_Polygon bufferBoundary = ZFactory.wbgf.createBufferedPolygons2D(siteBaseL.getBoundary(), MallConst.SHOP_SPAN_THRESHOLD[0] * -0.5).get(0);
//        WB_Polygon validBuffer = ZTransform.validateWB_Polygon(ZTransform.PolygonToWB_Polygon((Polygon) buffer));
//        List<ZPoint> dividePoints = ZGeoMath.splitPolyLineByThreshold(validBuffer, MallConst.EVACUATION_DIST, MallConst.EVACUATION_DIST - 10);

        this.evacRectTemp = new ArrayList<>();
        this.evacPoly = new ArrayList<>();

        for (ZPoint p : generatePoints) {
            double random = Math.random();
            if (random > 0.5) {
                stairways.add(new Stairway(0, p, validBuffer));
            } else {
                stairways.add(new Stairway(1, p, validBuffer));
            }
        }

//        for (ZPoint p : generatePoints) {
//            for (StructureGrid g : grids) {
//                Polygon rect = g.getRect();
//                if (rect.contains(p.toJtsPoint())) {
//                    double distTo10 = WB_GeometryOp.getDistance2D(p.toWB_Point(), g.getLat12().get(0).toWB_Segment());
//                    double distTo12 = WB_GeometryOp.getDistance2D(p.toWB_Point(), g.getLon10().get(0).toWB_Segment());
//
//                    if (distTo10 < g.getLengthUnit12()) {
//                        // 距离小于一个单元，靠近10边
//                        int n = (int) (distTo12 / g.getLengthUnit10());
//                        Coordinate[] coords = new Coordinate[5];
//                        if (n < 1) {
//                            // 012角
//                            coords[0] = g.getGridNodes()[0][0].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[0][1].centerWith(g.getGridNodes()[0][0]).toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[4][1].centerWith(g.getGridNodes()[4][0]).toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[4][0].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else if (n > g.getSize10() - 4) {
//                            coords[0] = g.getGridNodes()[g.getSize10() - 5][0].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[g.getSize10() - 5][1].centerWith(g.getGridNodes()[g.getSize10() - 5][0]).toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[g.getSize10() - 1][1].centerWith(g.getGridNodes()[g.getSize10() - 1][0]).toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[g.getSize10() - 1][0].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else {
//                            coords[0] = g.getGridNodes()[n][0].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[n][1].centerWith(g.getGridNodes()[n][0]).toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[n + 4][1].centerWith(g.getGridNodes()[n + 4][0]).toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[n + 4][0].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        }
//                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//                    } else if (distTo10 <= g.getLength12() && distTo10 > g.getLength12() - g.getLengthUnit12()) {
//                        // 靠近23边
//                        int size12 = g.getLat12().size();
//                        int n = (int) (distTo12 / g.getLengthUnit10());
//                        Coordinate[] coords = new Coordinate[5];
//                        if (n < 1) {
//                            coords[0] = g.getGridNodes()[0][size12 - 2].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[0][size12 - 1].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[2][size12 - 1].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[2][size12 - 2].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else if (n > g.getSize10() - 2) {
//                            coords[0] = g.getGridNodes()[n][size12 - 2].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[n][size12 - 1].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[n + 2][size12 - 1].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[n + 2][size12 - 2].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else {
//                            coords[0] = g.getGridNodes()[n - 1][size12 - 2].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[n - 1][size12 - 1].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[n + 1][size12 - 1].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[n + 1][size12 - 2].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        }
//                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//                    } else if (distTo12 < g.getLengthUnit10()) {
//                        // 靠近12边
//                        int n = (int) (distTo10 / g.getLengthUnit12());
//                        Coordinate[] coords = new Coordinate[5];
//                        if (n < 1) {
//                            coords[0] = g.getGridNodes()[1][0].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[0][0].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[0][2].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[1][2].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else if (n > g.getLat12().size() - 2) {
//                            coords[0] = g.getGridNodes()[1][n - 1].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[0][n - 1].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[0][n + 1].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[1][n + 1].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else {
//                            coords[0] = g.getGridNodes()[1][n].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[0][n].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[0][n + 2].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[1][n + 2].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        }
//                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//                    } else if (distTo12 <= g.getLength10() && distTo12 > g.getLength10() - g.getLengthUnit10()) {
//                        // 靠近30边
//                        int size10 = g.getSize10();
//                        int n = (int) (distTo10 / g.getLengthUnit12());
//                        Coordinate[] coords = new Coordinate[5];
//                        if (n < 1) {
//                            coords[0] = g.getGridNodes()[size10 - 1][0].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[size10 - 2][0].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[size10 - 2][2].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[size10 - 1][2].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else if (n > g.getLat12().size() - 2) {
//                            coords[0] = g.getGridNodes()[size10 - 1][n - 1].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[size10 - 2][n - 1].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[size10 - 2][n + 1].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[size10 - 1][n + 1].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        } else {
//                            coords[0] = g.getGridNodes()[size10 - 1][n].toJtsCoordinate();
//                            coords[1] = g.getGridNodes()[size10 - 2][n].toJtsCoordinate();
//                            coords[2] = g.getGridNodes()[size10 - 2][n + 2].toJtsCoordinate();
//                            coords[3] = g.getGridNodes()[size10 - 1][n + 2].toJtsCoordinate();
//                            coords[4] = coords[0];
//                        }
//                        evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//                    }
//                    break;
//                }
//            }
//        }
//
//        this.evacPoly = evacRectTemp;

//        // 布尔运算
//        for (Polygon rect : evacRectTemp) {
//            Geometry intersect = bound.intersection(rect);
//            if (intersect instanceof Polygon) {
//                evacPoly.add((Polygon) intersect);
//            }
//        }
//        Geometry difference = bound;
//        for (Polygon rect : evacRectTemp) {
//            difference = difference.difference(rect);
//        }
//        if (difference.getGeometryType().equals("MultiPolygon")) {
//            double area = 0;
//            for (int i = 0; i < difference.getNumGeometries(); i++) {
//                Geometry g = difference.getGeometryN(i);
//                if (g instanceof Polygon && g.getArea() > area) {
//                    this.newBound = (Polygon) g;
//                }
//            }
//        }
//        System.out.println("diff: " + difference.getGeometryType());
    }

    /* ------------- setter & getter ------------- */

    public SiteBase getSiteBase() {
        return siteBase;
    }

    public Coordinate[] getBoundaryNodes() {
        return siteBase.getBoundary().getCoordinates();
    }

    public String getBoundaryAreaAsString() {
        return boundaryArea > 0 ? String.format("%.2f", boundaryArea) + "㎡" : "";
    }

    public LineString getMainTrafficCurve() {
        return mainTraffic.getMainTrafficCurve();
    }

    public Polygon getMainTrafficBuffer() {
        return mainTraffic.getMainTrafficBuffer();
    }

    public List<WB_Point> getTrafficInnerNodes() {
        return mainTraffic.getInnerNodes();
    }

    public List<WB_Point> getTrafficEntryNodes() {
        return mainTraffic.getEntryNodes();
    }

    public void setRawAtrium_receive(Polygon[] rawAtrium_receive) {
        this.rawAtrium_receive = rawAtrium_receive;
    }

    public Coordinate[] getPublicSpaceCurveCtrls() {
        return publicSpace.getPublicSpaceShapeBufferCtrls();
    }

    public Polygon[] getAtriumCurrShapes() {
        return publicSpace.getAtriumCurrShapes();
    }

    public Polygon getAtriumCurrShape(int index) {
        return publicSpace.getAtriumCurrShapeN(index);
    }

    public List<WB_Point> getCorridorNode() {
        List<WB_Point> corridorNode = new ArrayList<>();
        for (ZLine l : publicSpace.getCorridorsLines()) {
            corridorNode.add(l.getPt0().toWB_Point());
            corridorNode.add(l.getPt1().toWB_Point());
        }
        return corridorNode;
    }

    public StructureGrid[] getGrids() {
        return grids;
    }

    public Polygon[] getGridRects() {
        Polygon[] rects = new Polygon[grids.length];
        for (int i = 0; i < grids.length; i++) {
            rects[i] = grids[i].getRect();
        }
        return rects;
    }

    public List<Shop> getShopCells(int floorNum) {
        return floors[floorNum - 1].getAllShops();
    }

    public void setShopCells(int floorNum, List<Polygon> shopCellPolys) {
        List<Shop> newShops = new ArrayList<>();
        for (Polygon p : shopCellPolys) {
            newShops.add(new Shop(p));
        }
        this.floors[floorNum - 1].setAllShops(newShops);
    }

    public List<Polygon> getEscalatorBounds() {
        return publicSpace.getEscalatorBounds();
    }

    public List<Integer> getEscalatorAtriumIDs() {
        return publicSpace.getEscalatorAtriumIDs();
    }

    public Polygon getEscalatorBoundN(int escalatorAtriumID) {
        return publicSpace.getEscalatorBoundN(escalatorAtriumID);
    }

    /* ------------- draw ------------- */

    public void displayLocal(PApplet app, WB_Render render, JtsRender jtsRender, int status, int floorNum) {
        app.pushStyle();
        switch (status) {
            case -1:
                break;
            case 0:
                displaySiteBoundaryLocal(app, jtsRender);
                break;
            case 1:
                displaySiteBoundaryLocal(app, jtsRender);
                displayTrafficLocal(app, jtsRender);
                break;
            case 2:
                displaySiteBoundaryLocal(app, jtsRender);
                displayTrafficLocal(app, jtsRender);
                displayMainCorridorLocal(app, jtsRender);
                break;
            case 3:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                break;
            case 4:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayGridLocal(app);
                break;
            case 5:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayGridLocal(app);
                displayShopCellsLocal(floorNum, app, jtsRender);
                break;
            case 6:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayShopCellsLocal(floorNum, app, jtsRender);
                displayEscalatorLocal(app, jtsRender);
                displayEscalatorRadiusLocal(app);
                break;
            case 7:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayShopCellsLocal(floorNum, app, jtsRender);
                displayGridLocal(app);
                displayEscalatorLocal(app, jtsRender);
                displayEvacuationLocal(app, jtsRender);
                if (test) {
                    displayEvacuationRadiusLocal(app);
                }

                break;
        }
        app.popStyle();
    }

    public boolean test = false;

    public void displaySiteBoundaryLocal(PApplet app, JtsRender render) {
        // draw boundary and site
        app.noFill();
        app.stroke(255);
        app.strokeWeight(6);
        render.drawGeometry(siteBase.getBoundary());
        app.stroke(255, 0, 0);
        app.strokeWeight(3);
        render.drawGeometry(siteBase.getSite());
    }

    public void displayTrafficLocal(PApplet app, JtsRender jtsRender) {
        // draw traffic route and buffer area
        app.stroke(255);
        app.strokeWeight(1);
        jtsRender.drawGeometry(mainTraffic.getMainTrafficCurve());
        app.stroke(52, 170, 187);
        app.strokeWeight(3);
        jtsRender.drawGeometry(mainTraffic.getMainTrafficBuffer());
    }

    public void displayMainCorridorLocal(PApplet app, JtsRender jtsRender) {
        app.strokeWeight(3);
        app.stroke(55, 103, 171);
        app.noFill();
        for (Polygon a : publicSpace.getAtriumTrimShapes()) {
            jtsRender.drawGeometry(a);
        }

        app.strokeWeight(1);
        app.stroke(0, 255, 0);
        for (int i = 0; i < publicSpace.getCorridorsLines().size(); i++) {
            publicSpace.getCorridorsLines().get(i).display(app);
        }

        app.fill(255);
        app.textSize(2);
        for (int i = 0; i < publicSpace.getAtriumTrimShapes().length; i++) {
            Polygon p = publicSpace.getAtriumTrimShapes()[i];
            app.pushMatrix();
            app.scale(1, -1);
            app.translate(0, (float) (-2 * p.getCentroid().getY()));
            app.text(String.format("%.2f", publicSpace.getAtriumCurrAreas()[i]), (float) p.getCentroid().getX(), (float) p.getCentroid().getY());
            app.popMatrix();
        }
    }

    public void displayPublicSpaceLocal(PApplet app, JtsRender jtsRender) {
        app.noFill();
        app.stroke(52, 170, 187);
        app.strokeWeight(3);
        jtsRender.drawGeometry(publicSpace.getPublicSpaceShape());
        for (Polygon p : publicSpace.getAtriumCurrShapes()) {
            jtsRender.drawGeometry(p);
        }

        app.fill(255);
        app.textSize(2);
        for (int i = 0; i < publicSpace.getAtriumTrimShapes().length; i++) {
            Polygon p = publicSpace.getAtriumTrimShapes()[i];
            app.pushMatrix();
            app.scale(1, -1);
            app.translate(0, (float) (-2 * p.getCentroid().getY()));
            app.text(String.format("%.2f", publicSpace.getAtriumCurrAreas()[i]), (float) p.getCentroid().getX(), (float) p.getCentroid().getY());
            app.popMatrix();
        }
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
        if (floors[floorNum - 1].getAllShops() != null) {
            app.strokeWeight(3);
            app.stroke(255);
            List<Shop> cells = floors[floorNum - 1].getAllShops();
            for (Shop s : cells) {
                jtsRender.drawGeometry(s.getShape());
            }
            app.noStroke();
            for (Shop s : floors[floorNum - 1].getAllShops()) {
                s.display(app, jtsRender);
            }

            app.fill(255);
            app.textSize(2);

            for (Shop s : floors[floorNum - 1].getAllShops()) {
                app.pushMatrix();
                app.scale(1, -1);
                app.translate(0, (float) (-2 * s.getCenter().getY()));
                s.displayText(app);
                app.popMatrix();
            }
        }
    }

    public void displayEscalatorLocal(PApplet app, JtsRender jtsRender) {
        app.noFill();
        app.stroke(255);
        app.strokeWeight(1);
        for (Polygon p : publicSpace.getEscalatorBounds()) {
            jtsRender.drawGeometry(p);
        }
        for (ZLine l : publicSpace.getEscalatorShapes()) {
            l.display(app);
        }
    }

    public void displayEscalatorRadiusLocal(PApplet app) {
        for (Polygon p : publicSpace.getEscalatorBounds()) {
            Point centroid = p.getCentroid();
            app.noFill();
            app.ellipse((float) centroid.getX(), (float) centroid.getY(), 2 * MallConst.ESCALATOR_DIST_MIN, 2 * MallConst.ESCALATOR_DIST_MIN);
            app.line(
                    (float) centroid.getX(),
                    (float) centroid.getY(),
                    (float) centroid.getX() + MallConst.ESCALATOR_DIST_MIN,
                    (float) centroid.getY()
            );
            app.fill(255);
            app.textSize(2);
            app.pushMatrix();
            app.scale(1, -1);
            app.translate(0, (float) (-2 * centroid.getY()));
            app.text("30m",
                    (float) centroid.getX() + 0.5f * MallConst.ESCALATOR_DIST_MIN,
                    (float) centroid.getY() + 1
            );
            app.popMatrix();
        }
    }

    public void displayEvacuationLocal(PApplet app, JtsRender jtsRender) {
        app.pushStyle();
        if (stairways != null) {
            app.stroke(255);
            for (Stairway s : stairways) {
                app.fill(80);
                jtsRender.drawGeometry(s.getBound());
                app.fill(255, 0, 0);
                s.getBase().displayAsPoint(app);
                for (ZLine l : s.getShapes()) {
                    l.display(app);
                }
            }
        }
//        if (evacPoly != null) {
//            app.stroke(255);
//            app.fill(80);
//            for (Polygon p : evacPoly) {
//                jtsRender.drawGeometry(p);
//                // display shape
//                ZPoint v01 = new ZPoint(
//                        p.getCoordinates()[1].getX() - p.getCoordinates()[0].getX(),
//                        p.getCoordinates()[1].getY() - p.getCoordinates()[0].getY()
//                );
//                ZPoint v03 = new ZPoint(
//                        p.getCoordinates()[3].getX() - p.getCoordinates()[0].getX(),
//                        p.getCoordinates()[3].getY() - p.getCoordinates()[0].getY()
//                );
//                ZPoint v01Nor = v01.normalize();
//                ZPoint v03Nor = v03.normalize();
//
//                app.line(
//                        new ZPoint(p.getCoordinates()[0]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[0]).add(v01Nor.scaleTo(2.4)).yf(),
//                        new ZPoint(p.getCoordinates()[3]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[3]).add(v01Nor.scaleTo(2.4)).yf()
//                );
//                app.line(
//                        new ZPoint(p.getCoordinates()[1]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[1]).add(v01Nor.scaleTo(2.4)).yf(),
//                        new ZPoint(p.getCoordinates()[2]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[2]).add(v01Nor.scaleTo(2.4)).yf()
//                );
//
//            }
//        }

//        if (newBound != null) {
//            app.stroke(255);
//            app.pushMatrix();
//            app.translate(500, 0);
//            jtsRender.drawGeometry(newBound);
//            app.popMatrix();
//        }

        app.popStyle();
    }

    public void displayEvacuationRadiusLocal(PApplet app) {
        app.pushStyle();
        for (Stairway s : stairways) {
            ZPoint base = s.getBase();
            app.noFill();
            app.stroke(255, 0, 0);
            app.strokeWeight(1.2f);
            app.ellipse(base.xf(), base.yf(), 100, 100);
            app.line(
                    base.xf(),
                    base.yf(),
                    base.xf() + 50,
                    base.yf()
            );
            app.fill(255, 0, 0);
            app.textSize(5);
            app.pushMatrix();
            app.scale(1, -1);
            app.translate(0, (float) (-2 * base.yf()));
            app.text("50m",
                    base.xf() + 0.5f * 25,
                    base.yf() + 1
            );
            app.popMatrix();
        }
        app.popStyle();
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
//        this.cellPolys_receive.set(floorNum - 1, cellPolys_receive);
    }

    public void setShopCells_receive(int floorNum, List<Shop> shopCell_receive) {
        this.floors[floorNum - 1].setAllShops(shopCell_receive);
    }

    public void setCellPolys_receive(List<List<WB_Polygon>> cellPolys_receive) {
//        this.cellPolys_receive = cellPolys_receive;
    }

    public MallFloor[] getFloors() {
        return floors;
    }

//    public List<WB_Segment> getGraphSegments(int floorNum) {
//        return floors[floorNum - 1].getGraph().toWB_Segments();
//    }

    public List<List<WB_Coord>> getBufferControlPoints(int floorNum) {
        return floors[floorNum - 1].getBufferControlPoints();
    }

//    public String getFloorStats(int floorNum) {
//        MallFloor floor = floors[floorNum - 1];
//
//        WB_Polygon bufferOut = ZFactory.wbgf.createBufferedPolygons2D(boundary_receive, MallConst.EVACUATION_WIDTH).get(0);
//        double bufferArea = Math.abs(bufferOut.getSignedArea());
//
////        double totalArea = Math.abs(boundary_receive.getSignedArea());
////        double shopArea = 0;
////        for (Polygon p : floor.getShopBlocks()) {
////            shopArea += p.getArea();
////        }
//        double shopArea = newBound.getArea();
//        for (Polygon ep : evacPoly) {
//            shopArea -= ep.getArea();
//        }
//
//        int shopNum = getShopCells(floorNum).size();
//        double trafficLength = 0;
//        for (ZEdge e : floor.getGraph().getAllEdges()) {
//            trafficLength += e.getLength();
//        }
//        double shopRatio = shopArea / bufferArea;
//
//        return "本层建筑面积 : " + String.format("%.2f", bufferArea) + " ㎡"
//                + "\n" + "可租赁面积 : " + String.format("%.2f", shopArea) + " ㎡"
//                + "\n" + "商铺总数量 : " + shopNum
//                + "\n" + "分层得铺率 : " + String.format("%.2f", shopRatio * 100) + " %"
//                + "\n" + "小铺率 : "
//                + "\n" + "动线长度 : " + String.format("%.2f", trafficLength) + " m";
//    }

    /* ------------- deprecated ------------- */

//    /**
//     * update traffic graph and buffer of current floor
//     *
//     * @param floorNum    current floor number
//     * @param dist        buffer distance
//     * @param curvePtsNum curve subdivision number
//     * @return void
//     */
//    public void generateGraphAndBuffer(int floorNum, double dist, int curvePtsNum) {
//        if (floorNum == 1) {
//            this.floors[floorNum - 1].updateGraph(innerNode_receive, entryNode_receive);
//        } else {
//            this.floors[floorNum - 1].updateGraph(innerNode_receive, new ArrayList<WB_Point>());
//        }
//        this.floors[floorNum - 1].updateBuffer(rawAtrium_receive, dist, curvePtsNum);
//
//        this.floors[floorNum - 1].disposeSplit();
//        this.floors[floorNum - 1].disposeSubdivision();
//        this.floors[floorNum - 1].disposeEvacuation();
//        this.floors[floorNum - 1].setStatus(1);
//    }
//
//    /**
//     * update block split and subdivision
//     *
//     * @param floorNum current floor number
//     * @return void
//     */
//    public void generateSubdivision(int floorNum) {
//        if (floorNum == 1) {
//            this.floors[floorNum - 1].updateSubdivision(bufferCurve_receive.get(0), grids);
//        } else {
//            this.floors[floorNum - 1].updateSubdivision(bufferCurve_receive.get(1), grids);
//        }
//
//        this.floors[floorNum - 1].disposeEvacuation();
//        this.floors[floorNum - 1].setStatus(2);
//    }
//
//    /**
//     * generate positions of evacuate stairways
//     *
//     * @param floorNum current floor number
//     * @return void
//     */
//    public void generateEvacuation(int floorNum) {
////        this.floors[floorNum - 1].updateEvacuation(cellPolys_receive.get(floorNum - 1));
//
//        this.floors[floorNum - 1].setStatus(3);
//    }
//
//

//    public void displayGraphLocal(int floorNum, PApplet app, WB_Render render) {
//        app.pushStyle();
//        app.stroke(255);
//        app.strokeWeight(1);
//        for (WB_Segment seg : getGraphSegments(floorNum)) {
//            render.drawSegment(seg);
//        }
//        app.popStyle();
//    }

    public void displayPartitionLocal(int floorNum, PApplet app, JtsRender jtsRender) {
        app.pushStyle();
        app.strokeWeight(3);
        app.stroke(255);
        if (floors[floorNum - 1].getAllShops() != null) {
            List<Shop> cells = floors[floorNum - 1].getAllShops();
            for (Shop s : cells) {
                jtsRender.drawGeometry(s.getShape());
            }
        }
//        if (floors[floorNum - 1].getAllSubLines() != null) {
//            for (LineString l : floors[floorNum - 1].getAllSubLines()) {
//                jtsRender.drawGeometry(l);
//            }
//        }

        if (floors[floorNum - 1].getAllShops() != null) {
            app.noStroke();
            for (Shop s : floors[floorNum - 1].getAllShops()) {
                s.display(app, jtsRender);
            }

            app.fill(255);
            app.textSize(2);

            for (Shop s : floors[floorNum - 1].getAllShops()) {
                app.pushMatrix();
                app.scale(1, -1);
                app.translate(0, (float) (-2 * s.getCenter().getY()));
                s.displayText(app);
                app.popMatrix();
            }
        }
        app.popStyle();
    }

//
//    /**
//     * converting status 1 geometries to JsonElement
//     *
//     * @param floorNum current floor
//     * @param elements list of JsonElement
//     * @param gson     Gson
//     * @return void
//     */
//    private void convertingStatus1(int floorNum, List<JsonElement> elements, Gson gson) {
//        // preparing data
//        List<WB_Segment> graphSegments = getGraphSegments(floorNum);
//        List<List<WB_Coord>> controlPoints = getBufferControlPoints(floorNum);
//
//        // converting to json
//        for (WB_Segment seg : graphSegments) {
//            Segments segments = WB_Converter.toSegments(seg);
//            JsonObject prop1 = new JsonObject();
//            prop1.addProperty("name", "treeEdges");
//            segments.setProperties(prop1);
//            elements.add(gson.toJsonTree(segments));
//        }
//        for (List<WB_Coord> splitPointsEach : controlPoints) {
//            Vertices bufferControlPointsEach = WB_Converter.toVertices(splitPointsEach, 3);
//            JsonObject prop2 = new JsonObject();
//            prop2.addProperty("name", "bufferControl");
//            bufferControlPointsEach.setProperties(prop2);
//            elements.add(gson.toJsonTree(bufferControlPointsEach));
//        }
//    }
//
//    /**
//     * converting status 2 geometries to JsonElement
//     *
//     * @param floorNum current floor
//     * @param elements list of JsonElement
//     * @param gson     Gson
//     * @return void
//     */
//    private void convertingStatus2(int floorNum, List<JsonElement> elements, Gson gson) {
//        // preparing data
//        List<Shop> allShops = floors[floorNum - 1].getAllCells();
//        List<WB_Polygon> allCells = new ArrayList<>();
//        for (Shop s : allShops) {
//            allCells.add(s.getShapeWB());
//        }
//
//        // converting to json
//        for (WB_Polygon p : allCells) {
//            Segments cell = WB_Converter.toSegments(p);
//            JsonObject prop = new JsonObject();
//            prop.addProperty("name", "shopCell");
//
//            double area = Math.abs(p.getSignedArea());
//            if (area > 2000) {
//                prop.addProperty("shopType", "anchor");
//            } else if (area > 400 && area <= 2000) {
//                prop.addProperty("shopType", "subAnchor");
//            } else if (area > 80 && area <= 400) {
//                prop.addProperty("shopType", "ordinary");
//            } else {
//                prop.addProperty("shopType", "invalid");
//            }
//
//            cell.setProperties(prop);
//            elements.add(gson.toJsonTree(cell));
//        }
//    }
//
//    /**
//     * converting status 3 geometries to JsonElement
//     *
//     * @param floorNum current floor
//     * @param elements list of JsonElement
//     * @param gson     Gson
//     * @return void
//     */
//    private void convertingStatus3(int floorNum, List<JsonElement> elements, Gson gson) {
//        // preparing data
//        List<WB_Point> evacuationPoints = floors[floorNum - 1].getEvacuationPoint();
//
//        // converting to json
//        Vertices evacuation = WB_Converter.toVertices(evacuationPoints, 3);
//        JsonObject prop = new JsonObject();
//        prop.addProperty("name", "evacuation");
//        evacuation.setProperties(prop);
//        elements.add(gson.toJsonTree(evacuation));
//    }
//
//    /* ------------- JSON sending ------------- */
//
//    /**
//     * convert backend geometries to ArchiJSON
//     * graph segments, buffer control points
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONGraphAndBuffer(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        convertingStatus1(floorNum, elements, gson);
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
//
//    /**
//     * convert backend geometries to ArchiJSON
//     * first-level subdivision cells
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONSubdivision(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        convertingStatus2(floorNum, elements, gson);
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
//
//    /**
//     * convert backend geometries to ArchiJSON
//     * evacuation points & segments
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONEvacuation(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        convertingStatus3(floorNum, elements, gson);
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
//
//    /**
//     * switch floor num: convert all geometries in one floor
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONFloor(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        // preparing data
//        int currentStatus = floors[floorNum - 1].getStatus();
//        int count = 1;
//        if (count <= currentStatus) {
//            convertingStatus1(floorNum, elements, gson);
//            count++; // ==2
//            if (count <= currentStatus) {
//                convertingStatus2(floorNum, elements, gson);
//                count++; // ==3
//                if (count <= currentStatus) {
//                    convertingStatus3(floorNum, elements, gson);
//                }
//            }
//        } else {
//            System.out.println("this floor hasn't been initialized due to some error");
//        }
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
}