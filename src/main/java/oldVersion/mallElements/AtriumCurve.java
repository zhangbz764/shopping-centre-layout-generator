package oldVersion.mallElements;

import advancedGeometry.ZCatmullRom;
import basicGeometry.ZPoint;
import main.MallConst;
import math.ZGeoMath;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.util.List;

/**
 * curve atrium in a shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/5/19
 * @time 14:29
 */
public class AtriumCurve {
    private WB_Point center;
    private WB_Point[] controlPoints;
    private WB_Polygon shape;

    /* ------------- constructor ------------- */

    public AtriumCurve(WB_Point center, int controlPointNum) {
        this.center = center;
        this.controlPoints = new WB_Point[controlPointNum];

        double theta = (Math.PI * 2) / controlPointNum;
        for (int i = 0; i < controlPointNum; i++) {
            double angle = theta * i;
            controlPoints[i] = new WB_Point(
                    center.xd() + MallConst.ATRIUM_R * Math.cos(angle),
                    center.yd() + MallConst.ATRIUM_R * Math.sin(angle)
            );
        }
        ZCatmullRom catmullRom = new ZCatmullRom(controlPoints, 5, true);
        this.shape = catmullRom.getAsWB_Polygon();
    }

    /* ------------- member function ------------- */

    public void changePosition(double x, double y) {
        double dirX = x - center.xd();
        double dirY = y - center.yd();

        this.center = new WB_Point(x, y);
        for (int i = 0; i < controlPoints.length; i++) {
            double oriX = controlPoints[i].xd();
            double oriY = controlPoints[i].yd();
            controlPoints[i].set(oriX + dirX, oriY + dirY);
        }
        for (int j = 0; j < shape.getNumberOfPoints(); j++) {
            double oriX = shape.getPoint(j).xd();
            double oriY = shape.getPoint(j).yd();
            shape.getPoint(j).set(oriX + dirX, oriY + dirY);
        }
    }

    public void changeShape(WB_Point[] controlPoints) {
        this.controlPoints = controlPoints;
        ZCatmullRom catmullRom = new ZCatmullRom(controlPoints, 5, true);
        this.shape = catmullRom.getAsWB_Polygon();
    }

    public void changeControlNum(int controlPointNum) {
        this.controlPoints = new WB_Point[controlPointNum];
        List<ZPoint> dividedPoints = ZGeoMath.splitPolyLineEdge(shape, controlPointNum);
        int ptsNum = dividedPoints.size();
        for (int i = 0; i < ptsNum; i++) {
            controlPoints[i] = dividedPoints.get(i).toWB_Point();
        }
        ZCatmullRom catmullRom = new ZCatmullRom(controlPoints, 5, true);
        this.shape = catmullRom.getAsWB_Polygon();
    }

    public boolean isCenter(WB_Point test) {
        return test.equals(center);
    }

    /* ------------- setter & getter ------------- */

    public WB_Point getCenter() {
        return center;
    }

    public WB_Point[] getControlPoints() {
        return controlPoints;
    }

    public WB_Polygon getShape() {
        return shape;
    }

    /* ------------- draw ------------- */
}
