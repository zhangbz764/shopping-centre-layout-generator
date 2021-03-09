package formInteractive.atrium;

import geometry.ZPoint;
import wblut.geom.WB_Polygon;

import java.util.List;

/**
 * new version of atrium in the shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/2/17
 * @time 23:12
 */
public abstract class NewAtrium {
    private ZPoint center;
    private String shapeType;

    private List<ZPoint> shapePoints;
    private WB_Polygon shape;

    /* ------------- constructor ------------- */

    public NewAtrium(){

    }

    public NewAtrium(ZPoint center, String shapeType) {
        setCenter(center);
        setShapeType(shapeType);
    }

    /* ------------- setter & getter ------------- */

    public void setCenter(ZPoint center) {
        this.center = center;
    }

    public void setShapeType(String shapeType) {
        this.shapeType = shapeType;
    }

    public void setShapePoints(List<ZPoint> shapePoints) {
        this.shapePoints = shapePoints;
    }

    public void setShape(WB_Polygon shape) {
        this.shape = shape;
    }

    public ZPoint getCenter() {
        return center;
    }

    public String getShapeType() {
        return shapeType;
    }

    public List<ZPoint> getShapePoints() {
        return shapePoints;
    }

    public WB_Polygon getShape() {
        return shape;
    }

    /* ------------- draw ------------- */
}
