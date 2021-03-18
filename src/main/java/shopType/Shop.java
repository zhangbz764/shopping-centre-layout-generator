package shopType;

import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

/**
 * abstract class of a shop
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 22:44
 */
public abstract class Shop {
    private WB_Point location;
    private WB_Polygon shape;
    private double area;

    /* ------------- constructor ------------- */

    public Shop() {

    }

    public Shop(WB_Point location, WB_Polygon shape) {
        setLocation(location);
        setShape(shape);
    }

    /* ------------- set & get ------------- */

    public void setLocation(WB_Point location) {
        this.location = location;
    }

    public void setShape(WB_Polygon shape) {
        this.shape = shape;
        this.area = Math.abs(shape.getSignedArea());
    }

    public double getArea() {
        return area;
    }

    public WB_Point getLocation() {
        return location;
    }

    public WB_Polygon getShape() {
        return shape;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render) {
        render.drawPolygonEdges2D(shape);
    }

    @Override
    public String toString() {
        return "Shop{" +
                "location=" + location +
                ", shape=" + shape +
                ", area=" + area +
                '}';
    }
}
