package testPrototype;

import advancedGeometry.rectCover.ZRectCover;
import basicGeometry.ZLine;
import guo_cam.CameraController;
import igeo.ICurve;
import igeo.IG;
import mallElementNew.StructureGrid;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import render.ZRender;
import transform.ZTransform;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/8
 * @time 12:21
 */
public class TestMallGrid extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1280, 720, SVG, ".\\src\\test\\resources\\grid_modulus.svg");
//        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private Polygon boundary1;
    private Polygon boundary2;
    private StructureGrid[] grid1;
    private StructureGrid[] grid2;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
//        this.gcam = new CameraController(this);
//        gcam.top();
        this.jtsRender = new JtsRender(this);

        loadBoundary();

        // rect
//        ZRectCover zrc1 = new ZRectCover(boundary1, 2);
//        ZRectCover zrc2 = new ZRectCover(boundary2, 3);
//
//        this.grid1 = new StructureGrid[zrc1.getBestRects().size()];
//        for (int i = 0; i < grid1.length; i++) {
//            grid1[i] = new StructureGrid(zrc1.getBestRects().get(i), 8.4);
//        }
//        this.grid2 = new StructureGrid[zrc2.getBestRects().size()];
//        for (int i = 0; i < grid2.length; i++) {
//            grid2[i] = new StructureGrid(zrc2.getBestRects().get(i), 8.4);
//        }

        // modulus
        ZRectCover zrc3 = new ZRectCover(boundary1, 2);
        ZRectCover zrc4 = new ZRectCover(boundary2, 2);

        this.grid1 = new StructureGrid[zrc3.getBestRects().size()];
        for (int i = 0; i < grid1.length; i++) {
            grid1[i] = new StructureGrid(zrc3.getBestRects().get(i), 8.4);
        }
        this.grid2 = new StructureGrid[zrc4.getBestRects().size()];
        for (int i = 0; i < grid2.length; i++) {
            grid2[i] = new StructureGrid(zrc4.getBestRects().get(i), 9);
        }
    }

    private void loadBoundary() {
        String path = ".\\src\\test\\resources\\testMall.3dm";
        IG.init();
        IG.open(path);

        ICurve[] curves = IG.layer("testGrid").curves();
        this.boundary1 = (Polygon) ZTransform.ICurveToJts(curves[0]);
        this.boundary2 = (Polygon) ZTransform.ICurveToJts(curves[1]);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);

        pushStyle();

        strokeWeight(3);
        stroke(0);
        noFill();
        jtsRender.drawGeometry(boundary1);
        jtsRender.drawGeometry(boundary2);


        strokeWeight(0.5f);
        stroke(100);
        for (StructureGrid g : grid1) {
            for (ZLine l : g.getAllLines()) {
                ZRender.drawZLine2D(this, l);
            }
        }
        for (StructureGrid g : grid2) {
            for (ZLine l : g.getAllLines()) {
                ZRender.drawZLine2D(this, l);
            }
        }

        strokeWeight(2);
        stroke(190, 60, 45);
        for (StructureGrid g : grid1) {
            jtsRender.drawGeometry(g.getRect());
        }
        for (StructureGrid g : grid2) {
            jtsRender.drawGeometry(g.getRect());
        }

        println("Finished.");
        exit();
    }

    public void keyPressed() {
        if (key == 's') {
            save(".\\src\\test\\resources\\grid");
        }
    }

}
