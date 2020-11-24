package formInteractive.spacialElements;

import formInteractive.graphAdjusting.TrafficNode;
import geometry.ZEdge;
import geometry.ZGeoFactory;
import geometry.ZLine;
import geometry.ZPoint;
import math.ZGeoMath;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 10:00
 * @description atrium in the public space of a shopping mall
 */
public class Atrium {
    private TrafficNode center;
    private WB_Polygon polygon;
    private List<ZLine> splitLineFromAtrium;

    private Escalator escalator;

    /* ------------- constructor ------------- */

    public Atrium(TrafficNode center) {
        this.center = center;
        initAtrium(center);
    }

    /* ------------- initializer ------------- */

    /**
     * @return void
     * @description main initializer
     */
    public void initAtrium(TrafficNode center) {
        this.splitLineFromAtrium = new ArrayList<>();
        if (center.isEnd()) {

        } else {
            List<ZPoint> verticalPoints = new ArrayList<>();
            List<ZPoint> vecFromCenter = new ArrayList<>();
            for (int i = 0; i < center.geiNeighborNum(); i++) {
                ZPoint ptOnEdge = center.add(center.getVecUnitToNeighbor(i).scaleTo(center.getLinkedEdge(i).getLength() * 0.25));
                ZPoint vertical1 = ptOnEdge.add(center.getVecUnitToNeighbor(i).rotate2D(Math.PI * 0.5).scaleTo(5));
                ZPoint vertical2 = ptOnEdge.add(center.getVecUnitToNeighbor(i).rotate2D(Math.PI * -0.5).scaleTo(5));
                verticalPoints.add(vertical1);
                verticalPoints.add(vertical2);
                vecFromCenter.add(vertical1.sub(center));
                vecFromCenter.add(vertical2.sub(center));
            }
            // larger than 180
            ZPoint[] sortedVec = ZGeoMath.sortPolarAngle(center.getVecUnitToNeighbors());
            for (int i = 0; i < sortedVec.length; i++) {
                if (sortedVec[i].cross2D(sortedVec[(i + 1) % sortedVec.length]) < 0) {
                    ZPoint bisector = ZGeoMath.getAngleBisectorOrdered(sortedVec[i], sortedVec[(i + 1) % sortedVec.length]);
                    double sin = Math.abs(sortedVec[i].cross2D(bisector));
                    ZPoint point = center.add(bisector.scaleTo(5 / sin));
                    verticalPoints.add(point);
                    vecFromCenter.add(point.sub(center));
                }
            }
            // sort polar angle to order polygon vertices
            int[] order = ZGeoMath.sortPolarAngleIndices(vecFromCenter);
            WB_Point[] polyPoints = new WB_Point[verticalPoints.size() + 1];
            for (int i = 0; i < verticalPoints.size(); i++) {
                polyPoints[i] = verticalPoints.get(order[i]).toWB_Point();
            }
            polyPoints[polyPoints.length - 1] = polyPoints[0];
            this.polygon = ZGeoFactory.wbgf.createSimplePolygon(polyPoints);
            // record offset segment as block boundary
            for (int i = 1; i < polygon.getNumberOfPoints(); i++) {
                ZPoint[] polySegment = new ZPoint[]{new ZPoint(polygon.getPoint(i)),
                        new ZPoint(polygon.getPoint((i % (polygon.getNumberOfPoints() - 1)) + 1).sub(polygon.getPoint(i)))};
                for (ZEdge edge : center.getLinkedEdges()) {
                    if (!ZGeoMath.checkSegmentsIntersect(edge.toLinePD(), polySegment)) {
                        splitLineFromAtrium.add(ZGeoMath.offsetOnePolySegment(polygon, i, (i % (polygon.getNumberOfPoints() - 1)) + 1, 5));
                    }
                }
            }

        }
    }

    /* ------------- set & get (public) ------------- */

    public void setCenter(TrafficNode center) {
        this.center = center;
    }

    public void setEscalator(Escalator escalator) {
        this.escalator = escalator;
    }

    public void setPolygon(WB_Polygon polygon) {
        this.polygon = polygon;
    }

    public TrafficNode getCenter() {
        return center;
    }

    public Escalator getEscalator() {
        return escalator;
    }

    public WB_Polygon getPolygon() {
        return polygon;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render3D render, PApplet app) {
        render.drawPolygonEdges2D(polygon);
        for (ZLine line : splitLineFromAtrium) {
            line.display(app);
        }
    }
}
