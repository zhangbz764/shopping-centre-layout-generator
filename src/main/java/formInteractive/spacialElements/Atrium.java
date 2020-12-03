package formInteractive.spacialElements;

import formInteractive.graphAdjusting.TrafficNode;
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

// TODO: 2020/12/3 整理代码结构 
public class Atrium {
    // center node
    private TrafficNode center;

    // boundary points by computing
    private List<ZPoint> boundaryPoints;
    private List<ZPoint> vecFromCenter;

    // shape polygon
    private WB_Polygon polygon;

    // offset line
    private List<ZLine> offsetSegmentsFromAtrium;
    private List<ZPoint> jointsFromAtrium;
    private List<ControlPoint> controlPoints;
    private int activeIndex = 0;

    private Escalator escalator;

    /* ------------- constructor ------------- */

    public Atrium(TrafficNode center) {
        this.center = center;
        initAtrium();
    }

    /* ------------- initializer ------------- */

    /**
     * @return void
     * @description main initializer
     */
    public void initAtrium() {
        initControlPoints(center);
        extractBoundaryPoints();
        createPolygon(vecFromCenter, boundaryPoints);
        findOffsetAndJoints(polygon, center.getLinkedEdges());
    }

    /* ------------- compute function ------------- */

    /**
     * @return void
     * @description find control point for adjustment
     */
    private void initControlPoints(TrafficNode center) {
        this.controlPoints = new ArrayList<>();
        if (center.isEnd()) {
            // end of a graph
            ControlPoint pt1 = new ControlPoint(center, center.getVecUnitToNeighbor(0), center.getLinkedEdge(0).getLength() * 0.25);
            ControlPoint pt2 = new ControlPoint(center, center.getVecUnitToNeighbor(0).scaleTo(-1), 0);
            controlPoints.add(pt1);
            controlPoints.add(pt2);
        } else {
            // on edge
            for (int i = 0; i < center.geiNeighborNum(); i++) {
                ControlPoint pt = new ControlPoint(center, center.getVecUnitToNeighbor(i), center.getLinkedEdge(i).getLength() * 0.25);
                controlPoints.add(pt);
            }
            // at convex point
            ZPoint[] sortedVec = ZGeoMath.sortPolarAngle(center.getVecUnitToNeighbors());
            for (int i = 0; i < sortedVec.length; i++) {
                if (sortedVec[i].cross2D(sortedVec[(i + 1) % sortedVec.length]) < 0) {
                    ZPoint bisector = ZGeoMath.getAngleBisectorOrdered(sortedVec[i], sortedVec[(i + 1) % sortedVec.length]);
                    ControlPoint pt = new ControlPoint(center, bisector, sortedVec[i]);
                    controlPoints.add(pt);
                }
            }
        }
    }

    /**
     * @return void
     * @description extract boundary points from control points
     */
    private void extractBoundaryPoints() {
        // extract boundary points from control points
        this.boundaryPoints = new ArrayList<>();
        this.vecFromCenter = new ArrayList<>();
        for (ControlPoint cp : controlPoints) {
            for (ZPoint p : cp.polyPoints) {
                boundaryPoints.add(p);
                vecFromCenter.add(p.sub(center));
            }
        }
    }

    /**
     * @return void
     * @description sort vectors and create shape polygon
     */
    private void createPolygon(List<ZPoint> vecFromCenter, List<ZPoint> boundaryPoints) {
        // sort polar angle to order polygon vertices
        int[] order = ZGeoMath.sortPolarAngleIndices(vecFromCenter);
        WB_Point[] polyPoints = new WB_Point[boundaryPoints.size() + 1];
        for (int i = 0; i < boundaryPoints.size(); i++) {
            polyPoints[i] = boundaryPoints.get(order[i]).toWB_Point();
        }
        polyPoints[polyPoints.length - 1] = polyPoints[0];
        this.polygon = ZGeoFactory.wbgf.createSimplePolygon(polyPoints);
    }

    /**
     * @return java.util.List<geometry.ZLine>
     * @description record offset segment as block boundary
     */
    private void findOffsetAndJoints(WB_Polygon polygon, List<? extends ZLine> testLines) {
        this.offsetSegmentsFromAtrium = new ArrayList<>();
        this.jointsFromAtrium = new ArrayList<>();
        for (int i = 0; i < polygon.getNumberSegments(); i++) {
            boolean intersect = false;
            for (ZLine testLine : testLines) {
                if (ZGeoMath.checkWB_SegmentIntersect(testLine.toWB_Segment(), polygon.getSegment(i))) {
                    intersect = true;
                    break;
                }
            }
            if (!intersect) {
                offsetSegmentsFromAtrium.add(ZGeoMath.offsetWB_PolygonSegment(polygon, i, 5));
            } else {
                ZLine intersectOffset = ZGeoMath.offsetWB_PolygonSegment(polygon, i, 5);
                jointsFromAtrium.add(intersectOffset.getPt0());
                jointsFromAtrium.add(intersectOffset.getPt1());
            }
        }
    }

