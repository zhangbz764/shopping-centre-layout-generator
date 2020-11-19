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
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
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
    private double scale = 1;

    // split block, could be variable types
    private Split blockSplit;
    private List<WB_Polygon> shopBlock;
    private WB_Polygon publicBlock;
    private List<ZSkeleton> skeletons;
    private List<HE_Mesh> meshes;

    // select and union
    private List<HE_Face> selected;

    // statistics
    private double shopRatio; // 得铺率
    private int shopNum; // 店铺数量
    private double shopArea; // 店铺面积
    private double boundaryArea; // 当前层总面积
    private double mainTrafficLength; // 主流线总长度
    private int atriumNum = 0;
    private int evacuationStairNum = 0;

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

        this.selected = new ArrayList<>();
    }

    public void updateSplit(TrafficGraph mainGraph) {
        if (mainGraph.update) {
            setTrafficGraph(mainGraph);

            blockSplit.init(this.boundary, this.trafficGraph);
            catchSplit();
            initShop();
            getStatistics();

            this.selected = new ArrayList<>();
        }
    }

    /* ------------- generate & catch information ------------- */

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
        // convert to mesh
        this.meshes = new ArrayList<>();
        List<WB_Polygon> cellPolygons = new ArrayList<>();
        for (WB_Voronoi2D voronoi : voronois) {
            for (WB_VoronoiCell2D cell : voronoi.getCells()) {
                cellPolygons.add(cell.getPolygon());
            }
            meshes.add(new HEC_FromPolygons(cellPolygons).create());
        }
    }

    private void getStatistics() {
        this.boundaryArea = Math.abs(boundary.getSignedArea()) / scale;
        this.shopArea = 0;
        for (WB_Polygon p : shopBlock) {
            shopArea = shopArea + Math.abs(p.getSignedArea()) / scale;
        }
        this.shopRatio = shopArea / boundaryArea;
        this.shopNum = 0;
        for (HE_Mesh m : meshes) {
            shopNum = shopNum + m.getNumberOfFaces();
        }
        this.mainTrafficLength = 0;
        for (ZEdge e : trafficGraph.getTreeEdges()) {
            mainTrafficLength = mainTrafficLength + e.getLength() / scale;
        }
        for (ZEdge e : trafficGraph.getFixedEdges()) {
            mainTrafficLength = mainTrafficLength + e.getLength() / scale;
        }
    }

    /* ------------- select & update ------------- */

    public void selectShop(int pointerX, int pointerY) {
        WB_Point pointer = new WB_Point(pointerX, pointerY);
        for (int i = 0; i < shopBlock.size(); i++) {
            if (WB_GeometryOp2D.contains2D(pointer, shopBlock.get(i))) {
                for (HE_Face face : meshes.get(i).getFaces()) {
                    if (WB_GeometryOp2D.contains2D(pointer, face.getPolygon())) {
                        if (!selected.contains(face)) {
                            selected.add(face);
                        } else {
                            selected.remove(face);
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    public void updateShop() {

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
        displaySelected(render, app);
        displayShop(render, app);
    }

    public void displaySelected(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.fill(18, 109, 86);
        app.noStroke();
        for (HE_Face f : selected) {
            render.drawFace(f);
        }
        app.popStyle();
    }

    public void displayShop(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.stroke(0);
        app.strokeWeight(3);
        for (HE_Mesh mesh : meshes) {
            render.drawEdges(mesh);
        }
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
                + "\n" + "TOTAL AREA : " + String.format("%.2f", boundaryArea) + " m2"
                + "\n" + "SHOP AREA : " + String.format("%.2f", shopArea) + " m2"
                + "\n" + "SHOP NUMBER : " + shopNum
                + "\n" + "RATIO : " + String.format("%.2f", shopRatio * 100) + " %"
                + "\n" + "TRAFFIC LENGTH : " + String.format("%.2f", mainTrafficLength) + " m";
    }
}
