package mallElementNew;

import advancedGeometry.ZBSpline;
import advancedGeometry.ZCatmullRom;
import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import math.ZGeoMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Transform2D;
import wblut.geom.WB_Vector;
import wblut.nurbs.WB_BSpline;

import java.util.Arrays;

/**
 * raw atrium shape in the shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/7/1
 * @time 23:22
 */
public class AtriumRaw {
    private final int shapePtsNum;      // number of shape control points
    private boolean curve;              // if the raw atrium is curve
    private WB_Point center;            // center (anchor) of raw atrium

    // distance percentage on main traffic
    private double percentTraffic = 0;
    // vector from anchor on main traffic to center
    private ZPoint vecFromAnchor = new ZPoint(0, 0);
    // tangent vector at anchor
    private ZPoint anchorTangent = new ZPoint(0, 1);

    private WB_Point[] shapePoints;     // shape control points
    private WB_Vector[] shapeVectors;   // vectors from center to shape control points
    private Polygon shape;              // shape
    private double area;                // area of the shape
    private String atriumType;          // type of raw atrium

    private WB_Point[] originalShapePoints; // shape control points of last position

    /* ------------- constructor ------------- */

    public AtriumRaw(WB_Point _center, WB_Point[] _shapePoints, boolean _ifCurve) {
        this.center = _center;
        this.shapePoints = _shapePoints;
        this.shapePtsNum = shapePoints.length;
        this.curve = _ifCurve;

        updateOriginalShapePoints();
        updateVectors();
        updateShape();
        updateArea();
    }

    /* ------------- public member function ------------- */

    /**
     * reverse the curve and polygon (area remains)
     */
    public void reverseCurve() {
        if (curve) {
            WB_Polygon polyTemp = new WB_Polygon(shapePoints);
            double ratio = Math.sqrt(this.area / Math.abs(polyTemp.getSignedArea()));
            for (int i = 0; i < shapePtsNum; i++) {
                shapeVectors[i].scaleSelf(ratio);
                shapePoints[i].set(center.add(shapeVectors[i]));
            }
            curve = false;
            updateOriginalShapePoints();
            updateShape();
            updateArea();
        } else {
            WB_Polygon curveTemp = new ZBSpline(shapePoints, 3, 25, ZBSpline.CLOSE).getAsWB_Polygon();
//            WB_Polygon curveTemp = new ZCatmullRom(shapePoints, 10, true).getAsWB_Polygon();
            double ratio = Math.sqrt(this.area / Math.abs(curveTemp.getSignedArea()));
            for (int i = 0; i < shapePtsNum; i++) {
                shapeVectors[i].scaleSelf(ratio);
                shapePoints[i].set(center.add(shapeVectors[i]));
            }
            curve = true;
            updateOriginalShapePoints();
            updateShape();
            updateArea();
        }
    }

    /**
     * move shape using given center and vectors
     *
     * @param newCenter new center
     */
    public void moveByCenter(WB_Point newCenter) {
        this.center.set(newCenter);
        for (int i = 0; i < shapePtsNum; i++) {
            shapePoints[i].set(center.add(shapeVectors[i]));
        }
        updateOriginalShapePoints();
        updateShape();
    }

    public void rotateByAngle(double angle){
        WB_Transform2D transform2D = new WB_Transform2D();
        WB_Point[] ori = this.getOriginalShapePoints();
        WB_Point[] shapePoints = this.getShapePoints();
        WB_Point center = this.getCenter();
        transform2D.addRotateAboutPoint(angle, center);
        for (int i = 0; i < shapePoints.length; i++) {
            shapePoints[i].set(transform2D.applyAsPoint2D(ori[i]));
        }
        this.updateVectors();
        this.updateShape();
    }

