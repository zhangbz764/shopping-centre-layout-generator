package main.demoTests;

import Guo_Cam.CameraController;
import geometry.ZSkeleton;
import processing.core.PApplet;
import render.DisplayBasic;
import wblut.geom.WB_GeometryFactory3D;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/31
 * @time 11:27
 * @description 测试ZSkeleton
 */
public class TestSkeleton extends PApplet {
    public void settings() {
        size(1800, 1000, P3D);
    }

    WB_Point[] pts;
    WB_Polygon poly;
    ZSkeleton ss;

    WB_Render render;
    CameraController gcam;
    WB_GeometryFactory3D wbgf = new WB_GeometryFactory3D();

    public void setup() {
        gcam = new CameraController(this);

        render = new WB_Render(this);

        pts = new WB_Point[5];
        pts[0] = new WB_Point(0, 0);
        pts[1] = new WB_Point(0, 100);
        pts[2] = new WB_Point(50, 50);
        pts[3] = new WB_Point(200, 0);
        pts[4] = new WB_Point(0, 0);
        poly = new WB_Polygon(pts);
        System.out.println("poly area" + poly.getSignedArea());

        System.out.println("origin:" + poly.getNormal());
        ss = new ZSkeleton(poly, true);
        ss.printInfo();

    }

    public void draw() {
        background(255);
        DisplayBasic.drawAxis(this);
        noFill();
        gcam.begin2d();
        render.drawPolygonEdges2D(poly);
        ss.display(this);
        gcam.begin3d();
        render.drawPolygonEdges(poly);
        fill(0);
        render.drawPoint(pts[2], 20);
        ss.display(this);
    }

    public void keyPressed() {
        if (key == '1') {
            pts[0] = pts[4] = new WB_Point(mouseX, mouseY);
        }
        if (key == '2') {
            pts[1] = new WB_Point(mouseX, mouseY);
        }
        if (key == '3') {
            pts[2] = new WB_Point(mouseX, mouseY);
        }
        if (key == '4') {
            pts[3] = new WB_Point(mouseX, mouseY);
        }
        if (key == '5') {
            pts[4] = new WB_Point(mouseX, mouseY);
        }
        poly = new WB_Polygon(pts);
        ss = new ZSkeleton(poly, true);
        ss.printInfo();
    }

    public void mouseDragged() {
        if (mouseButton == RIGHT) {
            pts[0] = pts[4] = new WB_Point(mouseX, mouseY);
            poly = new WB_Polygon(pts);
            ss = new ZSkeleton(poly, true);
            ss.printInfo();
        }
    }

}
