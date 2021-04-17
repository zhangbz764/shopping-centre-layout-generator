package floors;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import geometry.*;
import main.MallConstant;
import math.ZGeoMath;
import math.ZMath;
import processing.core.PApplet;
import processing.core.PConstants;
import render.JtsRender;
import subdivision.ZSD_SkeVorStrip;
import wblut.geom.*;
import wblut.processing.WB_Render;

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
    private double scale = MallConstant.SCALE; // 缩放比例

    // split block, could be variable types
    private Split blockSplit;
    private List<WB_Polygon> shopBlock; // 初始店铺分区
    private WB_Polygon publicBlock; // 公共分区
    private List<ZSkeleton> skeletons; // 分区直骨架

    private List<ZSD_SkeVorStrip> skeVorStrips;

    private List<WB_Polygon> allCells; // 初步剖分结果

    // select and union
    private List<WB_Polygon> selected; // 手动选择的店铺
    private List<WB_Polygon> invalid; // 不合法店铺
    private List<WB_Polygon> anchor; // 主力店铺
    private List<WB_Polygon> subAnchor; // 次主力店铺
    private List<WB_Polygon> ordinaryShop; // 普通商铺

    // statistics
    private double totalArea; // 当前层总面积
    private double shopArea; // 店铺面积
    private int shopNum; // 店铺数量
    private double shopRatio; // 得铺率

    private double maxArea;
    private double minArea;
    private double invalidArea = 0; // 不合法店铺面积
    private double ordinaryArea = 0; // 普通商铺面积
    private double subAnchorArea = 0; // 次主力店铺面积
    private double anchorArea = 0; // 主力店铺面积

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

    /* ------------- member function ------------- */

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
        this.skeVorStrips = new ArrayList<>();
        for (WB_Polygon polygon : shopBlock) {
            ZSD_SkeVorStrip divTool = new ZSD_SkeVorStrip(polygon);
            divTool.setSpan(16);
            divTool.performDivide();
            skeVorStrips.add(divTool);
        }
        this.allCells = new ArrayList<>();
        for (ZSD_SkeVorStrip svs : skeVorStrips) {
            allCells.addAll(svs.getAllSubPolygons());
        }
        // shop generator
