package mallElementNew;

import advancedGeometry.ZCatmullRom;
import math.ZGeoMath;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;

import java.util.Arrays;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/7/1
 * @time 23:22
 */
public class AtriumNew {
    private final int shapePtsNum;
    private boolean curve;
    private WB_Point center;

    private WB_Point[] shapePoints;
    private WB_Vector[] shapeVectors;
    private WB_Polygon shape;
    private double area;
    private String atriumType;

    private WB_Point[] originalShapePoints;

    /* ------------- constructor ------------- */

    public AtriumNew(WB_Point _center, WB_Point[] _shapePoints, boolean _ifCurve) {
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
            WB_Polygon curveTemp = new ZCatmullRom(shapePoints, 10, true).getAsWB_Polygon();
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
            WB_Polygon curveTemp = new ZCatmullRom(shapePoints, 10, true).getAsWB_Polygon();
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
            ZCatmullRom catmullRom = new ZCatmullRom(shapePoints, 10, true);
            this.shape = catmullRom.getAsWB_Polygon();
        } else {
            WB_Point[] constructor = new WB_Point[shapePtsNum + 1];
            System.arraycopy(shapePoints, 0, constructor, 0, shapePtsNum);
            constructor[constructor.length - 1] = constructor[0];
            this.shape = new WB_Polygon(constructor);
        }
    }

    /**
     * update area and type
     */
    public void updateArea() {
        this.area = Math.abs(shape.getSignedArea());
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

    public WB_Polygon getShape() {
        return shape;
    }

    public double getArea() {
        return area;
    }

    /* ------------- draw ------------- */
}
