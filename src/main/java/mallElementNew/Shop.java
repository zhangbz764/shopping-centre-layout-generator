package mallElementNew;

import math.ZMath;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Polygon;

/**
 * a single shop in the shopping mall
 * SP - 普通商铺
 * CZL - 次主力店
 * ZL - 主力店
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 22:44
 */
public class Shop {
    private Polygon shape;
    private Point center;

    private double area;
    private String shopType;
    private int[] color;

    /* ------------- constructor ------------- */

    public Shop(Polygon shape) {
        this.shape = shape;
        this.center = shape.getCentroid();
        this.area = shape.getArea();

        classifyType();
    }

    public Shop(WB_Polygon shape) {
        this.shape = ZTransform.WB_PolygonToPolygon(shape);
        this.center = this.shape.getCentroid();
        this.area = this.shape.getArea();

        classifyType();
    }

    /* ------------- member function ------------- */

    /**
     * classify different type of shop based on the area
     *
     * @return void
     */
    private void classifyType() {
        if (area >= 80 && area <= 300) {
            this.shopType = "SP";
            int c = (int) ZMath.mapToRegion(area, 80, 300, 90, 170);
            this.color = new int[]{c, c, c};
        } else if (area > 300 && area <= 2000) {
            this.shopType = "CZL";
            this.color = new int[]{79, 163, 219};
        } else if (area > 2000) {
            this.shopType = "ZL";
            this.color = new int[]{219, 163, 79};
        } else {
            this.shopType = "invalid";
            this.color = new int[]{50, 50, 50};
        }
    }

    /* ------------- setter & getter ------------- */

    public double getArea() {
        return area;
    }

    public Polygon getShape() {
        return shape;
    }

    public WB_Polygon getShapeWB() {
        return ZTransform.PolygonToWB_Polygon(shape);
    }

    public Point getCenter() {
        return center;
    }

    public String getShopType() {
        return shopType;
    }

    /* ------------- draw ------------- */

    public void display(PApplet app, JtsRender jtsRender) {
        app.fill(color[0], color[1], color[2]);
        jtsRender.drawGeometry(shape);
    }

    public void displayText(PApplet app) {
        app.text(String.format("%.2f", area), (float) center.getX(), (float) center.getY());
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
