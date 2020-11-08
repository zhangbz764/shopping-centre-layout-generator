package main.demoTests;

import geometry.ZPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/23
 * @time 11:41
 * @description 测试几何图形布尔关系
 */
public class TestGeoRelation extends PApplet {
    public void settings() {
        size(1000, 1000, P3D);
    }

    GeometryFactory gf = new GeometryFactory();
    Polygon poly;
    ZPoint mouse;
    JtsRender render;

    public void setup() {
        render = new JtsRender(this);

        Coordinate[] vertices = new Coordinate[6];
        vertices[0] = new Coordinate(100, 100);
        vertices[1] = new Coordinate(700, 100);
        vertices[2] = new Coordinate(800, 400);
        vertices[3] = new Coordinate(500, 800);
        vertices[4] = new Coordinate(100, 600);
        vertices[5] = new Coordinate(100, 100);


        poly = gf.createPolygon(vertices);
        mouse = new ZPoint(500, 500);
    }

    public void draw() {
        background(255);

        render.draw(poly);
        mouse.set(mouseX, mouseY);
        mouse.displayAsPoint(this);
        println(poly.contains(mouse.toJtsPoint()));
    }

}
