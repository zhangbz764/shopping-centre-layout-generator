package testPrototype;

import advancedGeometry.ZSkeleton;
import basicGeometry.ZFactory;
import guo_cam.CameraController;
import igeo.ICurve;
import igeo.IG;
import mallElementNew.MainTraffic;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Point;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/5
 * @time 16:12
 */
public class TestMallSkeleton extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
//        size(1280, 720, SVG, ".\\src\\test\\resources\\skeleton.svg");
//        size(1280, 720, SVG, ".\\src\\test\\resources\\traffic.svg");
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private Polygon[] boundaries;
    private ZSkeleton[] skeletons;

    private Polygon testBoundary;
    private MainTraffic mainTraffic;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
        this.gcam = new CameraController(this);
        gcam.top();
        this.jtsRender = new JtsRender(this);

        loadBoundary();

        this.skeletons = new ZSkeleton[boundaries.length];
        for (int i = 0; i < boundaries.length; ++i) {
            skeletons[i] = new ZSkeleton(boundaries[i]);
        }

        this.mainTraffic = new MainTraffic(testBoundary, 4);


        Polygon polygon = ZFactory.jtsgf.createPolygon(
                new Coordinate[]{
                        new Coordinate(0, 0,100),
                        new Coordinate(100, 0,100),
                        new Coordinate(100, 100,100),
                        new Coordinate(0, 100,100),
                        new Coordinate(0, 0,100)
                }
        );
        LineString ls = ZFactory.jtsgf.createLineString(
                new Coordinate[]{
                        new Coordinate(0, -50,100),
                        new Coordinate(150, 100,100)
                }
        );
        System.out.println(polygon.intersects(ls));
        System.out.println(polygon.intersection(ls).getNumPoints());
    }

    private void loadBoundary() {
        String path = ".\\src\\test\\resources\\testMall.3dm";
        IG.init();
        IG.open(path);

        ICurve[] curvesForSkel = IG.layer("testSkeleton").curves();
        this.boundaries = new Polygon[curvesForSkel.length];
        for (int i = 0; i < curvesForSkel.length; ++i) {
            boundaries[i] = (Polygon) ZTransform.ICurveToJts(curvesForSkel[i]);
        }

        ICurve[] curvesForTraf = IG.layer("testTraffic").curves();
        this.testBoundary = (Polygon) ZTransform.ICurveToJts(curvesForTraf[0]);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
//        for (ZSkeleton s : skeletons) {
//            ZRender.drawSkeleton(this, s);
//        }

        pushStyle();
        noFill();
        stroke(0);
        strokeWeight(3);
        jtsRender.drawGeometry(testBoundary);
        strokeWeight(1);
        jtsRender.drawGeometry(mainTraffic.getMainTrafficCurve());
        stroke(52, 170, 187);
        strokeWeight(1.5f);
        jtsRender.drawGeometry(mainTraffic.getMainTrafficBuffer());

        stroke(128);
        strokeWeight(0.5f);
        for (int i = 0; i < mainTraffic.getInnerNodes().size() - 1; i++) {
            line(
                    mainTraffic.getInnerNodes().get(i).xf(),
                    mainTraffic.getInnerNodes().get(i).yf(),
                    mainTraffic.getInnerNodes().get(i + 1).xf(),
                    mainTraffic.getInnerNodes().get(i + 1).yf()
            );
        }
        line(
                mainTraffic.getInnerNodes().get(0).xf(),
                mainTraffic.getInnerNodes().get(0).yf(),
                mainTraffic.getEntryNodes().get(0).xf(),
                mainTraffic.getEntryNodes().get(0).yf()
        );
        line(
                mainTraffic.getInnerNodes().get(mainTraffic.getInnerNodes().size() - 1).xf(),
                mainTraffic.getInnerNodes().get(mainTraffic.getInnerNodes().size() - 1).yf(),
                mainTraffic.getEntryNodes().get(1).xf(),
                mainTraffic.getEntryNodes().get(1).yf()
        );

        noStroke();
        fill(190, 60, 45);
        for (WB_Point in : mainTraffic.getInnerNodes()) {
            ellipse(in.xf(), in.yf(), 3, 3);
        }
        fill(128);
        for (WB_Point en : mainTraffic.getEntryNodes()) {
            ellipse(en.xf(), en.yf(), 3, 3);
        }

        popStyle();

//        println("Finished.");
//        exit();
    }

    public void keyPressed() {

    }

}
