package floors;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import geometry.*;
import math.ZGeoMath;
import processing.core.PApplet;
import render.JtsRender;
import wblut.geom.*;
import wblut.hemesh.HE_Face;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:49
 * @description class for floors in the shopping mall
 */
public class Floor {
    // input
    private int floorNum;
    private TrafficGraph trafficGraph;
    private WB_Polygon boundary;
    private WB_AABB boundaryAABB;
    private double span = 8;
    private double scale = 2;

    // split block, could be variable types
    private Split blockSplit;
    private List<WB_Polygon> shopBlock;
    private WB_Polygon publicBlock;
    private List<ZSkeleton> skeletons;
    private List<WB_Polygon> allInitCells;
//    private HE_Mesh[] meshes;

    // select and union
    private List<List<HE_Face>> selected;
    private List<WB_Polygon> selected2;
    private List<WB_Polygon> advicePolys;

    // statistics
    private double totalArea; // 当前层总面积
    private double shopArea; // 店铺面积
    private int shopNum; // 店铺数量
    private double shopRatio; // 得铺率
    private double maxShopArea; // 最大店铺面积
    private double minShopArea; // 最小店铺面积

    private double mainTrafficLength; // 主流线总长度
    private int atriumNum = 0; // 中庭个数
    private int evacuationStairNum = 0; // 疏散楼梯个数
    private int escalatorNum = 0; // 客用扶梯个数

    public boolean activate;

    /* ------------- constructor & initialize ------------- */

    public Floor() {

    }

    public Floor(int floorNum, TrafficGraph mainGraph, WB_Polygon boundary, double scale) {
        setScale(scale);
        setFloorNum(floorNum);
        setTrafficGraph(mainGraph);
        setBoundary(boundary);
        activate = this.floorNum == 1;

        // compute split block
        this.blockSplit = new SplitBisector(this.boundary, this.trafficGraph);
        catchSplit();
        initShop();
        // statistics
        getStatistics();
//        this.selected = new ArrayList<>();
//        for (WB_Polygon p : shopBlock) {
//            selected.add(new ArrayList<HE_Face>());
//        }
        this.selected2 = new ArrayList<>();
    }

    public void updateSplit(TrafficGraph mainGraph) {
        if (mainGraph.update) {
            setTrafficGraph(mainGraph);

            blockSplit.init(this.boundary, this.trafficGraph);
            catchSplit();
            initShop();
            getStatistics();

//            this.selected = new ArrayList<>();
//            for (WB_Polygon p : shopBlock) {
//                selected.add(new ArrayList<HE_Face>());
//            }
            this.selected2 = new ArrayList<>();
        }
    }

    /* ------------- generate & catch information ------------- */

    /**
     * @return void
     * @description get data from split polygon
     */
    private void catchSplit() {
        this.shopBlock = blockSplit.getShopBlockPolys();
        this.publicBlock = blockSplit.getPublicBlockPoly();

        // compute straight skeleton for each shop block
        this.skeletons = new ArrayList<>();
        for (WB_Polygon polygon : shopBlock) {
            ZSkeleton skeleton = new ZSkeleton(polygon);
            skeletons.add(skeleton);
        }
    }

    /**
     * @return void
     * @description generate shop from voronoi
     */
    private void initShop() {
        // shop generator
        List<WB_Voronoi2D> voronois = new ArrayList<>();
        for (int i = 0; i < skeletons.size(); i++) {
            // maybe null
            List<ZLine> centerSegments = skeletons.get(i).getRidges();
            centerSegments.addAll(skeletons.get(i).getExtendedRidges());
            WB_PolyLine polyLine = ZGeoFactory.createWB_PolyLine(centerSegments);
            if (polyLine != null) {
                List<ZPoint> splitResult = ZGeoMath.splitWB_PolyLineEdgeByStep(polyLine, 17);
                if (splitResult.size() > 1) {
                    splitResult.remove(splitResult.size() - 1);
                    splitResult.remove(0);
                } else if (splitResult.size() == 1) {
                    splitResult.remove(0);
                }
                // generate voronoi
                List<WB_Point> points = new ArrayList<>();
                for (ZPoint p : splitResult) {
                    points.add(p.toWB_Point());
                }
                WB_Voronoi2D voronoi = WB_VoronoiCreator.getClippedVoronoi2D(points, shopBlock.get(i));
                voronois.add(voronoi);
            }
        }
        this.allInitCells = new ArrayList<>();
        for (WB_Voronoi2D wb_voronoi2D : voronois) {
            for (WB_VoronoiCell2D cell : wb_voronoi2D.getCells()) {
                allInitCells.add(cell.getPolygon());
            }
        }
//        // convert to mesh
//        this.meshes = new HE_Mesh[shopBlock.size()];
//        for (int i = 0; i < voronois.size(); i++) {
//            List<WB_Polygon> cellPolygons = new ArrayList<>();
//            for (WB_VoronoiCell2D cell : voronois.get(i).getCells()) {
//                cellPolygons.add(cell.getPolygon());
//            }
//            meshes[i] = new HEC_FromPolygons(cellPolygons).create();
//        }
    }

