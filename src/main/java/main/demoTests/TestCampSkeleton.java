package main.demoTests;

import Guo_Cam.CameraController;
import geometry.ZLine;
import geometry.ZPoint;
import geometry.ZSkeleton;
import math.ZMath;
import org.locationtech.jts.geom.*;
import org.twak.camp.*;
import org.twak.utils.collections.Loop;
import org.twak.utils.collections.LoopL;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_GeometryFactory;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/12/5
 * @time 14:33
 * @description 测试campskeleton带洞
 */
public class TestCampSkeleton extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */
    Polygon boundary;
    Polygon polygon;
    WB_Polygon wb_polygon;
    GeometryFactory gf = new GeometryFactory();

    CameraController gcam;
    ZSkeleton skeleton;
    JtsRender jtsRender;
    WB_Render render;

    public void setup() {
        gcam = new CameraController(this);
        jtsRender = new JtsRender(this);
        render = new WB_Render(this);

        Coordinate[] outer = new Coordinate[6]; // counter clockwise
        outer[0] = new Coordinate(100, 100,0);
        outer[1] = new Coordinate(700, 100,0);
        outer[2] = new Coordinate(800, 400,0);
        outer[3] = new Coordinate(500, 800,0);
        outer[4] = new Coordinate(100, 600,0);
        outer[5] = new Coordinate(100, 100,0);
        LinearRing outerL = gf.createLinearRing(outer);

        Coordinate[] inner0 = new Coordinate[5]; // clockwise
        inner0[0] = new Coordinate(250, 200,0);
        inner0[1] = new Coordinate(250, 400,0);
        inner0[2] = new Coordinate(450, 400,0);
        inner0[3] = new Coordinate(450, 200,0);
        inner0[4] = new Coordinate(250, 200,0);
        LinearRing innerL1 = gf.createLinearRing(inner0);

        Coordinate[] inner1 = new Coordinate[5]; // clockwise
        inner1[0] = new Coordinate(500, 500,0);
        inner1[1] = new Coordinate(400, 600,0);
        inner1[2] = new Coordinate(500, 700,0);
        inner1[3] = new Coordinate(600, 500,0);
        inner1[4] = new Coordinate(500, 500,0);
        LinearRing innerL2 = gf.createLinearRing(inner1);

        LinearRing[] inner = new LinearRing[2];
        inner[0] = innerL1;
        inner[1] = innerL2;

        polygon = gf.createPolygon(outerL, inner);
        println(polygon.getNumInteriorRing());
        println(polygon.isSimple());


        wb_polygon = ZTransform.jtsPolygonToWB_Polygon(polygon);
        println(wb_polygon.isSimple());
        println(wb_polygon.getNormal());

        skeleton = new ZSkeleton(wb_polygon, true);

    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        fill(128);
        render.drawPolygonEdges2D(wb_polygon);
        translate(1000, 0, 0);

        fill(128);
        jtsRender.drawGeometry(polygon);
        fill(0);
        textSize(15);
        for (int i = 0; i < polygon.getExteriorRing().getNumPoints(); i++) {
            text(i, (float) polygon.getCoordinates()[i].x, (float) polygon.getCoordinates()[i].y);
        }
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            LineString curr = polygon.getInteriorRingN(i);
            for (int j = 0; j < curr.getNumPoints(); j++) {
                text(j, (float) curr.getCoordinates()[j].x, (float) curr.getCoordinates()[j].y);
            }
        }


        skeleton.display(this);
    }

}
