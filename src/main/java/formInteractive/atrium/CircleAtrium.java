package formInteractive.atrium;

import geometry.ZPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * atrium type: circle
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/2/17
 * @time 23:03
 */
public class CircleAtrium extends NewAtrium {
    private double r;



    /* ------------- constructor ------------- */

    public CircleAtrium(ZPoint center, double radius) {
        super(center, "CircleAtrium");

        List<ZPoint> shapePoints = new ArrayList<>();
        shapePoints.add(center.add(r, 0, 0));

        super.setShapePoints(shapePoints);
    }

    /* ------------- setter & getter ------------- */

    public void setR(double radius) {
        this.r = radius;
    }

    /* ------------- draw ------------- */

}
