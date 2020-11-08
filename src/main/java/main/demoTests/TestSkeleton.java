package main.demoTests;

import Guo_Cam.CameraController;
import processing.core.PApplet;
import geometry.ZSkeleton;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/31
 * @time 11:27
 * @description
 */
public class TestSkeleton extends PApplet {
    public void settings() {
        size(1000, 1800, P3D);
    }

    WB_Point[] pts;
    WB_Polygon poly;
    ZSkeleton ss;

    WB_Render render;
    CameraController gcam;


    public void setup() {
        gcam = new CameraController(this);
        render = new WB_Render(this);

        pts = new WB_Point[6];
        pts[0] = new WB_Point(50, 100);
        pts[1] = new WB_Point(800, 400);
        pts[2] = new WB_Point(900, 500);
        pts[3] = new WB_Point(500, 800);
        pts[4] = new WB_Point(100, 600);
        pts[5] = new WB_Point(50, 100);
        poly = new WB_Polygon(pts);


        ss = new ZSkeleton(poly);
        ss.printInfo();

    }

    public void draw() {
        background(255);
        noFill();
        gcam.begin2d();
        render.drawPolygonEdges2D(poly);
        ss.display(this);
        gcam.begin3d();
        render.drawPolygonEdges2D(poly);
        ss.display(this);
    }

    public void keyPressed() {
        if (key == '1') {
            pts[0] = pts[5] = new WB_Point(mouseX, mouseY);
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
        ss = new ZSkeleton(poly);
        ss.printInfo();
    }

}
