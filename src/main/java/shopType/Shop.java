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
public class Shop {
    private WB_Polygon shape;
    private double area;
    private String shopType;

    /* ------------- constructor ------------- */

    public Shop() {

    }

    public Shop(WB_Polygon shape, String shopType) {
        setShape(shape);
        setShopType(shopType);
    }

    /* ------------- set & get ------------- */

    public void setShape(WB_Polygon shape) {
        this.shape = shape;
        this.area = Math.abs(shape.getSignedArea());
    }

    public void setShopType(String shopType) {
        this.shopType = shopType;
    }

    public double getArea() {
        return area;
    }

    public WB_Polygon getShape() {
        return shape;
    }

    public String getShopType() {
        return shopType;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render render) {
        render.drawPolygonEdges2D(shape);
    }

    @Override
    public String toString() {
        return "Shop{" +
                "shop type = " + shopType +
                ", shape = " + shape +
                ", area = " + area +
                "}";
    }
}
