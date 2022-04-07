package testPrototype;

import advancedGeometry.rectCover.ZRectCover;
import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import guo_cam.CameraController;
import igeo.ICurve;
import igeo.IG;
import igeo.IVec;
import mallElementNew.StructureGrid;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import render.ZRender;
import transform.ZTransform;

import java.util.List;

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
//        size(1280, 720, SVG, ".\\src\\test\\resources\\grid_modulus.svg");
        size(1280, 720, SVG, ".\\src\\test\\resources\\grid_shape.svg");
//        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private Polygon boundary1;
    private Polygon boundary2;
    private StructureGrid[] grid1;
    private StructureGrid[] grid2;

    private List<ZLine> rays;

    private StructureGrid gi;

    private Polygon b1, b2, b3, b4;
    private StructureGrid[] g1, g2, g3, g4;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
        this.gcam = new CameraController(this);
        gcam.top();
        this.jtsRender = new JtsRender(this);

        loadBoundary();

        // rect
//        ZRectCover zrc1 = new ZRectCover(boundary1, 2);
//        ZRectCover zrc2 = new ZRectCover(boundary2, 3);
//        this.rays = zrc2.getRayExtends();
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
//        ZRectCover zrc3 = new ZRectCover(boundary1, 2);
//        ZRectCover zrc4 = new ZRectCover(boundary2, 2);
//
//        this.grid1 = new StructureGrid[zrc3.getBestRects().size()];
//        for (int i = 0; i < grid1.length; i++) {
//            grid1[i] = new StructureGrid(zrc3.getBestRects().get(i), 8.4);
//        }
//        this.grid2 = new StructureGrid[zrc4.getBestRects().size()];
//        for (int i = 0; i < grid2.length; i++) {
//            grid2[i] = new StructureGrid(zrc4.getBestRects().get(i), 9);
//        }

        // grid index
//        Polygon p = ZFactory.jtsgf.createPolygon(
//                new Coordinate[]{
//                        new Coordinate(0, 0),
//                        new Coordinate(72, 0),
//                        new Coordinate(72, 72),
//                        new Coordinate(0, 72),
//                        new Coordinate(0, 0)
//                }
//        );
//        this.gi = new StructureGrid(p, 9);

        // different shape
        ZRectCover z1 = new ZRectCover(b1, 2);
        ZRectCover z2 = new ZRectCover(b2, 3);
        ZRectCover z3 = new ZRectCover(b3, 3);
        ZRectCover z4 = new ZRectCover(b4, 2);

        this.g1 = new StructureGrid[z1.getBestRects().size()];
        for (int i = 0; i < g1.length; i++) {
            g1[i] = new StructureGrid(z1.getBestRects().get(i), 8.4);
        }
        this.g2 = new StructureGrid[z2.getBestRects().size()];
        for (int i = 0; i < g2.length; i++) {
            g2[i] = new StructureGrid(z2.getBestRects().get(i), 8.4);
        }
        this.g3 = new StructureGrid[z3.getBestRects().size()];
        for (int i = 0; i < g3.length; i++) {
            g3[i] = new StructureGrid(z3.getBestRects().get(i), 8.4);
        }
        this.g4 = new StructureGrid[z4.getBestRects().size()];
        for (int i = 0; i < g4.length; i++) {
            g4[i] = new StructureGrid(z4.getBestRects().get(i), 8.4);
        }
    }

    private void loadBoundary() {
        String path = ".\\src\\test\\resources\\testMall.3dm";
        IG.init();
        IG.open(path);

        ICurve[] curves = IG.layer("testGrid").curves();
        this.boundary1 = (Polygon) ZTransform.ICurveToJts(curves[0]);
        this.boundary2 = (Polygon) ZTransform.ICurveToJts(curves[1]);

        ICurve[] curves2 = IG.layer("testGrid2").curves();
        this.b1 = (Polygon) ZTransform.ICurveToJts(curves2[0]);
        this.b2 = (Polygon) ZTransform.ICurveToJts(curves2[1]);
        this.b3 = (Polygon) ZTransform.ICurveToJts(curves2[2]);
        this.b4 = (Polygon) ZTransform.ICurveToJts(curves2[3]);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);

