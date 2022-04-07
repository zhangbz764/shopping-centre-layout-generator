package testPrototype;

import advancedGeometry.ZBSpline;
import basicGeometry.ZFactory;
import guo_cam.CameraController;
import math.ZGeoMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/29
 * @time 22:05
 */
public class TestMallAtriumRound extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1280, 720, SVG, ".\\src\\test\\resources\\atrium_rebuild.svg");
//        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private Polygon atrium;
    private Polygon bspline;
    private Polygon round;
    private Polygon smooth;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
//        this.gcam = new CameraController(this);
//        gcam.top();
        this.jtsRender = new JtsRender(this);

        createPolygon();

        this.bspline = new ZBSpline(atrium.getCoordinates(), 3, 50, ZBSpline.CLOSE).getAsPolygon();
        this.round = ZGeoMath.roundPolygon(atrium, 20, 10);
        this.smooth = ZGeoMath.smoothPolygon(atrium, 5, 3);
    }

    private void createPolygon() {
        Coordinate[] coords = new Coordinate[]{
                new Coordinate(0, 0),
                new Coordinate(100, 0),
                new Coordinate(120, 240),
                new Coordinate(0, 200),
                new Coordinate(-40, 110),
                new Coordinate(0, 0)
        };
        this.atrium = ZFactory.jtsgf.createPolygon(coords);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);

        jtsRender.drawGeometry(atrium);
        ellipse(
                (float) atrium.getCentroid().getX(),
                (float) atrium.getCentroid().getY(),
                5, 5
        );
        translate(200, 0);
        jtsRender.drawGeometry(bspline);
        ellipse(
                (float) atrium.getCentroid().getX(),
                (float) atrium.getCentroid().getY(),
                5, 5
        );
        translate(200, 0);
        jtsRender.drawGeometry(round);
        ellipse(
                (float) atrium.getCentroid().getX(),
                (float) atrium.getCentroid().getY(),
                5, 5
        );
        translate(200, 0);
        jtsRender.drawGeometry(smooth);
        ellipse(
                (float) atrium.getCentroid().getX(),
                (float) atrium.getCentroid().getY(),
                5, 5
        );

        println("Finished.");
        exit();
    }

}
