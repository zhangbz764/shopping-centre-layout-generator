package main;

import basicGeometry.ZFactory;
import mallElementNew.AtriumCurve;
import mallElementNew.Shop;
import org.locationtech.jts.geom.*;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_GeometryOp2D;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
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
    private WB_Polygon boundary;            // 轮廓（共用）
    //    private WB_Polygon boundaryBuffer;      // 红线/上层轮廓（共用）
    private List<WB_Point> innerNode_interact;       // 内部控制点（共用）
    private List<WB_Point> entryNode_interact;       // 轮廓控制点（共用）
    private List<List<WB_Point>> atriumNode_interact;    // 中庭多边形（共用）
    private List<AtriumCurve> atriumCurves;

    private List<LineString> bufferCurve_interact;   // 动线边界曲线（不同层）
    private List<WB_Point> bufferCurveControl_interact;

    private List<Polygon> cellPolys_interact;     // 商铺剖分多边形（不同层）
    private List<Polygon> cellPolys_selected;

    private boolean showAtriumControl = false;

    /* ------------- constructor ------------- */

    public MallInteract(WB_Polygon boundary, List<WB_Point> innerNode, List<WB_Point> entryNode) {
        this.boundary = boundary;
//        this.boundaryBuffer = boundaryBuffer;
        this.innerNode_interact = innerNode;
        this.entryNode_interact = entryNode;
        this.atriumNode_interact = new ArrayList<>();
        this.atriumCurves = new ArrayList<>();
    }

    /* ------------- graph node interact ------------- */

    public void dragUpdateNode(double x, double y) {
        for (WB_Point p : innerNode_interact) {
            if (distance(p.xd(), p.yd(), x, y) <= MallConst.TRAFFIC_NODE_R) {
                WB_Point point = new WB_Point(x, y);
                if (WB_GeometryOp.contains2D(point, boundary) && WB_GeometryOp.getDistance2D(point, boundary) > MallConst.TRAFFIC_NODE_R) {
                    for (AtriumCurve a : atriumCurves) {
                        if (a.isCenter(p)) {
                            a.changePosition(x, y);
                        }
                    }
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

    public void drawBoundaryAndNode(PApplet app, WB_Render render) {
        app.pushStyle();

        // draw boundary
        app.noFill();
        app.stroke(255);
        app.strokeWeight(6);
        render.drawPolygonEdges(boundary);
//        app.stroke(180);
//        app.strokeWeight(3);
//        render.drawPolygonEdges(boundaryBuffer);

        // draw nodes
        app.noStroke();
        app.fill(255, 97, 136);
        for (WB_Point p : innerNode_interact) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.TRAFFIC_NODE_R, (float) MallConst.TRAFFIC_NODE_R);
        }
        app.fill(128);
        for (WB_Point p : entryNode_interact) {
            app.ellipse(p.xf(), p.yf(), (float) MallConst.TRAFFIC_NODE_R, (float) MallConst.TRAFFIC_NODE_R);
        }

        app.popStyle();
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

}
