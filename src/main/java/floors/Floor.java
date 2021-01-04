package floors;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import geometry.*;
import math.ZGeoMath;
import processing.core.PApplet;
import render.JtsRender;
import wblut.geom.*;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * class for floors in the shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:49
 */
public class Floor {
    // input
    private int floorNum; // 层数
    private TrafficGraph trafficGraph; // 动线
    private WB_Polygon boundary; // 外轮廓
    private double span = 8; // 店铺跨度
    private double scale = 2; // 缩放比例

    // split block, could be variable types
    private Split blockSplit;
    private List<WB_Polygon> shopBlock; // 初始店铺分区
    private WB_Polygon publicBlock; // 公共分区
    private List<ZSkeleton> skeletons; // 分区直骨架
    private List<WB_Polygon> allCells; // 初步剖分结果

    // select and union
    private List<WB_Polygon> selected; // 手动选择的店铺
    private List<WB_Polygon> advicePolys; // 不合法店铺

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

    public boolean activate; // 激活编辑开关

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

    /**
     * update shop split
     *
     * @param mainGraph the main adjustable TrafficGraph
     * @return void
     */
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

    /**
     * get data from split polygon
     *
     * @param
     * @return void
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
     * generate shop from voronoi
     *
     * @param
     * @return void
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
        this.allCells = new ArrayList<>();
        for (WB_Voronoi2D wb_voronoi2D : voronois) {
            for (WB_VoronoiCell2D cell : wb_voronoi2D.getCells()) {
                allCells.add(cell.getPolygon());
            }
        }
    }

    /* ------------- select & update ------------- */

    /**
     * select shop to union by mouse
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public void selectShop(int pointerX, int pointerY) {
        WB_Point pointer = new WB_Point(pointerX, pointerY);
        for (WB_Polygon cell : allCells) {
            if (WB_GeometryOp2D.contains2D(pointer, cell)) {
                if (!selected.contains(cell)) {
                    selected.add(cell);
                } else {
                    selected.remove(cell);
                }
                break;
            }
        }
    }

    /**
     * clear select
     *
     * @param
     * @return void
     */
    public void clearSelect() {
        this.selected.clear();
    }

    /**
     * union selected polygon
     *
     * @param
     * @return void
     */
    public void updateShop() {
        if (selected.size() > 1) {
            // remove faces
            allCells.removeAll(selected);
            // union faces
            List<WB_Polygon> union = new ArrayList<>();
            union.add(selected.get(0));
            for (int i = 1; i < selected.size(); i++) {
                union = ZGeoFactory.wbgf.unionPolygons2D(selected.get(i), union);
            }
            allCells.addAll(union);
        }
        clearSelect();
        getShopStatistics();
    }

    /* ------------- setter & getter ------------- */

    public void setFloorNum(int floorNum) {
        this.floorNum = floorNum;
    }

    public void setTrafficGraph(TrafficGraph graph) {
        this.trafficGraph = graph;
    }

    public void setBoundary(WB_Polygon boundary) {
        this.boundary = boundary;
    }

    public void setSpan(double span) {
        this.span = span;
    }

    public void setScale(double scale) {
        this.scale = scale;
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

    public List<WB_Polygon> getAllCells() {
        return allCells;
    }

    /**
     * calculate all statistics
     *
     * @param
     * @return void
     */
    private void getStatistics() {
        this.totalArea = Math.abs(boundary.getSignedArea()) / (scale * scale);
        this.shopArea = 0;
        for (WB_Polygon p : shopBlock) {
            shopArea = shopArea + Math.abs(p.getSignedArea()) / (scale * scale);
        }
        this.shopRatio = shopArea / totalArea;

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
     * calculate statistics of shop
     *
     * @param
     * @return void
     */
    public void getShopStatistics() {
        this.shopNum = allCells.size();
        this.advicePolys = new ArrayList<>();
        List<Double> shopAreas = new ArrayList<>();
        for (int i = 0; i < shopNum; i++) {
            double cellArea = Math.abs(allCells.get(i).getSignedArea()) / (scale * scale);
            if (cellArea > 3000 || cellArea < 80) {
                advicePolys.add(allCells.get(i));
            }
            shopAreas.add(cellArea);
        }
        this.maxShopArea = Collections.max(shopAreas);
        this.minShopArea = Collections.min(shopAreas);
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
        for (WB_Polygon p : selected) {
            render.drawPolygonEdges2D(p);
        }
        app.popStyle();
    }

    public void displayShop(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.stroke(0);
        app.strokeWeight(3);
        app.fill(220);
        for (WB_Polygon p : allCells) {
            render.drawPolygonEdges2D(p);
        }
        app.fill(255, 0, 0, 100);
        for (WB_Polygon p : advicePolys) {
            render.drawPolygonEdges2D(p);
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
        app.pushStyle();
        for (ZSkeleton skeleton : skeletons) {
//            skeleton.display(app);
            skeleton.displayTopEdges(app);
            skeleton.displayExtendedRidges(app);
        }
        app.popStyle();
    }

    /**
     * convert statistics to String
     *
     * @param
     * @return java.lang.String
     */
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

    /**
     * convert statistics to String
     *
     * @param
     * @return java.lang.String
     */
    public String getTextInfo2() {
        return "\n"
                + "\n" + "TRAFFIC LENGTH : " + String.format("%.2f", mainTrafficLength) + " m"
                + "\n" + "ATRIUM NUMBER : " + atriumNum
                + "\n" + "ESCALATOR NUMBER : " + escalatorNum
                + "\n" + "EVACUATION NUMBER : " + evacuationStairNum;
    }
}