    /**
     * update shape by a changed shape point while area remains
     *
     * @param newControl new location of the changed shape point
     * @param id         index of the changed shape point
     */
    public void updateShapeByArea(WB_Point newControl, int id) {
        this.shapePoints[id].set(newControl);
        double ratio = 1;
        if (curve) {
            WB_Polygon curveTemp = new ZBSpline(shapePoints, 3, 25, ZBSpline.CLOSE).getAsWB_Polygon();
//            WB_Polygon curveTemp = new ZCatmullRom(shapePoints, 10, true).getAsWB_Polygon();
            ratio = Math.sqrt(this.area / Math.abs(curveTemp.getSignedArea()));
        } else {
            WB_Polygon polyTemp = new WB_Polygon(shapePoints);
            ratio = Math.sqrt(this.area / Math.abs(polyTemp.getSignedArea()));
        }
        for (WB_Point shapePoint : shapePoints) {
            WB_Vector v = new WB_Vector(shapePoint.sub(shapePoints[id]));
            v.scaleSelf(ratio);
            shapePoint.set(shapePoints[id].add(v));
        }

        this.center = ZGeoMath.centerFromPoints(shapePoints);
        updateOriginalShapePoints();
        updateVectors();
        updateShape();
        updateArea();
    }

    /**
     * scale atrium shape by given area
     *
     * @param area new area
     * @return void
     */
    public void scaleShapeByArea(double area) {
        double ratio = Math.sqrt(area / this.area);
        for (int i = 0; i < shapeVectors.length; i++) {
            shapeVectors[i].scaleSelf(ratio);
            shapePoints[i].set(center.add(shapeVectors[i]));
        }
        updateOriginalShapePoints();
        updateShape();
        updateArea();
    }

    /* ------------- private member function ------------- */

    /**
     * update original shape points (for rotating)
     */
    public void updateOriginalShapePoints() {
        this.originalShapePoints = new WB_Point[shapePtsNum];
        for (int i = 0; i < shapePtsNum; i++) {
            originalShapePoints[i] = new WB_Point(shapePoints[i].xd(), shapePoints[i].yd());
        }
    }

    /**
     * update shape vectors
     */
    public void updateVectors() {
        this.shapeVectors = new WB_Vector[shapePtsNum];
        for (int i = 0; i < shapePtsNum; i++) {
            shapeVectors[i] = new WB_Vector(shapePoints[i].sub(center));
        }
    }

    /**
     * update shape polygon
     */
    public void updateShape() {
        if (curve) {
            ZBSpline spline = new ZBSpline(shapePoints, 3, 25, ZBSpline.CLOSE);
            this.shape = spline.getAsPolygon();
//            ZCatmullRom catmullRom = new ZCatmullRom(shapePoints, 10, true);
//            this.shape = catmullRom.getAsPolygon();
        } else {
            Coordinate[] constructor = new Coordinate[shapePtsNum + 1];
            for (int i = 0; i < shapePoints.length; i++) {
                constructor[i] = new Coordinate(shapePoints[i].xd(), shapePoints[i].yd(), shapePoints[i].zd());
            }
            constructor[constructor.length - 1] = constructor[0];
            this.shape = ZFactory.jtsgf.createPolygon(constructor);
        }
    }

    /**
     * update area and type
     */
    public void updateArea() {
        this.area = shape.getArea();
        if (area >= 450 && area <= 600) {
            atriumType = "main";
        } else if (area >= 280 && area <= 400) {
            atriumType = "sub";
        } else {
            atriumType = "other";
        }
    }

    /* ------------- setter & getter ------------- */

    public int getShapePtsNum() {
        return shapePtsNum;
    }

    public WB_Point getCenter() {
        return center;
    }

    public WB_Point[] getOriginalShapePoints() {
        return originalShapePoints;
    }

    public WB_Point[] getShapePoints() {
        return shapePoints;
    }

    public Polygon getShape() {
        return shape;
    }

    public double getArea() {
        return area;
    }

    public void setPercentTraffic(double percentTraffic) {
        this.percentTraffic = percentTraffic;
    }

    public double getPercentTraffic() {
        return percentTraffic;
    }

    public void setVecFromAnchor(ZPoint vecFromAnchor) {
        this.vecFromAnchor = vecFromAnchor;
    }

    public ZPoint getVecFromAnchor() {
        return vecFromAnchor;
    }

    public void setAnchorTangent(ZPoint anchorTangent) {
        this.anchorTangent = anchorTangent;
    }

    public ZPoint getAnchorTangent() {
        return anchorTangent;
    }

    /* ------------- draw ------------- */
}
