package mallElementNew;

import advancedGeometry.ZCatmullRom;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Vector;

import java.util.List;

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

    private WB_Point center;
    private WB_Point[] originalShapePoints;
    private WB_Point[] shapePoints;
    private WB_Vector[] shapeVectors;
    private WB_Polygon shape;

    private double area;

    private boolean curve;
    private String atriumType;

    /* ------------- constructor ------------- */

    public AtriumNew(WB_Point _center, WB_Point[] _shapePoints, boolean _ifCurve) {
        this.center = _center;
        this.shapePoints = _shapePoints;
        updateOriginalShapePoints();
        this.shapePtsNum = shapePoints.length;
        this.curve = _ifCurve;

        updateShape();
    }

    /* ------------- member function ------------- */

    /**
     * update shape vectors and shape polygon
     *
     * @param
     * @return void
     */
    public void updateShape() {
        this.shapeVectors = new WB_Vector[shapePoints.length];
        for (int i = 0; i < shapeVectors.length; i++) {
            shapeVectors[i] = new WB_Vector(shapePoints[i].sub(center));
        }
        if (curve) {
            ZCatmullRom catmullRom = new ZCatmullRom(shapePoints, 10, true);
            this.shape = catmullRom.getAsWB_Polygon();
        } else {
            WB_Point[] constructor = new WB_Point[shapePoints.length + 1];
            System.arraycopy(shapePoints, 0, constructor, 0, shapePoints.length);
            constructor[constructor.length - 1] = constructor[0];
            this.shape = new WB_Polygon(constructor);
        }
    }

    /**
     * update area and type
     *
     * @param
     * @return void
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

    /**
     * update shape using center and vectors
     *
     * @param
     * @return void
     */
    public void updateShapeByCenter() {
        for (int i = 0; i < shapePoints.length; i++) {
            shapePoints[i].set(center.add(shapeVectors[i]));
        }
        updateShape();
    }

    /**
     * update original shape points (for rotating)
     *
     * @param
     * @return void
     */
    public void updateOriginalShapePoints() {
        this.originalShapePoints = new WB_Point[shapePoints.length];
        for (int i = 0; i < shapePoints.length; i++) {
            originalShapePoints[i] = new WB_Point(shapePoints[i].xd(), shapePoints[i].yd());
        }
    }

    public void updateShapeByArea() {

    }

    public void reverseCurve() {
        this.curve = !curve;
    }

    /* ------------- setter & getter ------------- */

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
