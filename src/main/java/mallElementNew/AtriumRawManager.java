package mallElementNew;

import advancedGeometry.ZBSpline;
import basicGeometry.ZPoint;
import math.ZGeoMath;
import wblut.geom.WB_GeometryOp2D;
import wblut.geom.WB_Point;
import wblut.geom.WB_PolyLine;

import java.util.ArrayList;
import java.util.List;

/**
 * the manager and creator of raw atriums
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/8/25
 * @time 16:09
 */
public class AtriumRawManager {
    private WB_PolyLine trafficLine;
    private double trafficLength;

    private List<AtriumRaw> atriumRaws;
    private List<Double> posPercentage;

    /* ------------- constructor ------------- */

    public AtriumRawManager(ZBSpline mainTrafficCurve) {
        this.trafficLine = mainTrafficCurve.getAsWB_PolyLine();
        this.trafficLength = ZGeoMath.getPolyLength(trafficLine);

        this.posPercentage = new ArrayList<>();
        this.atriumRaws = new ArrayList<>();
    }

    /* ------------- member function ------------- */

    /**
     * add an AtriumRaw to the manager, sort by position, and set anchor etc.
     *
     * @param atriumRaw new AtriumRaw
     * @return void
     */
    public void addAtriumRaw(AtriumRaw atriumRaw) {
        WB_Point center = atriumRaw.getCenter();
        WB_Point closestAnchor = WB_GeometryOp2D.getClosestPoint2D(center, trafficLine);
        double dist = ZGeoMath.distFromStart(trafficLine, closestAnchor);
        double p = dist / trafficLength;

        if (posPercentage.size() == 0) {
            posPercentage.add(p);
            atriumRaw.setPercentTraffic(p);
            ZPoint vec = new ZPoint(atriumRaw.getCenter()).sub(new ZPoint(closestAnchor));
            atriumRaw.setVecFromAnchor(vec);
            ZPoint[] ptsBesidesAnchor = ZGeoMath.pointOnEdgeByDist(new ZPoint(closestAnchor), trafficLine, 0.1);
            ZPoint anchorTangent = ptsBesidesAnchor[0].sub(ptsBesidesAnchor[1]).normalize();
            atriumRaw.setAnchorTangent(anchorTangent);
            atriumRaws.add(atriumRaw);
        } else {
            if (p >= posPercentage.get(posPercentage.size() - 1)) {
                posPercentage.add(p);
                atriumRaw.setPercentTraffic(p);
                ZPoint vec = new ZPoint(atriumRaw.getCenter()).sub(new ZPoint(closestAnchor));
                atriumRaw.setVecFromAnchor(vec);
                ZPoint[] ptsBesidesAnchor = ZGeoMath.pointOnEdgeByDist(new ZPoint(closestAnchor), trafficLine, 0.1);
                ZPoint anchorTangent = ptsBesidesAnchor[0].sub(ptsBesidesAnchor[1]).normalize();
                atriumRaw.setAnchorTangent(anchorTangent);
                atriumRaws.add(atriumRaw);
            } else {
                for (int i = 0; i < posPercentage.size(); i++) {
                    if (p < posPercentage.get(i)) {
                        posPercentage.add(i, p);
                        atriumRaw.setPercentTraffic(p);
                        ZPoint vec = new ZPoint(atriumRaw.getCenter()).sub(new ZPoint(closestAnchor));
                        atriumRaw.setVecFromAnchor(vec);
                        ZPoint[] ptsBesidesAnchor = ZGeoMath.pointOnEdgeByDist(new ZPoint(closestAnchor), trafficLine, 0.1);
                        ZPoint anchorTangent = ptsBesidesAnchor[0].sub(ptsBesidesAnchor[1]).normalize();
                        atriumRaw.setAnchorTangent(anchorTangent);
                        atriumRaws.add(i, atriumRaw);
                        break;
                    }
                }
            }
        }
    }

    /**
     * remove an AtriumRaw from the manager
     *
     * @param a AtriumRaw
     * @return void
     */
    public void removeAtriumRaw(AtriumRaw a) {
        int index = atriumRaws.indexOf(a);
        atriumRaws.remove(a);
        posPercentage.remove(index);
    }

