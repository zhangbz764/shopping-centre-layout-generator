package main;

import basicGeometry.ZFactory;
import basicGeometry.ZPoint;
import mallElementNew.AtriumRaw;
import mallElementNew.AtriumRawManager;
import mallElementNew.Shop;
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
    // 场地&建筑轮廓
    private int boundaryBase = 0;                       // 生成建筑轮廓的场地基点序号
    private Coordinate[] boundary_controllers;          // 建筑轮廓控制点

    // 主路径 & 原始中庭
    private boolean trafficOrAtrium = true;             // 路径交互 or 中庭交互
    private List<WB_Point> traffic_innerControllers;    // 动线中部控制点
    private List<WB_Point> traffic_entryControllers;    // 动线端头控制点

    private AtriumRawManager atriumRawManager;
    private AtriumRaw selectedAtrium;                   // 选择的原始中庭
    private int selectedAtriumType = -1;                // 选择的中庭类型代号
    private WB_Point[] atrium_controllers;              // 选择的原始中庭的控制点
    private int atriumDragFlag = -1;                    // 拖拽中心 or 边界控制点
    private int atriumNodeID = -1;                      // 被拖拽的边界控制点序号
    private Polygon mainTraffic_buffer;               // 主路径区域（用于判断）

    // 空中走廊
    private List<WB_Point> corridorNode_interact;       // 走廊控制点
    private WB_Point[] selectedCorridorNode;            // 选择的走廊控制点
    private int selectedCorridorID = -1;

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


    private List<LineString> bufferCurve_interact;   // 动线边界曲线（不同层）
    private List<WB_Point> bufferCurveControl_interact;

    /* ------------- constructor ------------- */

    public MallInteract() {

    }

    /* ------------- utils ------------- */

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
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
        return "";
