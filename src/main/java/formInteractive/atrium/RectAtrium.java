package formInteractive.atrium;

import geometry.ZPoint;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * atrium type: rectangle
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/2/18
 * @time 17:11
 */
public class RectAtrium extends NewAtrium {
    private double w, h;



    /* ------------- constructor ------------- */

    public RectAtrium(ZPoint center, double width, double height) {
        super(center, "RectAtrium");

        setWH(width, height);
        List<ZPoint> shapePoints = new ArrayList<>();
        shapePoints.add(center.add(w * 0.5, 0, 0));
        shapePoints.add(center.sub(w * 0.5, 0, 0));
        shapePoints.add(center.add(0, h * 0.5, 0));
        shapePoints.add(center.sub(0, h * 0.5, 0));

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

    /* ------------- setter & getter ------------- */

    public void setWH(double width, double height) {
        this.w = width;
        this.h = height;
    }

    /* ------------- draw ------------- */
}