    /**
     * update the position of AtriumRaws by a new traffic line
     *
     * @param newCenterLine new traffic center line
     * @return void
     */
    public void updateAtriumRawByTraffic(ZBSpline newCenterLine) {
        this.trafficLine = newCenterLine.getAsWB_PolyLine();
        this.trafficLength = ZGeoMath.getPolyLength(trafficLine);
        for (int i = 0; i < atriumRaws.size(); i++) {
            AtriumRaw a = atriumRaws.get(i);
            double pct = a.getPercentTraffic();

            double dist = trafficLength * pct;
            WB_Point newAnchor = ZGeoMath.getPointOnPolyEdge(trafficLine, dist);
            ZPoint[] ptsBesidesAnchor = ZGeoMath.pointOnEdgeByDist(new ZPoint(newAnchor), trafficLine, 0.1);
            ZPoint newAnchorTangent = ptsBesidesAnchor[0].sub(ptsBesidesAnchor[1]).normalize();
            double angle = a.getAnchorTangent().angleWith(newAnchorTangent);
            ZPoint newVecFromAnchor = a.getVecFromAnchor().rotate2D(Math.PI * (angle / 180));

            a.setAnchorTangent(newAnchorTangent);
            a.setVecFromAnchor(newVecFromAnchor);
            WB_Point newCenter = newAnchor.add(newVecFromAnchor.toWB_Point());
            a.moveByCenter(newCenter);
            a.rotateByAngle(Math.PI * (angle / 180));
        }
    }

    public void initMainCorridor() {

        for (int i = 0; i < atriumRaws.size(); i++) {

        }
    }

    /* ------------- setter & getter ------------- */

    public List<AtriumRaw> getAtriumRaws() {
        return atriumRaws;
    }

    /* ------------- atrium creator ------------- */

