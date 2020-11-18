package floors;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import geometry.ZGeoFactory;
import geometry.ZLine;
import geometry.ZPoint;
import geometry.ZSkeleton;
import math.ZGeoMath;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:49
 * @description abstract class for floors in the shopping mall
 */
public abstract class Floor {
    // input
    private int floorNum;
    private TrafficGraph trafficGraph;
    private WB_Polygon boundary;

    // split block, could be variable types
    private Split blockSplit;
    private List<WB_Polygon> shopBlock;
    private WB_Polygon publicBlock;
    private List<ZSkeleton> skeletons;

    // shop generator
    List<WB_Voronoi2D> voronois;

    // statistics
    private double storeRatio;

    /* ------------- constructor & initialize ------------- */

    public Floor(int floorNum, TrafficGraph mainGraph, WB_Polygon boundary) {
        setFloorNum(floorNum);
        setTrafficGraph(mainGraph);
        setBoundary(boundary);

        // compute split block
        this.blockSplit = new SplitBisector(this.boundary, this.trafficGraph);
        catchSplit();
    }

    public void updateSplit() {
        if (trafficGraph.update) {
            blockSplit.init(this.boundary, this.trafficGraph);
            catchSplit();
        }
    }

    public void catchSplit() {
        this.shopBlock = blockSplit.getShopBlockPolys();
        this.publicBlock = blockSplit.getPublicBlockPoly();

        // compute straight skeleton for each shop block
        this.skeletons = new ArrayList<>();
        for (WB_Polygon polygon : shopBlock) {
            ZSkeleton skeleton = new ZSkeleton(polygon);
            skeletons.add(skeleton);
        }
    }

    public void initShop() {
//        for (int i = 0; i < skeletons.size(); i++) {
//            // maybe null
//            List<ZLine> centerSegments = skeletons.get(i).getRidges();
//            centerSegments.addAll(skeletons.get(i).getExtendedRidges());
//            WB_PolyLine polyLine = ZGeoFactory.createWB_PolyLine(centerSegments);
//            if (polyLine != null) {
//                polyLineToGenerate.add(polyLine);
//
//                List<ZPoint> splitResult = ZGeoMath.splitWB_PolyLineEdgeByThreshold(polyLine, 17, 16);
//                if (splitResult.size() != 0) {
//                    splitResult.remove(splitResult.size() - 1);
//                    splitResult.remove(0);
//                }
//                splitPoints.add(splitResult);
//
//                List<WB_Point> points = new ArrayList<>();
//                for (ZPoint p : splitResult) {
//                    points.add(p.toWB_Point());
//                }
//                WB_Voronoi2D voronoi = WB_VoronoiCreator.getClippedVoronoi2D(points, shopBlock.get(i));
//                voronois.add(voronoi);
//            }
//        }
    }

    /* ------------- set & get ------------- */

    public void setFloorNum(int floorNum) {
        this.floorNum = floorNum;
    }

    public void setTrafficGraph(TrafficGraph mainGraph) {
        if (this.floorNum == 1) {
            this.trafficGraph = mainGraph;
        } else if (this.floorNum > 1) {
            this.trafficGraph = mainGraph;
            trafficGraph.clearFixed();
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


    public void setStoreRatio(double storesArea) {
        this.storeRatio = storesArea / Math.abs(boundary.getSignedArea());
    }

    public double getStoreRatio() {
        return storeRatio;
    }

    // abstract
    public abstract int getShopBlockNum();

    public abstract int getShopNum();

    /* ------------- draw ------------- */

    public void display(WB_Render3D render, PApplet app) {
        trafficGraph.display(render, app);
    }
}
