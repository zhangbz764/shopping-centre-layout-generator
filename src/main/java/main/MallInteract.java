package main;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import mallElementNew.AtriumCurve;
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
    private int status;

    private WB_Polygon site;                // 场地轮廓
    private WB_Polygon redLine;             // 场地红线
    private WB_Polygon boundary;            // 建筑轮廓
    private int boundaryBase = 0;
    private List<WB_Point> boundaryNode_interact; // 建筑轮廓控制点

    private List<WB_Point> innerNode_interact;       // 动线中部控制点
    private List<WB_Point> entryNode_interact;       // 动线端头控制点

    private List<List<WB_Point>> atriumNode_interact;    // 中庭多边形（共用）
    private List<AtriumCurve> atriumCurves;

    private List<LineString> bufferCurve_interact;   // 动线边界曲线（不同层）
    private List<WB_Point> bufferCurveControl_interact;

    private List<Polygon> cellPolys_interact;     // 商铺剖分多边形（不同层）
    private List<Polygon> cellPolys_selected;

    private boolean showAtriumControl = false;

    /* ------------- constructor ------------- */

    public MallInteract() {

    }

    /* ------------- site boundary interact ------------- */

    /**
     * initialize
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

    /**
     * switch 4 possible L-shape boundary
     *
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
     * @param x
     * @param y
     * @return void
     */
    public void dragUpdateBoundary(double x, double y) {
        for (int i = 0; i < boundaryNode_interact.size() - 1; i++) {
            WB_Point p = boundaryNode_interact.get(i);
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.BOUNDARY_INTERACT_R) {
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

    public void dragUpdateNode(double x, double y) {
        for (WB_Point p : innerNode_interact) {
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                WB_Point point = new WB_Point(x, y);
                if (WB_GeometryOp.contains2D(point, boundary) && WB_GeometryOp.getDistance2D(point, boundary) > MallConst.TRAFFIC_NODE_R) {
//                    for (AtriumCurve a : atriumCurves) {
//                        if (a.isCenter(p)) {
//                            a.changePosition(x, y);
//                        }
//                    }
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

    public void addInnerNode(double x, double y) {
        WB_Point innerNode = new WB_Point(x, y);
        if (WB_GeometryOp.contains2D(innerNode, boundary) && WB_GeometryOp.getDistance2D(innerNode, boundary) > MallConst.TRAFFIC_NODE_R) {
            this.innerNode_interact.add(innerNode);
        }
    }

    public void addEntryNode(double x, double y) {
        WB_Point fixedNode = new WB_Point(x, y);
        this.entryNode_interact.add(WB_GeometryOp2D.getClosestPoint2D(fixedNode, ZTransform.WB_PolygonToWB_PolyLine(boundary)));
    }

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


    public void addOrRemoveAtrium(double x, double y) {
        // 是否选到node
        WB_Point selected = null;
        int size = innerNode_interact.size();
        for (int i = 0; i < size; i++) {
            WB_Point p = innerNode_interact.get(i);
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                selected = p;
                break;
            }
        }
        // node上是否有中庭
        if (selected != null) {
            boolean flag = false;
            for (AtriumCurve a : atriumCurves) {
                if (a.isCenter(selected)) {
                    atriumCurves.remove(a);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                this.atriumCurves.add(new AtriumCurve(selected, 6));
            }
        }
    }

    /* ------------- atrium shape interact ------------- */

    public void enableAtriumEdit() {
        this.atriumNode_interact = new ArrayList<>();
        for (AtriumCurve a : atriumCurves) {
            WB_Point[] controls = a.getControlPoints();
            List<WB_Point> atriumControls = new ArrayList<>(Arrays.asList(controls));
            atriumNode_interact.add(atriumControls);
        }
        this.showAtriumControl = true;
    }

    public void disableAtriumEdit() {
//        this.atriumNode_interact.clear();
        this.showAtriumControl = false;
    }

    public void dragUpdateAtrium(double x, double y) {
        if (atriumNode_interact.size() > 0) {
            out:
            for (List<WB_Point> ptsList : atriumNode_interact) {
                for (WB_Point p : ptsList) {
                    if (distance(p.xd(), p.yd(), x, y) <= MallConst.CONTROL_NODE_D * 0.5) {
                        p.set(x, y);

                        break out;
                    }
                }
            }
        }
    }

    public void drawAtrium(PApplet app, WB_Render render) {
        app.pushStyle();

        // draw atrium shape
        app.stroke(55, 103, 171);
        app.strokeWeight(2);
        app.noFill();
        if (atriumCurves != null && atriumCurves.size() > 0) {
            for (AtriumCurve a : atriumCurves) {
                render.drawPolygonEdges(a.getShape());
            }
        }

        if (showAtriumControl) {
            app.noStroke();
            app.fill(169, 210, 118);
            for (List<WB_Point> ptsList : atriumNode_interact) {
                for (WB_Point p : ptsList) {
                    app.rect(
                            (float) (p.xf() - 0.5 * MallConst.CONTROL_NODE_D),
                            (float) (p.yf() - 0.5 * MallConst.CONTROL_NODE_D),
                            (float) MallConst.CONTROL_NODE_D,
                            (float) MallConst.CONTROL_NODE_D
                    );
                    // app.ellipse(p.xf(), p.yf(), (float) MallStats.TRAFFIC_NODE_R, (float) MallStats.TRAFFIC_NODE_R);
                }
            }
        }

        app.popStyle();
    }

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

    public List<WB_Point> getTrafficControls() {
        List<WB_Point> controls = new ArrayList<>();
        controls.add(entryNode_interact.get(0));
        controls.addAll(innerNode_interact);
        controls.add(entryNode_interact.get(entryNode_interact.size() - 1));
        return controls;
    }

    public List<WB_Point> getInnerNode_interact() {
        return innerNode_interact;
    }

    public List<WB_Point> getEntryNode_interact() {
        return entryNode_interact;
    }

    public List<WB_Polygon> getAtrium_interact() {
        List<WB_Polygon> atriums = new ArrayList<>();
        if (atriumNode_interact.size() > 0) {
            for (int i = 0; i < atriumNode_interact.size(); i++) {
                List<WB_Point> ptsList = atriumNode_interact.get(i);
                WB_Point[] controls = new WB_Point[ptsList.size()];
                for (int j = 0; j < ptsList.size(); j++) {
                    controls[j] = ptsList.get(j);
                }
                atriumCurves.get(i).changeShape(controls);
            }
            for (AtriumCurve a : atriumCurves) {
                atriums.add(a.getShape());
            }
        }
        return atriums;
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
        }
        app.popStyle();
    }

    public void displaySiteBoundary(PApplet app, WB_Render render) {
        // draw boundary control nodes
        app.noStroke();
        app.fill(255, 97, 136);
        for (WB_Point p : boundaryNode_interact) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.BOUNDARY_INTERACT_R, (float) MallConst.BOUNDARY_INTERACT_R);
        }

        // draw boundary and site
        app.noFill();
        app.stroke(255);
        app.strokeWeight(6);
        render.drawPolygonEdges(boundary);
        app.stroke(255, 0, 0);
        app.strokeWeight(3);
        render.drawPolygonEdges(site);
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

}
