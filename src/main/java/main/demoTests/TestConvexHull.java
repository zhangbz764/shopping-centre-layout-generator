package main.demoTests;

import geometry.ZLine;
import geometry.ZPoint;
import igeo.ICurve;
import igeo.IG;
import math.ZGeoMath;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/15
 * @time 20:48
 * @description
 */
public class TestConvexHull extends PApplet {
    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    List<Geometry> polys;
    List<Geometry> convexHull;
    List<List<Integer>> concavePoints;
    JtsRender jtsRender;

    public void setup() {
        this.jtsRender = new JtsRender(this);

        // 载入几何模型，算凸包
        IG.init();
        IG.open("E:\\AAA_Project\\202009_Shuishi\\codefiles\\test_convex_hull.3dm");
        this.polys = new ArrayList<>();
        this.convexHull = new ArrayList<>();

        ICurve[] polyLines = IG.layer("test").curves();
        for (ICurve polyLine : polyLines) {
            polys.add(ZTransform.ICurveToJts(polyLine));
        }

        for (Geometry g : polys) {
            convexHull.add(g.convexHull());
        }

        // 算凹点
        this.concavePoints = new ArrayList<>();
        for (Geometry g : polys) {
            concavePoints.add(ZGeoMath.getConcavePointIndices((Polygon) g));
        }
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        stroke(0);

        // 测试线与多段线交点
        WB_Polygon newPoly = ZTransform.JtsPolygonToWB_Polygon((Polygon) polys.get(0));
        ZLine ray = new ZLine(new ZPoint(650, 250), new ZPoint(mouseX, mouseY));
        ray.display(this);
        List<ZPoint> inters = ZGeoMath.rayPolygonIntersect2D(ray.toLinePD(), newPoly);
        for (ZPoint p : inters) {
            p.displayAsPoint(this, 15);
        }
        List<Integer> interOrder = ZGeoMath.rayPolygonIntersectIndices2D(ray.toLinePD(), newPoly);
        fill(0);
        if (interOrder.size() != 0) {
            for (int i = 0; i < interOrder.size(); i++) {
                text(i + 1, newPoly.getSegment(interOrder.get(i)).getCenter().xf(), newPoly.getSegment(interOrder.get(i)).getCenter().yf());
            }
        }

        // 找交点最近的那条边 作垂点

        for (int i = 0; i < concavePoints.size(); i++) {
            for (Integer index : concavePoints.get(i)) {

            }
        }

        noFill();
        // 画凹点，画多边形和凸包，打印比例
        for (int i = 0; i < polys.size(); i++) {
            for (Integer index : concavePoints.get(i)) {
                ellipse((float) polys.get(i).getCoordinates()[index].x, (float) polys.get(i).getCoordinates()[index].y, 10, 10);
            }
        }
        for (int i = 0; i < polys.size(); i++) {
            noFill();
            jtsRender.drawGeometry(polys.get(i));
            float ratio = (float) (polys.get(i).getArea() / convexHull.get(i).getArea());
            fill(0);
            textSize(15);
            text(ratio * 100 + "%", (float) convexHull.get(i).getCentroid().getX(), (float) convexHull.get(i).getCentroid().getY());
        }
        stroke(255, 0, 0);
        noFill();
        for (Geometry ch : convexHull) {
            jtsRender.drawGeometry(ch);
        }
    }

}
