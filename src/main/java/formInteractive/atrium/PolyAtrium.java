package formInteractive.atrium;

import geometry.ZPoint;
import math.ZGeoMath;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * atrium type: custom polygon
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/2/20
 * @time 19:42
 */
public class PolyAtrium extends NewAtrium {
    private int pointNum = 3;

    /* ------------- constructor ------------- */

    public PolyAtrium(ZPoint center, int polyPointNum, double initRadius) {
        super(center, "PolyAtrium");

        assert polyPointNum >= 3 : "invalid polygon point number";
        setPointNum(polyPointNum);
        List<ZPoint> shapePoints = new ArrayList<>();
        double angle = (Math.PI * 2) / pointNum;
        for (int i = 0; i < pointNum; i++) {
            ZPoint p = new ZPoint(
                    initRadius * Math.cos(i * angle),
                    initRadius * Math.sin(i * angle),
                    0
            );
            shapePoints.add(p);
        }
        updateShape(shapePoints);
    }

    /* ------------- member function ------------- */

    /**
     * main method to update the shape point and the atrium shape
     *
     * @param shapePoints
     * @return void
     */
    private void updateShape(List<ZPoint> shapePoints) {
        super.setShapePoints(shapePoints);

        List<WB_Point> points = new ArrayList<>();
        for (ZPoint shapePoint : shapePoints) {
            points.add(shapePoint.toWB_Point());
        }
        points.add(shapePoints.get(0).toWB_Point());

        super.setShape(new WB_Polygon(points));
    }

    /**
     * add a point to the poly atrium
     *
     * @param newPoint
     * @return void
     */
    public void addShapePoint(ZPoint newPoint) {
        List<ZPoint> pointList = super.getShapePoints();
        WB_Polygon shape = super.getShape();

        int closest = ZGeoMath.closestSegment(newPoint, shape);
        pointList.add(closest + 1, newPoint);

        updateShape(pointList);
        this.pointNum++;
    }

    /**
     * remove a point from the poly atrium
     *
     * @param newPoint
     * @return void
     */
    private void removeShapePoint(ZPoint newPoint) {
        if (this.pointNum > 3) {
            List<ZPoint> pointList = super.getShapePoints();
            pointList.remove(newPoint);

            updateShape(pointList);
            this.pointNum--;
        }
    }

    /* ------------- setter & getter ------------- */

    public void setPointNum(int pointNum) {
        this.pointNum = pointNum;
    }

    public int getPointNum() {
        return pointNum;
    }

    /* ------------- draw ------------- */
}