    /* ------------- set & get (public) ------------- */

    public void switchActiveControl() {
        activeIndex = (activeIndex + 1) % controlPoints.size();
    }

    public void updateLength(double delta) {
        controlPoints.get(activeIndex).updateDistOnEdge(delta);
        extractBoundaryPoints();
        createPolygon(vecFromCenter, boundaryPoints);
        findOffsetAndJoints(polygon, center.getLinkedEdges());

        System.out.println(controlPoints.get(activeIndex).distOnEdge);
    }

    public void updateWidth(double delta) {
        controlPoints.get(activeIndex).updateDistFromEdge(delta);
        extractBoundaryPoints();
        createPolygon(vecFromCenter, boundaryPoints);
        findOffsetAndJoints(polygon, center.getLinkedEdges());

        System.out.println(controlPoints.get(activeIndex).distFromEdge);
    }

    public void setCenter(TrafficNode center) {
        this.center = center;
    }

    public void setEscalator(Escalator escalator) {
        this.escalator = escalator;
    }

    public TrafficNode getCenter() {
        return center;
    }

    public WB_Polygon getPolygon() {
        return polygon;
    }

    public List<ZLine> getOffsetSegmentsFromAtrium() {
        return offsetSegmentsFromAtrium;
    }

    public List<ZPoint> getJointsFromAtrium() {
        return jointsFromAtrium;
    }

    public Escalator getEscalator() {
        return escalator;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render3D render, PApplet app) {
        app.strokeWeight(1);
        render.drawPolygonEdges2D(polygon);
    }

    public void displayActiveControl(PApplet app) {
        app.strokeWeight(3);
        controlPoints.get(activeIndex).display(app);
    }

    /* ------------- inner class ------------- */

    private static class ControlPoint {
        private int flag;
        private ZPoint center;
        private ZPoint moveDir;
        private ZPoint oneEdgeVec = null;
        private double distOnEdge;
        private double distFromEdge = 5;
        private ZPoint point;
        private List<ZPoint> polyPoints;

        // on edge
        private ControlPoint(ZPoint center, ZPoint vec, double distOnEdge) {
            this.flag = 0;
            this.center = center;
            this.moveDir = vec;
            this.distOnEdge = distOnEdge;
            System.out.println(distOnEdge);

            this.point = this.center.add(moveDir.scaleTo(this.distOnEdge));
            this.polyPoints = new ArrayList<>();
            polyPoints.add(point.add(moveDir.rotate2D(Math.PI * 0.5).scaleTo(distFromEdge)));
            polyPoints.add(point.add(moveDir.rotate2D(Math.PI * -0.5).scaleTo(distFromEdge)));
        }

        // at convex point
        private ControlPoint(ZPoint center, ZPoint vec, ZPoint oneEdgeVec) {
            this.flag = 1;
            this.center = center;
            this.moveDir = vec;
            this.oneEdgeVec = oneEdgeVec;

            double sin = Math.abs(this.oneEdgeVec.cross2D(moveDir));
            this.point = this.center.add(moveDir.scaleTo(this.distFromEdge / sin));
            this.polyPoints = new ArrayList<>();
            polyPoints.add(this.point);
        }

        private void updateDistOnEdge(double delta) {
            distOnEdge += delta;
            if (flag == 0) {
                this.point = this.center.add(moveDir.scaleTo(this.distOnEdge));
                this.polyPoints = new ArrayList<>();
                polyPoints.add(point.add(moveDir.rotate2D(Math.PI * 0.5).scaleTo(distFromEdge)));
                polyPoints.add(point.add(moveDir.rotate2D(Math.PI * -0.5).scaleTo(distFromEdge)));
            } else {
                double sin = Math.abs(this.oneEdgeVec.cross2D(moveDir));
                this.point = this.center.add(moveDir.scaleTo(this.distFromEdge / sin));
                this.polyPoints = new ArrayList<>();
                polyPoints.add(this.point);
            }
        }

        private void updateDistFromEdge(double delta) {
            distFromEdge += delta;
            if (flag == 0) {
                this.point = center.add(moveDir.scaleTo(this.distOnEdge));
                this.polyPoints = new ArrayList<>();
                polyPoints.add(point.add(moveDir.rotate2D(Math.PI * 0.5).scaleTo(distFromEdge)));
                polyPoints.add(point.add(moveDir.rotate2D(Math.PI * -0.5).scaleTo(distFromEdge)));
            } else {
                double sin = Math.abs(this.oneEdgeVec.cross2D(moveDir));
                this.point = center.add(moveDir.scaleTo(this.distFromEdge / sin));
                this.polyPoints = new ArrayList<>();
                polyPoints.add(this.point);
            }
        }

        private ZPoint getPoint() {
            return point;
        }

        private void display(PApplet app) {
            if (polyPoints.size() == 2) {
                app.line((float) polyPoints.get(0).x(), (float) polyPoints.get(0).y(), (float) polyPoints.get(1).x(), (float) polyPoints.get(1).y());
            }
            point.displayAsPoint(app, 5);
        }
    }
}
