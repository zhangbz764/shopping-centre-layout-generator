package shoptype;

import geometry.ZGeoFactory;
import geometry.ZPoint;
import geometry.ZSkeleton;
import kn.uni.voronoitreemap.extension.VoroCellObject;
import math.ZGeoMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.triangulate.VoronoiDiagramBuilder;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    public ShopGenerator(List<WB_Polygon> shopBlock, List<ZSkeleton> skeletons) {
        assert shopBlock.size() == skeletons.size();
        this.polyLineToGenerate = new ArrayList<>();
        this.splitPoints = new ArrayList<>();

        for (ZSkeleton skel : skeletons) {
            WB_PolyLine polyLine = ZGeoFactory.createWB_PolyLine(skel.getRidges());
            polyLineToGenerate.add(polyLine);
        }
        splitPolyLine();

        this.voronois = new ArrayList<>();
        for (int i = 0; i < splitPoints.size(); i++) {
            List<WB_Point> points = new ArrayList<>();
            for (ZPoint p : splitPoints.get(i)) {
                points.add(p.toWB_Point());
            }
            WB_Voronoi2D voronoi = WB_VoronoiCreator.getClippedVoronoi2D(points, shopBlock.get(i));
            voronois.add(voronoi);
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
            splitPoints.add(ZGeoMath.splitPolyLineEdge(pl, 8));
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
