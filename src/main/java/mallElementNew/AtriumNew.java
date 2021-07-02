package mallElementNew;

import advancedGeometry.ZCatmullRom;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

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
    private WB_Point center;
    private WB_Point[] shapePoints;
    private int shapePtsNum;
    private WB_Polygon shape;

    private double area;

    private boolean curve;
    private String atriumType;

    /* ------------- constructor ------------- */

    public AtriumNew(WB_Point _center, WB_Point[] _shapePoints, boolean _ifCurve) {
        this.center = _center;
        this.shapePoints = _shapePoints;
        this.shapePtsNum = shapePoints.length;
        this.curve = _ifCurve;

        if (curve) {
            ZCatmullRom catmullRom = new ZCatmullRom(shapePoints, 10, true);
            this.shape = catmullRom.getAsWB_Polygon();
        } else {
            this.shape = new WB_Polygon(shapePoints);
        }
        this.area = Math.abs(shape.getSignedArea());
        if (area >= 450 && area <= 600) {
            atriumType = "main";
        } else if (area >= 280 && area <= 400) {
            atriumType = "sub";
        } else {
            atriumType = "other";
        }
    }

    /* ------------- member function ------------- */

    public void updateShapeByArea() {

    }

    /* ------------- setter & getter ------------- */

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