    /**
     * create a triangle atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium3(WB_Point base, double area, boolean curve) {
        double ratio = Math.sqrt((area * 2) / (2 * 7.28));
        WB_Point[] pts = new WB_Point[3];
        pts[0] = new WB_Point(base.xd(), base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() + 3.64 * ratio, base.yd() + ratio);
        pts[2] = new WB_Point(base.xd() - 3.64 * ratio, base.yd() + ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }

    /**
     * create a square atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium4(WB_Point base, double area, boolean curve) {
        double d = Math.sqrt(area) * 0.5;
        WB_Point[] pts = new WB_Point[4];
        pts[0] = new WB_Point(base.xd() - d, base.yd() - d);
        pts[1] = new WB_Point(base.xd() + d, base.yd() - d);
        pts[2] = new WB_Point(base.xd() + d, base.yd() + d);
        pts[3] = new WB_Point(base.xd() - d, base.yd() + d);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }

    /**
     * create a trapezoid atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium4_(WB_Point base, double area, boolean curve) {
        double ratio = Math.sqrt(area / 4.098);
        WB_Point[] pts = new WB_Point[4];
        pts[0] = new WB_Point(base.xd() - 1.366 * ratio, base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() + 1.366 * ratio, base.yd());
        pts[2] = new WB_Point(base.xd() + 1.366 * ratio, base.yd() + ratio);
        pts[3] = new WB_Point(base.xd() - 1.366 * ratio, base.yd() + ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }

    /**
     * create a pentagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium5(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[5];
        temp[0] = new ZPoint(base.xd(), base.yd() - 3);
        temp[1] = new ZPoint(base.xd() + 5.4, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 5.4, base.yd() + 3);
        temp[3] = new ZPoint(base.xd() - 5.4, base.yd() + 3);
        temp[4] = new ZPoint(base.xd() - 5.4, base.yd() - 1);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[5];
        pts[0] = new WB_Point(base.xd(), base.yd() - 3 * ratio);
        pts[1] = new WB_Point(base.xd() + 5.4 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 5.4 * ratio, base.yd() + 3 * ratio);
        pts[3] = new WB_Point(base.xd() - 5.4 * ratio, base.yd() + 3 * ratio);
        pts[4] = new WB_Point(base.xd() - 5.4 * ratio, base.yd() - ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }

    /**
     * create a hexagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium6(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[6];
        temp[0] = new ZPoint(base.xd(), base.yd() - 2);
        temp[1] = new ZPoint(base.xd() + 2.7, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 2.7, base.yd() + 1);
        temp[3] = new ZPoint(base.xd(), base.yd() + 2);
        temp[4] = new ZPoint(base.xd() - 2.7, base.yd() + 1);
        temp[5] = new ZPoint(base.xd() - 2.7, base.yd() - 1);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[6];
        pts[0] = new WB_Point(base.xd(), base.yd() - 2 * ratio);
        pts[1] = new WB_Point(base.xd() + 2.7 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 2.7 * ratio, base.yd() + ratio);
        pts[3] = new WB_Point(base.xd(), base.yd() + 2 * ratio);
        pts[4] = new WB_Point(base.xd() - 2.7 * ratio, base.yd() + ratio);
        pts[5] = new WB_Point(base.xd() - 2.7 * ratio, base.yd() - ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }

    /**
     * create a hexagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium6_(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[6];
        temp[0] = new ZPoint(base.xd() - 2, base.yd());
        temp[1] = new ZPoint(base.xd() - 1, base.yd() - 1.67);
        temp[2] = new ZPoint(base.xd() + 1, base.yd() - 1.67);
        temp[3] = new ZPoint(base.xd() + 2, base.yd());
        temp[4] = new ZPoint(base.xd() + 1, base.yd() + 1.67);
        temp[5] = new ZPoint(base.xd() - 1, base.yd() + 1.67);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[6];
        pts[0] = new WB_Point(base.xd() - 2 * ratio, base.yd());
        pts[1] = new WB_Point(base.xd() - ratio, base.yd() - 1.67 * ratio);
        pts[2] = new WB_Point(base.xd() + ratio, base.yd() - 1.67 * ratio);
        pts[3] = new WB_Point(base.xd() + 2 * ratio, base.yd());
        pts[4] = new WB_Point(base.xd() + ratio, base.yd() + 1.67 * ratio);
        pts[5] = new WB_Point(base.xd() - ratio, base.yd() + 1.67 * ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }

    /**
     * create a L-Shape atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium7(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[7];
        temp[0] = new ZPoint(base.xd(), base.yd() - 4);
        temp[1] = new ZPoint(base.xd() + 3.4, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 7.4, base.yd() - 1);
        temp[3] = new ZPoint(base.xd() + 7.4, base.yd() + 4);
        temp[4] = new ZPoint(base.xd(), base.yd() + 4);
        temp[5] = new ZPoint(base.xd() - 3.4, base.yd() + 2);
        temp[6] = new ZPoint(base.xd() - 3.4, base.yd() - 2);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[7];
        pts[0] = new WB_Point(base.xd(), base.yd() - 4 * ratio);
        pts[1] = new WB_Point(base.xd() + 3.4 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 7.4 * ratio, base.yd() - ratio);
        pts[3] = new WB_Point(base.xd() + 7.4 * ratio, base.yd() + 4 * ratio);
        pts[4] = new WB_Point(base.xd(), base.yd() + 4 * ratio);
        pts[5] = new WB_Point(base.xd() - 3.4 * ratio, base.yd() + 2 * ratio);
        pts[6] = new WB_Point(base.xd() - 3.4 * ratio, base.yd() - 2 * ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }

    /**
     * create a octagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public void createAtrium8(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[8];
        temp[0] = new ZPoint(base.xd() - 2.732, base.yd() - 1);
        temp[1] = new ZPoint(base.xd() - 1, base.yd() - 2);
        temp[2] = new ZPoint(base.xd() + 1, base.yd() - 2);
        temp[3] = new ZPoint(base.xd() + 2.732, base.yd() - 1);
        temp[4] = new ZPoint(base.xd() + 2.732, base.yd() + 1);
        temp[5] = new ZPoint(base.xd() + 1, base.yd() + 2);
        temp[6] = new ZPoint(base.xd() - 1, base.yd() + 2);
        temp[7] = new ZPoint(base.xd() - 2.732, base.yd() + 1);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[8];
        pts[0] = new WB_Point(base.xd() - 2.732 * ratio, base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() - ratio, base.yd() - 2 * ratio);
        pts[2] = new WB_Point(base.xd() + ratio, base.yd() - 2 * ratio);
        pts[3] = new WB_Point(base.xd() + 2.732 * ratio, base.yd() - ratio);
        pts[4] = new WB_Point(base.xd() + 2.732 * ratio, base.yd() + ratio);
        pts[5] = new WB_Point(base.xd() + ratio, base.yd() + 2 * ratio);
        pts[6] = new WB_Point(base.xd() - ratio, base.yd() + 2 * ratio);
        pts[7] = new WB_Point(base.xd() - 2.732 * ratio, base.yd() + ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        this.addAtriumRaw(a);
    }
}