//        List<WB_Voronoi2D> voronois = new ArrayList<>();
//        for (int i = 0; i < skeletons.size(); i++) {
//            // maybe null
//            List<ZLine> centerSegments = skeletons.get(i).getRidges();
//            centerSegments.addAll(skeletons.get(i).getExtendedRidges());
//            WB_PolyLine polyLine = ZFactory.createWB_PolyLine(centerSegments);
//            if (polyLine != null) {
//                List<ZPoint> splitResult = ZGeoMath.splitPolyLineByStep(polyLine, 17);
//                if (splitResult.size() > 1) {
//                    splitResult.remove(splitResult.size() - 1);
//                    splitResult.remove(0);
//                } else if (splitResult.size() == 1) {
//                    splitResult.remove(0);
//                }
//                // generate voronoi
//                List<WB_Point> points = new ArrayList<>();
//                for (ZPoint p : splitResult) {
//                    points.add(p.toWB_Point());
//                }
//                WB_Voronoi2D voronoi = WB_VoronoiCreator.getClippedVoronoi2D(points, shopBlock.get(i));
//                voronois.add(voronoi);
//            }
//        }
//        this.allCells = new ArrayList<>();
//        for (WB_Voronoi2D wb_voronoi2D : voronois) {
//            for (WB_VoronoiCell2D cell : wb_voronoi2D.getCells()) {
//                allCells.add(cell.getPolygon());
//            }
//        }
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
                union = ZFactory.wbgf.unionPolygons2D(selected.get(i), union);
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
        this.invalid = new ArrayList<>();
        this.ordinaryShop = new ArrayList<>();
        this.anchor = new ArrayList<>();
        this.subAnchor = new ArrayList<>();

        invalidArea = 0; // 不合法店铺面积
        ordinaryArea = 0; // 普通商铺面积
        subAnchorArea = 0; // 次主力店铺面积
        anchorArea = 0; // 主力店铺面积

        List<Double> allArea = new ArrayList<>();
        for (int i = 0; i < shopNum; i++) {
            double cellArea = Math.abs(allCells.get(i).getSignedArea()) / (scale * scale);
            if (cellArea < 80) {
                invalid.add(allCells.get(i));
                this.invalidArea += cellArea;
            } else {
                if (cellArea >= 80 && cellArea <= 300) {
                    ordinaryShop.add(allCells.get(i)); // 普通商铺
                    this.ordinaryArea += cellArea;
                } else if (cellArea > 300 && cellArea <= 2000) {
                    subAnchor.add(allCells.get(i)); // 次主力店
                    this.subAnchorArea += cellArea;
                } else {
                    anchor.add(allCells.get(i)); // 主力店
                    this.anchorArea += cellArea;
                }
            }
            allArea.add(cellArea);
        }
        this.minArea = Collections.min(allArea);
        this.maxArea = Collections.max(allArea);
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render, JtsRender jtsRender, PApplet app) {
        displayBlock(jtsRender, app);
        displaySkeleton(app);
//        app.pushStyle();
//        for (ZSD_SkeVorStrip svs : skeVorStrips) {
//            svs.display(app, render);
//        }
//        app.popStyle();
        displayGraph(render, app);
        displayShop(render, app);
        displaySelected(render, app);
    }

    public void displaySelected(WB_Render render, PApplet app) {
        app.pushStyle();
        app.noFill();
        app.stroke(0, 255, 0);
        app.strokeWeight(3);
        for (WB_Polygon p : selected) {
            render.drawPolygonEdges2D(p);
        }
        app.popStyle();
    }

    public void displayShop(WB_Render render, PApplet app) {
        app.pushStyle();
        app.stroke(0);
        app.strokeWeight(3);
        app.fill(220);
        for (WB_Polygon p : ordinaryShop) {
            render.drawPolygonEdges2D(p);
        }
        app.fill(170, 214, 223);
        for (WB_Polygon p : subAnchor) {
            render.drawPolygonEdges2D(p);
        }
        app.fill(195, 137, 136);
        for (WB_Polygon p : anchor) {
            render.drawPolygonEdges2D(p);
        }
        app.fill(0, 100);
        for (WB_Polygon p : invalid) {
            render.drawPolygonEdges2D(p);
        }
        app.popStyle();
    }

    public void displayGraph(WB_Render render, PApplet app) {
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
     * description
     *
     * @param app
     * @param x
     * @param y
     * @return void
     */
    public void displayStats(PApplet app, float x, float y) {
        app.pushStyle();
        app.pushMatrix();
        app.translate(x, y);
        app.noStroke();

        app.fill(220);
        app.rect(0, 0, (float) ZMath.mapToRegion(ordinaryArea, 0, shopArea, 0, 100), 15);
        app.arc(200, 30, 100, 100,
                0, (float) ((ordinaryArea / shopArea) * Math.PI * 2),
                PConstants.PIE
        );

        app.fill(170, 214, 223);
        app.rect(0, 20, (float) ZMath.mapToRegion(subAnchorArea, 0, shopArea, 0, 100), 15);
        app.arc(200, 30, 100, 100,
                (float) ((ordinaryArea / shopArea) * Math.PI * 2), (float) (((ordinaryArea + subAnchorArea) / shopArea) * Math.PI * 2),
                PConstants.PIE
        );

        app.fill(195, 137, 136);
        app.rect(0, 40, (float) ZMath.mapToRegion(anchorArea, 0, shopArea, 0, 100), 15);
        app.arc(200, 30, 100, 100,
                (float) (((ordinaryArea + subAnchorArea) / shopArea) * Math.PI * 2), (float) (((ordinaryArea + subAnchorArea + anchorArea) / shopArea) * Math.PI * 2),
                PConstants.PIE
        );

        app.fill(0, 100);
        app.rect(0, 60, (float) ZMath.mapToRegion(invalidArea, 0, shopArea, 0, 100), 15);
        app.arc(200, 30, 100, 100,
                (float) (((ordinaryArea + subAnchorArea + anchorArea) / shopArea) * Math.PI * 2), (float) (((ordinaryArea + subAnchorArea + anchorArea + invalidArea) / shopArea) * Math.PI * 2),
                PConstants.PIE
        );

        app.popMatrix();
        app.popStyle();
    }

    /**
     * convert statistics to String
     *
     * @param
     * @return java.lang.String
     */
    public String getTextInfo() {
        return "本层建筑面积 : " + String.format("%.2f", totalArea) + " ㎡"
                + "\n" + "可租赁面积 : " + String.format("%.2f", shopArea) + " ㎡"
                + "\n" + "商铺总数量 : " + shopNum
                + "\n" + "得铺率 : " + String.format("%.2f", shopRatio * 100) + " %"
                + "\n" + "最小区域面积 : " + String.format("%.2f", minArea) + " ㎡"
                + "\n" + "最大区域面积 : " + String.format("%.2f", maxArea) + " ㎡"
                + "\n" + "动线长度 : " + String.format("%.2f", mainTrafficLength) + " m";
    }

    /**
     * convert statistics to String
     *
     * @param
     * @return java.lang.String
     */
    public String getTextInfo2() {
        String info = "普通商铺"
                + "\n" + "次主力店"
                + "\n" + "主力店";
        if (invalidArea != 0) {
            info += "\n" + "不合法区域";
        }
        return info;
    }
}