//        strokeWeight(3);
//        stroke(0);
//        noFill();
//        jtsRender.drawGeometry(boundary1);
//        jtsRender.drawGeometry(boundary2);
//
//
//        strokeWeight(0.5f);
//        stroke(100);
//        for (StructureGrid g : grid1) {
//            for (ZLine l : g.getAllLines()) {
//                ZRender.drawZLine2D(this, l);
//            }
//        }
//        for (StructureGrid g : grid2) {
//            for (ZLine l : g.getAllLines()) {
//                ZRender.drawZLine2D(this, l);
//            }
//        }
//
//        strokeWeight(2);
//        stroke(190, 60, 45);
//        for (StructureGrid g : grid1) {
//            jtsRender.drawGeometry(g.getRect());
//        }
//        for (StructureGrid g : grid2) {
//            jtsRender.drawGeometry(g.getRect());
//        }
//
//        for (ZLine l : rays) {
//            ZRender.drawZLine2D(this, l);
//        }

//        jtsRender.drawGeometry(gi.getRect());
//        for (int i = 0; i < gi.getLon10().size(); i++) {
//            ZLine l = gi.getLon10().get(i);
//            ZRender.drawZLine2D(this, gi.getLon10().get(i));
//            fill(0);
//            text(i, l.getPt0().xf(), l.getPt0().yf());
//        }
//        for (int i = 0; i < gi.getLat12().size(); i++) {
//            ZLine l = gi.getLat12().get(i);
//            ZRender.drawZLine2D(this, gi.getLat12().get(i));
//            text(i, l.getPt0().xf(), l.getPt0().yf());
//        }

        strokeWeight(3);
        stroke(0);
        noFill();
        jtsRender.drawGeometry(b1);
        jtsRender.drawGeometry(b2);
        jtsRender.drawGeometry(b3);
        jtsRender.drawGeometry(b4);

        strokeWeight(0.5f);
        stroke(100);
        for (StructureGrid g : g1) {
            for (ZLine l : g.getAllLines()) {
                ZRender.drawZLine2D(this, l);
            }
        }
        for (StructureGrid g : g2) {
            for (ZLine l : g.getAllLines()) {
                ZRender.drawZLine2D(this, l);
            }
        }
        for (StructureGrid g : g3) {
            for (ZLine l : g.getAllLines()) {
                ZRender.drawZLine2D(this, l);
            }
        }
        for (StructureGrid g : g4) {
            for (ZLine l : g.getAllLines()) {
                ZRender.drawZLine2D(this, l);
            }
        }

        println("Finished.");
        exit();
    }

    public void keyPressed() {
        if (key == 's') {
            save(".\\src\\test\\resources\\grid");
        }
        if (key == 'e') {
            IG.init();
            ZTransform.PolygonToICurve(boundary2).layer("boundary");
            for (ZLine l : rays) {
                new ICurve(
                        new IVec[]{
                                new IVec(l.getPt0().toIPoint()),
                                new IVec(l.getPt1().toIPoint())
                        }
                ).layer("rays");
            }
            for (StructureGrid g : grid2) {
                ZTransform.PolygonToICurve(g.getRect()).layer("rect");
            }
            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\grid_rays.3dm");
        }

        if (key == 'q') {
            IG.init();
            ZTransform.PolygonToICurve(gi.getRect()).layer("rect");
            for (ZLine l : gi.getAllLines()) {
                new ICurve(
                        new IVec[]{
                                new IVec(l.getPt0().toIPoint()),
                                new IVec(l.getPt1().toIPoint())
                        }
                ).layer("lines");
            }
            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\grid_index.3dm");
        }
    }

}
