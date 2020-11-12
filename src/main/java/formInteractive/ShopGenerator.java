package formInteractive;

import geometry.ZGeoFactory;
import geometry.ZPoint;
import geometry.ZSkeleton;
import math.ZGeoMath;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/8
 * @time 21:35
 * @description
 */

// TODO: 2020/11/8 好多代码结构和健壮性问题
public class ShopGenerator {
    List<WB_PolyLine> polyLineToGenerate;
    List<List<ZPoint>> splitPoints;
    List<WB_Voronoi2D> voronois;

    /* ------------- constructor ------------- */

    public ShopGenerator() {

    }

    public ShopGenerator(List<WB_Polygon> shopBlock, List<ZSkeleton> skeletons) {
        assert shopBlock.size() == skeletons.size();
        this.polyLineToGenerate = new ArrayList<>();
        this.splitPoints = new ArrayList<>();
        this.voronois = new ArrayList<>();

        for (int i = 0; i < skeletons.size(); i++) {
            // maybe null
            WB_PolyLine polyLine = ZGeoFactory.createWB_PolyLine(skeletons.get(i).getRidges());
            if (polyLine != null) {
                polyLineToGenerate.add(polyLine);

                List<ZPoint> splitResult = ZGeoMath.splitWB_PolyLineEdge(polyLine, 8);
                splitPoints.add(splitResult);

                List<WB_Point> points = new ArrayList<>();
                for (ZPoint p : splitResult) {
                    points.add(p.toWB_Point());
                }
                WB_Voronoi2D voronoi = WB_VoronoiCreator.getClippedVoronoi2D(points, shopBlock.get(i));
                voronois.add(voronoi);
            }
        }
    }

    public void performVoronoi() {

    }

    /**
     * @return void
     * @description
     */
    public void splitPolyLine() {
        for (WB_PolyLine pl : polyLineToGenerate) {
            splitPoints.add(ZGeoMath.splitWB_PolyLineEdge(pl, 8));
        }
    }

    public void display(WB_Render3D render, PApplet app) {
        app.pushStyle();
        // draw voronoi
        app.fill(128);
        app.stroke(0);
        app.strokeWeight(3);
        for (WB_Voronoi2D voronoi2D : voronois) {
            for (WB_VoronoiCell2D cell : voronoi2D.getCells()) {
                render.drawPolygonEdges2D(cell.getPolygon());
            }
        }
        // draw point
        app.noStroke();
        app.fill(0, 0, 255);
        for (List<ZPoint> splitPoint : splitPoints) {
            for (ZPoint p : splitPoint) {
                p.displayAsPoint(app, 10);
            }
        }
        app.popStyle();
    }
}
