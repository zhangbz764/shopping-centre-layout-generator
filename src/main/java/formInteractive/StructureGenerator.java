package formInteractive;

import formInteractive.graphAdjusting.TrafficGraph;
import geometry.ZLine;
import geometry.ZPoint;
import math.ZGeoMath;
import processing.core.PApplet;
import wblut.geom.WB_GeometryOp2D;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/13
 * @time 11:46
 * @description generate structure net based on graph node
 */
@Deprecated
public class StructureGenerator {
    private TrafficGraph graph;
    private final WB_Polygon boundary;
    private final double span;
    List<WB_Polygon> shopBlocks;

    private final List<ZPoint> allSplitPoints;
    private List<ZPoint> axisPoint;
    private List<ZPoint> axisVec;
    private List<ZLine> axis;

    List<List<Integer>> concavePoints;

    /* ------------- constructor ------------- */

    public StructureGenerator(WB_Polygon boundary, double span, List<WB_Polygon> shopBlocks) {
        this.boundary = boundary;
        this.span = span;
        this.shopBlocks = shopBlocks;

        this.allSplitPoints = ZGeoMath.splitWB_PolyLineEdgeByThreshold(boundary, this.span + 1, span - 1);

        splitConcave();
        setAxis(shopBlocks);
    }

    /* ------------- set & get (public) ------------- */

    private void splitConcave() {
        this.concavePoints = new ArrayList<>();
        for (WB_Polygon block : shopBlocks) {
            concavePoints.add(ZGeoMath.getConcavePointIndices(block));
        }
    }

    /**
     * @return void
     * @description for all boundary split points,
     * if point within one of the shop blocks,
     * record point and axis vector
     */
    public void setAxis(List<WB_Polygon> shopBlocks) {
        this.axisPoint = new ArrayList<>();
        this.axisVec = new ArrayList<>();

        if (allSplitPoints.size() != 0) {
            for (ZPoint p : allSplitPoints) {
                for (WB_Polygon shopBlock : shopBlocks) {
                    if (WB_GeometryOp2D.contains2D(p.toWB_Point(), shopBlock)) {
                        axisPoint.add(p);
                        int[] indices = ZGeoMath.pointOnWhichPolyEdge(p, boundary);
                        WB_Vector edgeDir = new WB_Vector(boundary.getPoint(indices[1]).sub(boundary.getPoint(indices[0])));
                        axisVec.add(new ZPoint(edgeDir.yd(), -1 * edgeDir.xd()).unit());
                        break;
                    }
                }
            }
        }
    }

    /* ------------- draw ------------- */

    public void display(PApplet app) {
        app.pushStyle();
        app.noFill();
//        for (int i = 0; i < concavePoints.size(); i++) {
//            for (Integer index : concavePoints.get(i)) {
//                app.ellipse((float) shopBlocks.get(i).getPoint(index).xd(), (float) shopBlocks.get(i).getPoint(index).yd(), 10, 10);
//            }
//        }
        app.strokeWeight(2);
        for (int i = 0; i < axisVec.size(); i++) {
            axisVec.get(i).displayAsVector(app, axisPoint.get(i), 50);
        }
        app.popStyle();
    }
}