    /**
     * @return void
     * @description calculate statistics
     */
    private void getStatistics() {
        this.totalArea = Math.abs(boundary.getSignedArea()) / (scale * scale);
        this.shopArea = 0;
        for (WB_Polygon p : shopBlock) {
            shopArea = shopArea + Math.abs(p.getSignedArea()) / (scale * scale);
        }
        this.shopRatio = shopArea / totalArea;
//        this.shopNum = 0;
//        for (HE_Mesh m : meshes) {
//            shopNum = shopNum + m.getNumberOfFaces();
//        }
        this.mainTrafficLength = 0;
        for (ZEdge e : trafficGraph.getTreeEdges()) {
            mainTrafficLength = mainTrafficLength + e.getLength() / scale;
        }
        for (ZEdge e : trafficGraph.getFixedEdges()) {
            mainTrafficLength = mainTrafficLength + e.getLength() / scale;
        }
        getShopStatistics();
    }

    /**
     * @return void
     * @description calculate statistics
     */
    public void getShopStatistics() {
        this.shopNum = allInitCells.size();
        this.advicePolys = new ArrayList<>();
        List<Double> shopAreas = new ArrayList<>();
        for (int i = 0; i < shopNum; i++) {
            double cellArea = Math.abs(allInitCells.get(i).getSignedArea()) / (scale * scale);
            if (cellArea > 3000 || cellArea < 80) {
                advicePolys.add(allInitCells.get(i));
            }
            shopAreas.add(cellArea);
        }
        this.maxShopArea = Collections.max(shopAreas);
        this.minShopArea = Collections.min(shopAreas);
    }

    /* ------------- select & update ------------- */

    /**
     * @return void
     * @description select shop to union by mouse
     */
    public void selectShop(int pointerX, int pointerY) {
        WB_Point pointer = new WB_Point(pointerX, pointerY);
        for (WB_Polygon cell : allInitCells) {
            if (WB_GeometryOp2D.contains2D(pointer, cell)) {
                if (!selected2.contains(cell)) {
                    selected2.add(cell);
                } else {
                    selected2.remove(cell);
                }
                break;
            }
        }

//        for (int i = 0; i < shopBlock.size(); i++) {
//            if (WB_GeometryOp2D.contains2D(pointer, shopBlock.get(i))) {
//                for (HE_Face face : meshes[i].getFaces()) {
//                    if (WB_GeometryOp2D.contains2D(pointer, face.getPolygon())) {
//                        if (!selected.get(i).contains(face)) {
//                            selected.get(i).add(face);
//                        } else {
//                            selected.get(i).remove(face);
//                        }
//                        break;
//                    }
//                }
//                break;
//            }
//        }
    }

    /**
     * @return void
     * @description clear select
     */
    public void clearSelect() {
        this.selected2.clear();
    }

