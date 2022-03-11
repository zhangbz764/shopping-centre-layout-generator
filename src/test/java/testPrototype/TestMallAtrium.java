package testPrototype;

import guo_cam.CameraController;
import mallElementNew.AtriumRaw;
import mallElementNew.AtriumRawFactory;
import processing.core.PApplet;
import render.JtsRender;
import wblut.geom.WB_Point;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/10
 * @time 18:25
 */
public class TestMallAtrium extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1280, 720, SVG, ".\\src\\test\\resources\\atrium_factory.svg");
//        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private WB_Point[] atriumBases;
    private AtriumRaw[] atriumRaws1;
    private AtriumRaw[] atriumRaws2;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
//        this.gcam = new CameraController(this);
//        gcam.top();
        this.jtsRender = new JtsRender(this);

        createAtrium();

    }

    private void createAtrium() {
        this.atriumBases = new WB_Point[]{
                new WB_Point(-10, 0),
                new WB_Point(50, 0),
                new WB_Point(100, 0),
                new WB_Point(150, 0),
                new WB_Point(200, 0),
                new WB_Point(250, 0),
                new WB_Point(300, 0),
                new WB_Point(350, 0),
                new WB_Point(-10, 50),
                new WB_Point(50, 50),
                new WB_Point(100, 50),
                new WB_Point(150, 50),
                new WB_Point(200, 50),
                new WB_Point(250, 50),
                new WB_Point(300, 50),
                new WB_Point(350, 50),
        };

        this.atriumRaws1 = new AtriumRaw[8];
        atriumRaws1[0] = AtriumRawFactory.createAtriumTri(atriumBases[0], 500, false);
        atriumRaws1[1] = AtriumRawFactory.createAtriumSq(atriumBases[1], 500, false);
        atriumRaws1[2] = AtriumRawFactory.createAtriumTra(atriumBases[2], 500, false);
        atriumRaws1[3] = AtriumRawFactory.createAtriumPen(atriumBases[3], 500, false);
        atriumRaws1[4] = AtriumRawFactory.createAtriumHex(atriumBases[4], 500, false);
        atriumRaws1[5] = AtriumRawFactory.createAtriumHex2(atriumBases[5], 500, false);
        atriumRaws1[6] = AtriumRawFactory.createAtriumLS(atriumBases[6], 500, false);
        atriumRaws1[7] = AtriumRawFactory.createAtriumOct(atriumBases[7], 500, false);

        this.atriumRaws2 = new AtriumRaw[8];
        atriumRaws2[0] = AtriumRawFactory.createAtriumTri(atriumBases[8], 500, true);
        atriumRaws2[1] = AtriumRawFactory.createAtriumSq(atriumBases[9], 500, true);
        atriumRaws2[2] = AtriumRawFactory.createAtriumTra(atriumBases[10], 500, true);
        atriumRaws2[3] = AtriumRawFactory.createAtriumPen(atriumBases[11], 500, true);
        atriumRaws2[4] = AtriumRawFactory.createAtriumHex(atriumBases[12], 500, true);
        atriumRaws2[5] = AtriumRawFactory.createAtriumHex2(atriumBases[13], 500, true);
        atriumRaws2[6] = AtriumRawFactory.createAtriumLS(atriumBases[14], 500, true);
        atriumRaws2[7] = AtriumRawFactory.createAtriumOct(atriumBases[15], 500, true);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);

        for (AtriumRaw a : atriumRaws1) {
            pushStyle();
            noFill();
            strokeWeight(2);
            jtsRender.drawGeometry(a.getShape());

            strokeWeight(1);
            fill(255);
            for (WB_Point p : a.getShapePoints()) {
                ellipse(p.xf(), p.yf(), 2, 2);
            }

            noStroke();
            fill(190, 60, 45);
            ellipse(a.getCenter().xf(), a.getCenter().yf(), 4, 4);
            popStyle();
        }

        for (AtriumRaw a : atriumRaws2) {
            pushStyle();
            noFill();
            stroke(0);
            strokeWeight(2);
            jtsRender.drawGeometry(a.getShape());

            strokeWeight(1);
            fill(255);
            for (WB_Point p : a.getShapePoints()) {
                ellipse(p.xf(), p.yf(), 2, 2);
            }

            strokeWeight(0.5f);
            stroke(128);
            for (int i = 0; i < a.getShapePoints().length; i++) {
                line(
                        a.getShapePoints()[i].xf(),
                        a.getShapePoints()[i].yf(),
                        a.getShapePoints()[(i + 1) % a.getShapePoints().length].xf(),
                        a.getShapePoints()[(i + 1) % a.getShapePoints().length].yf()
                );
            }

            noStroke();
            fill(190, 60, 45);
            ellipse(a.getCenter().xf(), a.getCenter().yf(), 4, 4);
            popStyle();
        }

        println("Finished.");
        exit();
    }

}