//        return shopArea > 0 ? String.format("%.2f", (shopArea / boundary.getArea()) * 100) + "%" : "";
    }

    public String getSmallShopRatio() {
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
     * switch 4 possible L-shape boundary
     *
     * @param siteBufferDist distance to buffer
     * @return void
     */
    public void switchBoundary(double siteBufferDist) {
        this.boundaryBase = (boundaryBase + 1) % 4;
    }

    /**
     * drag update to change the boundary shape manually
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void dragUpdateBoundary(double x, double y) {
        for (int i = 0; i < boundary_controllers.length - 1; i++) {
            Coordinate c = boundary_controllers[i];
            if (distance(c.getX(), c.getY(), x, y) <= MallConst.BOUNDARY_NODE_R) {
                boundary_controllers[i] = new Coordinate(x, y);
                if (i == 0) {
                    boundary_controllers[boundary_controllers.length - 1] = new Coordinate(x, y);
                }
                break;
            }
        }
    }

    /* ------------- traffic & raw atrium interact ------------- */

    /**
     * switch the controller of traffic or atrium
     *
     * @return void
     */
    public void reverseTrafficOrAtrium() {
        this.trafficOrAtrium = !trafficOrAtrium;
        if (trafficOrAtrium) {
            this.selectedAtrium = null;
            this.atrium_controllers = null;
        }
    }

    /**
     * initialize the AtriumRawManager by given central traffic curve
     *
     * @param _mainTrafficCurve central traffic curve
     * @return void
     */
    public void initAtriumRawManager(WB_PolyLine _mainTrafficCurve) {
        this.atriumRawManager = new AtriumRawManager(_mainTrafficCurve);
    }

    /**
     * drag update to change the main traffic or raw atriums manually
     *
     * @param x        pointer x
     * @param y        pointer y
     * @param boundary boundary polygon
     * @return void
     */
    public void dragUpdateTrafficAtrium(double x, double y, Polygon boundary) {
        if (trafficOrAtrium) {
            // 主路径控制点显示
            WB_Polygon boundTemp = ZTransform.PolygonToWB_Polygon(boundary);
            for (WB_Point p : traffic_innerControllers) {
                if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                    WB_Point point = new WB_Point(x, y);
                    if (WB_GeometryOp.contains2D(point, boundTemp) && WB_GeometryOp.getDistance2D(point, boundTemp) > MallConst.TRAFFIC_NODE_R) {
                        p.set(x, y);
                    }
                    return;
                }
            }
            for (WB_Point p : traffic_entryControllers) {
                if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                    WB_Point point = new WB_Point(x, y);
                    p.set(WB_GeometryOp2D.getClosestPoint2D(point, ZTransform.WB_PolygonToWB_PolyLine(boundTemp).get(0)));
                    return;
                }
            }
        } else {
            // 主路径控制点关闭
            if (selectedAtrium != null) {
                WB_Point center = atrium_controllers[0];
                if (distance(center.xd(), center.yd(), x, y) <= MallConst.ATRIUM_POS_R) {
                    WB_Point point = new WB_Point(x, y);
                    if (mainTraffic_buffer.contains(ZFactory.jtsgf.createPoint(new Coordinate(x, y)))) {
                        center.set(x, y);
                    }
                    atriumDragFlag = 0;
                } else {
                    for (int i = 1; i < atrium_controllers.length; i++) {
                        WB_Point p = atrium_controllers[i];
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
    }

    /**
     * release mouse to update
     *
     * @return void
     */
    public void releaseUpdateAtrium() {
        if (atriumDragFlag == 0) {
            // move by center
            selectedAtrium.moveByCenter(atrium_controllers[0]);
            atriumRawManager.removeAtriumRaw(selectedAtrium);
            atriumRawManager.addAtriumRaw(selectedAtrium);
            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atrium_controllers[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
            atriumRawManager.validateAtriumRaw();

            atriumDragFlag = -1;
        } else if (atriumDragFlag == 1) {
            // update shape
            selectedAtrium.updateShapeByArea(atrium_controllers[atriumNodeID], atriumNodeID - 1);
            atrium_controllers[0] = selectedAtrium.getCenter().copy();
            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atrium_controllers[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
            atriumRawManager.validateAtriumRaw();

            atriumDragFlag = -1;
            atriumNodeID = -1;
        }
    }

    /**
     * click update: add atrium or select atrium
     *
     * @param x pointer x
     * @param y pointer y
     * @return void
     */
    public void clickUpdateAtrium(double x, double y) {
        if (!trafficOrAtrium) {
            WB_Point p = new WB_Point(x, y);
            if (selectedAtriumType > -1) {
                // add atrium
                if (mainTraffic_buffer.contains(ZFactory.jtsgf.createPoint(new Coordinate(x, y)))) {
                    switch (selectedAtriumType) {
                        case 0:
                            atriumRawManager.createAtrium3(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                        case 1:
                            atriumRawManager.createAtrium4(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                        case 2:
                            atriumRawManager.createAtrium4_(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                        case 3:
                            atriumRawManager.createAtrium5(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                        case 4:
                            atriumRawManager.createAtrium6(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                        case 5:
                            atriumRawManager.createAtrium6_(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                        case 6:
                            atriumRawManager.createAtrium7(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                        case 7:
                            atriumRawManager.createAtrium8(p, MallConst.ATRIUM_AREA_INIT, false);
                            break;
                    }
                    selectedAtriumType = -1;
                }
            } else {
                // select / unselect atrium
                if (selectedAtrium == null) {
                    for (AtriumRaw a : atriumRawManager.getAtriumRaws()) {
                        WB_Polygon shape = ZTransform.PolygonToWB_Polygon(a.getShape());
                        if (WB_GeometryOp.contains2D(p, shape)) {
                            this.selectedAtrium = a;
                            this.atrium_controllers = new WB_Point[a.getShapePtsNum() + 1];
                            atrium_controllers[0] = a.getCenter().copy();
                            for (int i = 0; i < a.getShapePtsNum(); i++) {
                                atrium_controllers[i + 1] = a.getShapePoints()[i].copy();
                            }
                            break;
                        }
                    }
                } else {
                    WB_Polygon shape = ZTransform.PolygonToWB_Polygon(selectedAtrium.getShape());
                    if (!WB_GeometryOp.contains2D(p, shape)) {
                        this.selectedAtrium = null;
                        this.atrium_controllers = null;
                    }
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
            atriumRawManager.removeAtriumRaw(selectedAtrium);
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
                atrium_controllers[i + 1] = selectedAtrium.getShapePoints()[i].copy();
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
            selectedAtrium.rotateByAngle(angle);
            for (int i = 0; i < selectedAtrium.getShapePtsNum(); i++) {
                atrium_controllers[i + 1] = selectedAtrium.getShapePoints()[i].copy();
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
                atrium_controllers[i + 1] = selectedAtrium.getShapePoints()[i].copy();
            }
        }
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
                    this.selectedCorridorID = (int) (i * 0.5);
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
                selectedCorridorID = -1;
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
        if (selectedCorridorNode != null && selectedCorridorID > -1) {
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
     * @return void
     */
    public void removeCorridorNode() {
        if (selectedCorridorNode != null) {
            corridorNode_interact.remove(selectedCorridorNode[0]);
            corridorNode_interact.remove(selectedCorridorNode[1]);
            selectedCorridorNode = null;
            selectedCorridorID = -1;
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

    /* ------------- setter & getter ------------- */

    public int getBoundaryBase() {
        return boundaryBase;
    }

    public void setBoundary_controllers(Coordinate[] boundary_controllers) {
        this.boundary_controllers = boundary_controllers;
    }

    public Coordinate[] getBoundary_controllers() {
        return boundary_controllers;
    }

    public boolean getTrafficOrAtrium() {
        return trafficOrAtrium;
    }

    public void setTraffic_innerControllers(List<WB_Point> traffic_innerControllers) {
        this.traffic_innerControllers = traffic_innerControllers;
    }

    public void setTraffic_entryControllers(List<WB_Point> traffic_entryControllers) {
        this.traffic_entryControllers = traffic_entryControllers;
    }

    public List<WB_Point> getTraffic_innerControllers() {
        return traffic_innerControllers;
    }

    public List<WB_Point> getTraffic_entryControllers() {
        return traffic_entryControllers;
    }

    public AtriumRawManager getAtriumRawManager() {
        return atriumRawManager;
    }

    public void setMainTraffic_buffer(Polygon mainTraffic_buffer) {
        this.mainTraffic_buffer = mainTraffic_buffer;
    }

    public void setSelectedAtriumType(int selectedAtriumType) {
        this.selectedAtriumType = selectedAtriumType;
    }

    public Polygon[] getAtriumRawShapes() {
        Polygon[] atriums = new Polygon[atriumRawManager.getNumAtriumRaw()];
        for (int i = 0; i < atriums.length; i++) {
            AtriumRaw a = atriumRawManager.getAtriumRaws().get(i);
            atriums[i] = a.getShape();
        }
        return atriums;
    }

    public void setCorridorNode_interact(List<WB_Point> corridorNode_interact) {
        this.corridorNode_interact = corridorNode_interact;
    }

    public List<WB_Point> getAllCorridorNode_interact() {
        return corridorNode_interact;
    }

    public int getSelectedCorridorID() {
        return selectedCorridorID;
    }

    public WB_Point[] getSelectedCorridorNode() {
        return selectedCorridorNode;
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
                if (trafficOrAtrium) {
                    displayTraffic(app, render);
                }
                displayRawAtrium(app, jtsRender, render);
                if (selectedAtrium != null) {
                    displaySelectedAtrium(app, jtsRender);
                }
                break;
            case 2:
                displayCorridorNode(app);
                if (selectedCorridorNode != null) {
                    displaySelectedCorridorNode(app);
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
                break;
        }
        app.popStyle();
    }

    private void displaySiteBoundary(PApplet app, JtsRender render) {
        // draw boundary control nodes
        app.noStroke();
        app.fill(255, 97, 136);
        for (Coordinate p : boundary_controllers) {
            app.ellipse((float) p.getX(), (float) p.getY(), (float) MallConst.BOUNDARY_NODE_R, (float) MallConst.BOUNDARY_NODE_R);
        }
    }

    private void displayTraffic(PApplet app, WB_Render render) {
        // draw traffic control nodes
        app.noStroke();
        app.fill(255, 97, 136);
        for (WB_Point p : traffic_innerControllers) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.TRAFFIC_NODE_R, (float) MallConst.TRAFFIC_NODE_R);
        }
        app.fill(128);
        for (WB_Point p : traffic_entryControllers) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.TRAFFIC_NODE_R, (float) MallConst.TRAFFIC_NODE_R);
        }
        app.stroke(128);
        app.strokeWeight(0.5f);
        for (int i = 0; i < traffic_innerControllers.size() - 1; i++) {
            app.line(
                    traffic_innerControllers.get(i).xf(), traffic_innerControllers.get(i).yf(),
                    traffic_innerControllers.get(i + 1).xf(), traffic_innerControllers.get(i + 1).yf()
            );
        }
        app.line(
                traffic_entryControllers.get(0).xf(), traffic_entryControllers.get(0).yf(),
                traffic_innerControllers.get(0).xf(), traffic_innerControllers.get(0).yf()
        );
        app.line(
                traffic_entryControllers.get(1).xf(), traffic_entryControllers.get(1).yf(),
                traffic_innerControllers.get(traffic_innerControllers.size() - 1).xf(), traffic_innerControllers.get(traffic_innerControllers.size() - 1).yf()
        );
    }

    private void displayRawAtrium(PApplet app, JtsRender jtsRender, WB_Render render) {
        // draw all raw atriums
        if (atriumRawManager.getValidAtriumRaw()) {
            app.stroke(55, 103, 171);
        } else {
            app.stroke(255, 0, 0);
        }
        app.strokeWeight(2);
        app.noFill();

        for (AtriumRaw ar : atriumRawManager.getAtriumRaws()) {
            jtsRender.drawGeometry(ar.getShape());
        }
    }

    private void displaySelectedAtrium(PApplet app, JtsRender render) {
        // draw center
        app.noStroke();
        app.fill(255, 97, 136);
        app.ellipse(atrium_controllers[0].xf(), atrium_controllers[0].yf(), MallConst.ATRIUM_POS_R, MallConst.ATRIUM_POS_R);

        // draw control points
        app.fill(169, 210, 118);
        for (int i = 1; i < atrium_controllers.length; i++) {
            app.ellipse(atrium_controllers[i].xf(), atrium_controllers[i].yf(), MallConst.ATRIUM_CTRL_R, MallConst.ATRIUM_CTRL_R);
        }

        // draw shape
        app.stroke(0, 255, 0);
        app.strokeWeight(4);
        app.noFill();
        render.drawGeometry(selectedAtrium.getShape());

        app.stroke(128);
        app.strokeWeight(0.5f);
        for (int i = 1; i < atrium_controllers.length - 1; i++) {
            app.line(
                    atrium_controllers[i].xf(), atrium_controllers[i].yf(),
                    atrium_controllers[i + 1].xf(), atrium_controllers[i + 1].yf()
            );
        }
        app.line(
                atrium_controllers[1].xf(), atrium_controllers[1].yf(),
                atrium_controllers[atrium_controllers.length - 1].xf(), atrium_controllers[atrium_controllers.length - 1].yf()
        );
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

    private void displayPublicSpaceNodes(PApplet app) {
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
}