    /**
     * @return void
     * @description union polygon, generate new mesh
     */
    public void updateShop() {
        if (selected2.size() > 1) {
            // remove faces
            allInitCells.removeAll(selected2);
            // union faces
            List<WB_Polygon> union = new ArrayList<>();
            union.add(selected2.get(0));
            for (int i = 1; i < selected2.size(); i++) {
                union = ZGeoFactory.wbgf.unionPolygons2D(selected2.get(i), union);
            }
            allInitCells.addAll(union);
        }
        clearSelect();
        getShopStatistics();
//        for (int i = 0; i < selected.size(); i++) {
//            if (selected.get(i).size() > 1) {
//                // union faces
//                List<WB_Polygon> union = new ArrayList<>();
//                union.add(selected.get(i).get(0).getPolygon());
//                for (int j = 1; j < selected.get(i).size(); j++) {
//                    union = ZGeoFactory.wbgf.unionPolygons2D(selected.get(i).get(j).getPolygon(), union);
//                }
//
//                // remove face
//                meshes[i].removeFaces(selected.get(i));
//                // create new mesh
//                List<WB_Polygon> newPolygons = meshes[i].getPolygonList();
//                newPolygons.addAll(union);
//                meshes[i] = new HEC_FromPolygons(newPolygons).create();
//            }
//            selected.get(i).clear();
//        }
    }

    /* ------------- set & get ------------- */
    public void setFloorNum(int floorNum) {
        this.floorNum = floorNum;
    }

    public void setTrafficGraph(TrafficGraph mainGraph) {
        if (this.floorNum == 1) {
            this.trafficGraph = new TrafficGraph(mainGraph.getTreeNodes(), mainGraph.getFixedNodes());
        } else if (this.floorNum > 1) {
            this.trafficGraph = new TrafficGraph(mainGraph.getTreeNodes(), new ArrayList<TrafficNode>());
        } else {
            this.trafficGraph = null;
        }
    }

    public void setBoundary(WB_Polygon boundary) {
        this.boundary = boundary;
    }

    public int getFloorNum() {
        return floorNum;
    }

    public TrafficGraph getTrafficGraph() {
        return this.trafficGraph;
    }

    public WB_Polygon getBoundary() {
        return boundary;
    }

    public void setSpan(double span) {
        this.span = span;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render3D render, JtsRender jtsRender, PApplet app) {
        displayBlock(jtsRender, app);
        displaySkeleton(app);
        displayGraph(render, app);
        displayShop(render, app);
        displaySelected(render, app);
    }

    public void displaySelected(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.fill(18, 109, 86);
        app.noStroke();
        for (WB_Polygon p : selected2) {
            render.drawPolygonEdges2D(p);
        }
//        for (List<HE_Face> eachSelect : selected) {
//            for (HE_Face f : eachSelect) {
//                render.drawFace(f);
//            }
//        }
        app.popStyle();
    }

    public void displayShop(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.stroke(0);
        app.strokeWeight(3);
        app.fill(220);
        for (WB_Polygon p : allInitCells) {
            render.drawPolygonEdges2D(p);
        }
        app.fill(255, 0, 0, 100);
        for (WB_Polygon p : advicePolys) {
            render.drawPolygonEdges2D(p);
        }
//        for (HE_Mesh mesh : meshes) {
//            render.drawEdges(mesh);
//        }
        app.popStyle();
    }

    public void displayGraph(WB_Render3D render, PApplet app) {
        trafficGraph.display(render, app);
    }

    public void displayBlock(JtsRender jtsRender, PApplet app) {
        blockSplit.display(jtsRender, app);
    }

    public void displaySkeleton(PApplet app) {
        for (ZSkeleton skeleton : skeletons) {
            skeleton.display(app);
        }
    }

    public String getTextInfo() {
        return floorNum + " F"
                + "\n"
                + "\n" + "TOTAL AREA : " + String.format("%.2f", totalArea) + " m2"
                + "\n" + "SHOP AREA : " + String.format("%.2f", shopArea) + " m2"
                + "\n" + "SHOP NUMBER : " + shopNum
                + "\n" + "RATIO : " + String.format("%.2f", shopRatio * 100) + " %"
                + "\n" + "MAX SHOP AREA : " + String.format("%.2f", maxShopArea) + " m2"
                + "\n" + "MIN SHOP AREA : " + String.format("%.2f", minShopArea) + " m2";
    }

    public String getTextInfo2() {
        return "\n"
                + "\n" + "TRAFFIC LENGTH : " + String.format("%.2f", mainTrafficLength) + " m"
                + "\n" + "ATRIUM NUMBER : " + atriumNum
                + "\n" + "ESCALATOR NUMBER : " + escalatorNum
                + "\n" + "EVACUATION NUMBER : " + evacuationStairNum;
    }
}
