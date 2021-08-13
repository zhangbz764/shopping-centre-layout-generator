package main;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import mallElementNew.AtriumFactory;
import mallElementNew.AtriumRaw;
import mallElementNew.Shop;
import math.ZGeoMath;
import org.locationtech.jts.geom.*;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.*;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Arrays;
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
    // 场地&建筑轮廓
    private Polygon site;                            // 场地轮廓
    private Polygon redLine;                         // 场地红线
    private Polygon boundary;                        // 建筑轮廓
    private int boundaryBase = 0;                       // 生成建筑轮廓的场地基点序号
    private List<Coordinate> boundaryNode_interact;       // 建筑轮廓控制点

    // 主路径
    private List<WB_Point> innerNode_interact;          // 动线中部控制点
    private List<WB_Point> entryNode_interact;          // 动线端头控制点

    // 原始中庭
    private Polygon mainTraffic_interact;               // 主路径区域（用于判断）
    private int selectedAtriumType = -1;                // 选择的中庭类型代号
    private List<AtriumRaw> rawAtriums;                 // 原始中庭
    private AtriumRaw selectedAtrium;                   // 选择的原始中庭
    private WB_Point[] atriumNode_interact;             // 选择的原始中庭的控制点
    private int atriumDragFlag = -1;                    // 拖拽中心 or 边界控制点
    private int atriumNodeID = -1;                      // 被拖拽的边界控制点序号

    // 交通空间轮廓
    private List<ZPoint> publicSpaceNode_interact;

    // 柱网
    private Polygon[] rect_interact;                    // 柱网矩形
    private Polygon selectedRect;                       // 选择的柱网
    private int selectedRectID = -1;                    // 选择的柱网序号
    private WB_Point[] rectNode_interact;               // 柱网控制点
    private WB_Line[] rectCentralLine;                  // 柱网矩形轴线（用于限制控制点）

    // 商铺
    private List<Polygon> shopCell_interact;            // 商铺划格（不同层）
    private List<Polygon> shopCell_selected;            // 选择的商铺
    private double shopArea = -1;

    // 中庭&走廊
    private List<WB_Point> corridorNode_interact;       // 走廊控制点
    private WB_Point[] selectedCorridorNode;            // 选择的走廊控制点

    private List<LineString> bufferCurve_interact;   // 动线边界曲线（不同层）
    private List<WB_Point> bufferCurveControl_interact;

    /* ------------- constructor ------------- */

    public MallInteract() {

    }

    /* ------------- utils ------------- */

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    public String getBoundaryArea() {
        return boundary != null ? String.format("%.2f", boundary.getArea()) + "㎡" : "";
    }

    public String getRentArea() {
        if (shopCell_interact != null && shopCell_interact.size() > 0) {
            shopArea = 0;
            for (Polygon p : shopCell_interact) {
                shopArea += p.getArea();
            }
            return String.format("%.2f", shopArea) + "㎡";
        } else {
            shopArea = -1;
            return "";
        }
    }

    public String getShopRatio() {
        return shopArea > 0 ? String.format("%.2f", (shopArea / boundary.getArea()) * 100) + "%" : "";
    }

    public String getSmallShooRatio() {
        if (shopArea > 0) {
            float small = 0;
            for (Polygon p : shopCell_interact) {
                if (p.getArea() < 150) {
                    small += 1;
                }
            }
            return String.format("%.2f", (small / shopCell_interact.size()) * 100) + "%";
        } else {
            return "";
        }
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
        this.site = ZTransform.WB_PolygonToPolygon(ZGeoMath.polygonFaceUp(_site));
        if (_boundary != null) {
            this.boundary = ZTransform.WB_PolygonToPolygon(ZGeoMath.polygonFaceUp(_boundary));
            this.boundaryNode_interact = new ArrayList<>();
            for (int i = 0; i < boundary.getNumPoints(); i++) {
                boundaryNode_interact.add(boundary.getCoordinates()[i]);
            }
        } else {
            WB_Polygon redLineSite = ZFactory.wbgf.createBufferedPolygons2D(
                    _site, -1 * redLineDist
            ).get(0);
            this.redLine = ZTransform.WB_PolygonToPolygon(ZGeoMath.polygonFaceUp(redLineSite));

            this.boundary = ZTransform.WB_PolygonToPolygon(
                    generateBoundary(ZTransform.PolygonToWB_Polygon(redLine), boundaryBase, siteBufferDist)
            );

            this.boundaryNode_interact = new ArrayList<>();
            for (int i = 0; i < boundary.getNumPoints(); i++) {
                boundaryNode_interact.add(boundary.getCoordinates()[i]);
            }
        }
    }

    /**
     * update site and boundary by given straightly
     *
     * @param _site     input site
     * @param _boundary input boundary
     * @return void
     */
    public void updateSiteBoundary(Polygon _site, Polygon _boundary) {
        this.site = _site;
        this.boundary = _boundary;
        this.boundaryNode_interact = new ArrayList<>();
        for (int i = 0; i < boundary.getNumPoints(); i++) {
            boundaryNode_interact.add(boundary.getCoordinates()[i]);
        }
        System.out.println(boundary);
    }

    /**
     * switch 4 possible L-shape boundary
     *
     * @param siteBufferDist distance to buffer
     * @return void
     */
    public void switchBoundary(double siteBufferDist) {
        this.boundaryBase = (boundaryBase + 1) % 4;
        this.boundary = ZTransform.WB_PolygonToPolygon(
                generateBoundary(ZTransform.PolygonToWB_Polygon(redLine), boundaryBase, siteBufferDist)
        );
        this.boundaryNode_interact = new ArrayList<>();
        for (int i = 0; i < boundary.getNumPoints(); i++) {
            boundaryNode_interact.add(boundary.getCoordinates()[i]);
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
            Coordinate c = boundaryNode_interact.get(i);
            if (distance(c.getX(), c.getY(), x, y) <= MallConst.BOUNDARY_NODE_R) {
                boundaryNode_interact.set(i, new Coordinate(x, y));
                if (i == 0) {
                    boundaryNode_interact.set(boundaryNode_interact.size() - 1, new Coordinate(x, y));
                }
                boundary = ZFactory.createPolygonFromList(boundaryNode_interact);
                break;
            }
        }
    }

    /**
     * generate an L-shape building boundary from a quad site
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
        WB_Polygon boundTemp = ZTransform.PolygonToWB_Polygon(boundary);
        for (WB_Point p : innerNode_interact) {
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                WB_Point point = new WB_Point(x, y);
                if (WB_GeometryOp.contains2D(point, boundTemp) && WB_GeometryOp.getDistance2D(point, boundTemp) > MallConst.TRAFFIC_NODE_R) {
                    p.set(x, y);
                }
                return;
            }
        }
        for (WB_Point p : entryNode_interact) {
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                WB_Point point = new WB_Point(x, y);
                p.set(WB_GeometryOp2D.getClosestPoint2D(point, ZTransform.WB_PolygonToWB_PolyLine(boundTemp).get(0)));
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
                for (AtriumRaw a : rawAtriums) {
                    WB_Polygon shape = ZTransform.PolygonToWB_Polygon(a.getShape());
                    if (WB_GeometryOp.contains2D(p, shape)) {
                        this.selectedAtrium = a;
                        this.atriumNode_interact = new WB_Point[a.getShapePtsNum() + 1];
                        atriumNode_interact[0] = a.getCenter().copy();
                        for (int i = 0; i < a.getShapePtsNum(); i++) {
                            atriumNode_interact[i + 1] = a.getShapePoints()[i].copy();
                        }
                        break;
                    }
                }
            } else {
                WB_Polygon shape = ZTransform.PolygonToWB_Polygon(selectedAtrium.getShape());
                if (!WB_GeometryOp.contains2D(p, shape)) {
                    this.selectedAtrium = null;
                    this.atriumNode_interact = null;
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
            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atriumNode_interact[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
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
            selectedAtrium.updateVectors();
            selectedAtrium.updateShape();

            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atriumNode_interact[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
        }
    }

    /**
     * scale selected atrium by area
     *
     * @param area input area
     * @return void
     */
    public void changeAtriumArea(double area) {
        if (selectedAtrium != null) {
            selectedAtrium.scaleShapeByArea(area);

            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atriumNode_interact[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
        }
    }

    /**
     * drag to change an atrium's position or shape
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void dragUpdateAtrium(double x, double y) {
        if (selectedAtrium != null) {
            WB_Point center = atriumNode_interact[0];
            if (distance(center.xd(), center.yd(), x, y) <= MallConst.ATRIUM_POS_R) {
                WB_Point point = new WB_Point(x, y);
                if (mainTraffic_interact.contains(ZFactory.jtsgf.createPoint(new Coordinate(x, y)))) {
                    center.set(x, y);
                }
//                else {
//                    WB_PolyLine pl = ZTransform.PolygonToWB_PolyLine(mainTraffic_interact).get(0);
//                    WB_Point closest = WB_GeometryOp.getClosestPoint2D(point, pl);
//                    center.set(closest);
//                }
                atriumDragFlag = 0;
            } else {
                for (int i = 1; i < atriumNode_interact.length; i++) {
                    WB_Point p = atriumNode_interact[i];
                    if (distance(p.xd(), p.yd(), x, y) <= MallConst.ATRIUM_CTRL_R) {
                        p.set(x, y);
                        atriumDragFlag = 1;
                        atriumNodeID = i;
                        break;
                    }
                }
            }
        }
    }

    /**
     * release mouse to update
     *
     * @return void
     */
    public void releaseUpdateAtrium() {
        if (atriumDragFlag == 0) {
            // move by center
            selectedAtrium.moveByCenter(atriumNode_interact[0]);
            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atriumNode_interact[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
            atriumDragFlag = -1;
        } else if (atriumDragFlag == 1) {
            // update shape
            selectedAtrium.updateShapeByArea(atriumNode_interact[atriumNodeID], atriumNodeID - 1);
            atriumNode_interact[0] = selectedAtrium.getCenter().copy();
            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atriumNode_interact[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
            atriumDragFlag = -1;
            atriumNodeID = -1;
        }
    }

    /* ------------- public space interact ------------- */

    public void dragUpdatePublicSpace(double x, double y) {
        for (ZPoint p : publicSpaceNode_interact) {
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.ATRIUM_CTRL_R) {
                p.set(x, y);
                return;
            }
        }
    }

    /* ------------- structure grid interact ------------- */

    /**
     * select grid rectangle
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void selectGridRect(double x, double y) {
        Point mouse = ZFactory.jtsgf.createPoint(new Coordinate(x, y));

        if (selectedRect == null) {
            for (int i = 0; i < rect_interact.length; i++) {
                Polygon p = rect_interact[i];
                if (p.contains(mouse)) {
                    selectedRect = p;
                    selectedRectID = i;
                    rectNode_interact = new WB_Point[4];
                    for (int j = 0; j < rectNode_interact.length; j++) {
                        Coordinate c1 = p.getCoordinates()[j];
                        Coordinate c2 = p.getCoordinates()[j + 1];
                        rectNode_interact[j] = new WB_Point(0.5 * (c1.x + c2.x), 0.5 * (c1.y + c2.y));
                    }
                    rectCentralLine = new WB_Line[2];
                    for (int j = 0; j < rectCentralLine.length; j++) {
                        WB_Point p1 = rectNode_interact[j];
                        WB_Point p2 = rectNode_interact[j + 2];
                        rectCentralLine[j] = new WB_Line(p1, p2.sub(p1));
                    }
                    break;
                }
            }
        } else {
            if (!selectedRect.contains(mouse)) {
                unselectGridRect();
            }
        }
    }

    /**
     * clear selected grid rectangle
     *
     * @return void
     */
    public void unselectGridRect() {
        selectedRect = null;
        selectedRectID = -1;
        rectCentralLine = null;
        rectNode_interact = null;
    }

    /**
     * drag to update grid rectangle
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void dragUpdateGrid(double x, double y) {
        if (selectedRect != null) {
            for (int i = 0; i < rectNode_interact.length; i++) {
                WB_Point p = rectNode_interact[i];
                if (distance(p.xd(), p.yd(), x, y) <= MallConst.STRUCTURE_CTRL_R) {
                    Coordinate[] coords = selectedRect.getCoordinates();
                    double[] d1 = new double[]{coords[i].x - p.xd(), coords[i].y - p.yd()};
                    double[] d2 = new double[]{coords[i + 1].x - p.xd(), coords[i + 1].y - p.yd()};

                    p.set(WB_GeometryOp.getClosestPoint2D(new WB_Point(x, y), rectCentralLine[i & 1]));

                    // set coordinates
                    coords[i].setCoordinate(new Coordinate(p.xd() + d1[0], p.yd() + d1[1]));
                    coords[i + 1].setCoordinate(new Coordinate(p.xd() + d2[0], p.yd() + d2[1]));
                    if (i == 0) {
                        coords[4] = coords[i];
                    } else if (i == 3) {
                        coords[0] = coords[i + 1];
                    }

                    // rebuild rectangle
                    selectedRect = ZFactory.jtsgf.createPolygon(coords);
                    rect_interact[selectedRectID] = selectedRect;

                    // set interact nodes and central lines
                    int i1 = (i + 1) % rectNode_interact.length;
                    int i2 = (i + 3) % rectNode_interact.length;
                    rectNode_interact[i1].set(0.5 * (coords[i1].x + coords[i1 + 1].x), 0.5 * (coords[i1].y + coords[i1 + 1].y));
                    rectNode_interact[i2].set(0.5 * (coords[i2].x + coords[i2 + 1].x), 0.5 * (coords[i2].y + coords[i2 + 1].y));

                    rectCentralLine[((i & 1) + 1) % 2] = new WB_Line(rectNode_interact[i1], rectNode_interact[i2].sub(rectNode_interact[i1]));
                    break;
                }
            }
        }
    }

    /* ------------- shop cell interact ------------- */

    /**
     * select shop cell
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void selectShopCell(double x, double y) {
        int size = shopCell_interact.size();
        Point pointer = ZFactory.jtsgf.createPoint(new Coordinate(x, y));
        for (int i = 0; i < size; i++) {
            Polygon cell = shopCell_interact.get(i);
            if (cell.contains(pointer)) {
                if (this.shopCell_selected.contains(cell)) {
                    shopCell_selected.remove(cell);
                } else {
                    shopCell_selected.add(cell);
                }
                break;
            }
        }
    }

    /**
     * union selected shop cells
     *
     * @return void
     */
    public void unionShopCell() {
        if (shopCell_selected.size() > 1) {
            shopCell_interact.removeAll(shopCell_selected);

            Polygon[] polygons = new Polygon[shopCell_selected.size()];
            for (int i = 0; i < shopCell_selected.size(); i++) {
                polygons[i] = shopCell_selected.get(i);
            }
            GeometryCollection collection = ZFactory.jtsgf.createGeometryCollection(polygons);
            Geometry union = collection.buffer(0);

            if (union instanceof Polygon) {
                shopCell_interact.add((Polygon) union);
            } else if (union instanceof MultiPolygon) {
                for (int i = 0; i < union.getNumGeometries(); i++) {
                    shopCell_interact.add((Polygon) union.getGeometryN(i));
                }
            } else {
                System.out.println(union.getGeometryType());
            }
        }
        shopCell_selected.clear();
    }

    /* ------------- main corridor interact ------------- */

    /**
     * select corridor node
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void selectCorridorNode(double x, double y) {
        if (selectedCorridorNode == null) {
            for (int i = 0; i < corridorNode_interact.size(); i++) {
                WB_Point p0, p1;

                WB_Point curr = corridorNode_interact.get(i);
                if (distance(curr.xd(), curr.yd(), x, y) <= MallConst.CORRIDOR_NODE_R) {
                    if (i % 2 == 0) {
                        p0 = curr;
                        p1 = corridorNode_interact.get(i + 1);
                    } else {
                        p1 = curr;
                        p0 = corridorNode_interact.get(i - 1);
                    }
                    this.selectedCorridorNode = new WB_Point[]{p0, p1};
                    break;
                }
            }
        } else {
            WB_Point p0 = selectedCorridorNode[0];
            WB_Point p1 = selectedCorridorNode[1];
            if (distance(p0.xd(), p0.yd(), x, y) > MallConst.CORRIDOR_NODE_R
                    &&
                    distance(p1.xd(), p1.yd(), x, y) > MallConst.CORRIDOR_NODE_R
            ) {
                selectedCorridorNode = null;
            }
        }
    }

    /**
     * drag to change corridor node
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void dragUpdateCorridor(double x, double y) {
        if (selectedCorridorNode != null) {
            for (WB_Point p : selectedCorridorNode) {
                if (distance(p.xd(), p.yd(), x, y) <= MallConst.CORRIDOR_NODE_R) {
                    WB_Point point = new WB_Point(x, y);
                    p.set(x, y);
                    break;
                }
            }
        }
    }

    /**
     * remove the corridor node (divide line)
     *
     * @param
     * @return void
     */
    public void removeCorridorNode() {
        if (selectedCorridorNode != null) {
            corridorNode_interact.remove(selectedCorridorNode[0]);
            corridorNode_interact.remove(selectedCorridorNode[1]);
            selectedCorridorNode = null;
        }
    }

    /* ------------- buffer curve shape interact ------------- */

    public void switchFloor(char floorKey) {

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

    /* ------------- setter & getter ------------- */

    public Polygon getSite() {
        return site;
    }

    public Polygon getBoundary() {
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

    public void setRawAtriums(List<AtriumRaw> rawAtriums) {
        this.rawAtriums = rawAtriums;
    }

    public List<Polygon> getRawAtriumShapes() {
        List<Polygon> atriums = new ArrayList<>();
        for (AtriumRaw a : rawAtriums) {
            atriums.add(a.getShape());
        }
        return atriums;
    }

    public void setPublicSpaceNode_interact(List<ZPoint> publicSpaceNode_interact) {
        this.publicSpaceNode_interact = publicSpaceNode_interact;
    }

    public List<ZPoint> getPublicSpaceNode_interact() {
        return publicSpaceNode_interact;
    }

    public void setRect_interact(Polygon[] rect_interact) {
        this.rect_interact = rect_interact;
    }

    public Polygon getSelectedRect() {
        return selectedRect;
    }

    public int getSelectedRectID() {
        return selectedRectID;
    }

    public void setShopCell_interact(List<Shop> currentShops) {
        this.shopCell_interact = new ArrayList<>();
        for (Shop shop : currentShops) {
            shopCell_interact.add(shop.getShape());
        }
        this.shopCell_selected = new ArrayList<>();
    }

    public List<Polygon> getShopCell_interact() {
        return shopCell_interact;
    }

    public void setCorridorNode_interact(List<WB_Point> corridorNode_interact) {
        this.corridorNode_interact = corridorNode_interact;
    }

    public List<WB_Point> getCorridorNode_interact() {
        return corridorNode_interact;
    }

    //    public void setBufferCurve_interact(List<LineString> bufferCurve_interact) {
//        this.bufferCurve_interact = bufferCurve_interact;
//    }
//
//    public List<LineString> getBufferCurve_interact() {
//        return bufferCurve_interact;
//    }

    /* ------------- draw ------------- */

    public void displayLocal(PApplet app, WB_Render render, JtsRender jtsRender, int status) {
        app.pushStyle();
        switch (status) {
            case -1:
                break;
            case 0:
                displaySiteBoundary(app, jtsRender);
                break;
            case 1:
                displayTraffic(app, render);
                break;
            case 2:
                displayRawAtrium(app, jtsRender);
                if (selectedAtrium != null) {
                    displaySelectedAtrium(app, jtsRender);
                }
                break;
            case 3:
                displayPublicSpaceNodes(app);
                break;
            case 4:
                if (selectedRect != null) {
                    displaySelectedGrid(app, jtsRender);
                }
                break;
            case 5:
                if (shopCell_selected != null) {
                    displaySelectedCell(app, jtsRender);
                }
                break;
            case 6:
                displayCorridorNode(app);
                if (selectedCorridorNode != null) {
                    displaySelectedCorridorNode(app);
                }
                break;
        }
        app.popStyle();
    }

    private void displaySiteBoundary(PApplet app, JtsRender render) {
        // draw boundary control nodes
        app.noStroke();
        app.fill(255, 97, 136);
        for (Coordinate p : boundaryNode_interact) {
            app.ellipse((float) p.getX(), (float) p.getY(), (float) MallConst.BOUNDARY_NODE_R, (float) MallConst.BOUNDARY_NODE_R);
        }

        // draw boundary and site
        app.noFill();
        app.stroke(255);
        app.strokeWeight(6);
        render.drawGeometry(boundary);
        app.stroke(255, 0, 0);
        app.strokeWeight(3);
        render.drawGeometry(site);
    }

    private void displayTraffic(PApplet app, WB_Render render) {
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

    private void displayRawAtrium(PApplet app, JtsRender render) {
        // draw all raw atriums
        app.stroke(55, 103, 171);
        app.strokeWeight(2);
        app.noFill();
        for (AtriumRaw an : rawAtriums) {
            render.drawGeometry(an.getShape());
        }
    }

    private void displaySelectedAtrium(PApplet app, JtsRender render) {
        // draw center
        app.noStroke();
        app.fill(255, 97, 136);
        app.ellipse(atriumNode_interact[0].xf(), atriumNode_interact[0].yf(), MallConst.ATRIUM_POS_R, MallConst.ATRIUM_POS_R);

        // draw control points
        app.fill(169, 210, 118);
        for (int i = 1; i < atriumNode_interact.length; i++) {
            app.ellipse(atriumNode_interact[i].xf(), atriumNode_interact[i].yf(), MallConst.ATRIUM_CTRL_R, MallConst.ATRIUM_CTRL_R);
        }

        // draw shape
        app.stroke(0, 255, 0);
        app.strokeWeight(4);
        app.noFill();
        render.drawGeometry(selectedAtrium.getShape());
    }

    private void displayPublicSpaceNodes(PApplet app){
        app.noStroke();
        app.fill(169, 210, 118);
        for (ZPoint p : publicSpaceNode_interact) {
            app.ellipse(p.xf(), p.yf(), MallConst.ATRIUM_CTRL_R, MallConst.ATRIUM_CTRL_R);
        }
    }

    private void displaySelectedGrid(PApplet app, JtsRender jtsRender) {
        // draw rect
        app.stroke(0, 255, 0);
        app.strokeWeight(4);
        app.noFill();
        jtsRender.drawGeometry(selectedRect);

        // draw control points
        app.noStroke();
        app.fill(255, 97, 136);
        for (WB_Point p : rectNode_interact) {
            app.ellipse(p.xf(), p.yf(), MallConst.STRUCTURE_CTRL_R, MallConst.STRUCTURE_CTRL_R);
        }
    }

    private void displaySelectedCell(PApplet app, JtsRender jtsRender) {
        app.noFill();
        app.stroke(0, 255, 0);
        app.strokeWeight(5);
        for (Polygon p : shopCell_selected) {
            jtsRender.drawGeometry(p);
        }
    }

    private void displayCorridorNode(PApplet app) {
        app.stroke(0, 255, 0);
        for (int i = 0; i < corridorNode_interact.size(); i += 2) {
            WB_Point p1 = corridorNode_interact.get(i);
            WB_Point p2 = corridorNode_interact.get(i + 1);
            app.line(p1.xf(), p1.yf(), p2.xf(), p2.yf());
        }
        app.noStroke();
        app.fill(80);
        for (WB_Point p : corridorNode_interact) {
            app.ellipse(p.xf(), p.yf(), MallConst.CORRIDOR_NODE_R, MallConst.CORRIDOR_NODE_R);
        }
    }

    private void displaySelectedCorridorNode(PApplet app) {
        app.fill(255, 97, 136);
        for (WB_Point p : selectedCorridorNode) {
            app.ellipse(p.xf(), p.yf(), MallConst.CORRIDOR_NODE_R, MallConst.CORRIDOR_NODE_R);
        }
    }
}
