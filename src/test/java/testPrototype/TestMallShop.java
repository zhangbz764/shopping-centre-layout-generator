package testPrototype;

import advancedGeometry.rectCover.ZRectCover;
import guo_cam.CameraController;
import igeo.ICurve;
import igeo.IG;
import mallElementNew.Shop;
import mallElementNew.ShopManager;
import mallElementNew.StructureGrid;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/16
 * @time 10:41
 */
public class TestMallShop extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private Polygon boundary;
    private Polygon publicSpaceCurve;
    private StructureGrid[] grid;

    private ShopManager shopManager;

    private CameraController gcam;
    private JtsRender jtsRender;

    public void setup() {
        this.gcam = new CameraController(this);
        gcam.top();
        this.jtsRender = new JtsRender(this);

        loadBoundary();

        List<LineString> publicSpaceCurves = new ArrayList<>(ZTransform.PolygonToLineString(publicSpaceCurve));
        Point verify = publicSpaceCurve.getInteriorPoint();

        ZRectCover zrc3 = new ZRectCover(boundary, 3);
        this.grid = new StructureGrid[zrc3.getBestRects().size()];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = new StructureGrid(zrc3.getBestRects().get(i), 8.4);
        }

        this.shopManager = new ShopManager();
        shopManager.updateShopPartition(
                boundary,
                publicSpaceCurves,
                verify,
                grid
        );
    }

    private void loadBoundary() {
        String path = ".\\src\\test\\resources\\testMall.3dm";
        IG.init();
        IG.open(path);

        ICurve[] curvesForBoundary = IG.layer("testShop_boundary").curves();
        this.boundary = (Polygon) ZTransform.ICurveToJts(curvesForBoundary[0]);

        ICurve[] curvesForPSC = IG.layer("testShop_publicSpace").curves();
        this.publicSpaceCurve = (Polygon) ZTransform.ICurveToJts(curvesForPSC[0]);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);

        pushStyle();
        stroke(0);
        strokeWeight(3);
        jtsRender.drawGeometry(boundary);

        stroke(52, 170, 187);
        strokeWeight(1.5f);
        jtsRender.drawGeometry(publicSpaceCurve);

        stroke(190, 60, 45);

        for (Shop shop : shopManager.getAllShops()) {
            jtsRender.drawGeometry(shop.getShape());
        }

        popStyle();
    }

    public void keyPressed() {
        if (key == 's') {
            IG.init();
            ZTransform.PolygonToICurve(boundary).layer("boundary");
            ZTransform.PolygonToICurve(publicSpaceCurve).layer("public");

            for (Shop shop : shopManager.getAllShops()) {
                ZTransform.PolygonToICurve(shop.getShape()).layer("shop");
            }

//            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\shop.3dm");
            IG.save("E:\\AAA_Study\\202112_MasterDegreeThesis\\正文\\图\\shop2.3dm");
        }
    }
}
