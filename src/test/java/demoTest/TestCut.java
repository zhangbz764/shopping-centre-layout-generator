package demoTest;

import basicGeometry.ZFactory;
import guo_cam.CameraController;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/5/22
 * @time 20:24
 */
public class TestCut extends PApplet {

    public static void main(String[] args) {
        PApplet.main("demoTest.TestCut");
    }

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private LineString boundary;
    private Polygon cut;
    private Geometry cutResult;
    private JtsRender jtsRender;
    private CameraController gcam;

    public void setup() {
        this.jtsRender = new JtsRender(this);
        this.gcam = new CameraController(this);

        this.boundary = ZFactory.jtsgf.createLineString(
                new Coordinate[]{
                        new Coordinate(0,0),
                        new Coordinate(100,0),
                        new Coordinate(130,20),
                        new Coordinate(120,40),
                        new Coordinate(40,100),
                        new Coordinate(0,100),
                        new Coordinate(0,0)
                }
        );

        this.cut = ZFactory.jtsgf.createPolygon(
                new Coordinate[]{
                        new Coordinate(100,15),
                        new Coordinate(150,15),
                        new Coordinate(150,30),
                        new Coordinate(100,30),
                        new Coordinate(100,15)
                }
        );

        this.cutResult = boundary.intersection(cut);
        System.out.println(cutResult.getGeometryType());
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        stroke(0);
        strokeWeight(1);
        noFill();
        jtsRender.drawGeometry(boundary);
        fill(200);
        jtsRender.drawGeometry(cut);
        noFill();
        stroke(255,0,0);
        strokeWeight(3);
        jtsRender.drawGeometry(cutResult);
    }

}
