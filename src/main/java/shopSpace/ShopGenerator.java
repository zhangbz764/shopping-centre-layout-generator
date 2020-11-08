package shopSpace;

import formInteractive.SpacialFormGenerator;
import geometry.ZGeoFactory;
import geometry.ZPoint;
import geometry.ZSkeleton;
import math.ZGeoMath;
import processing.core.PApplet;
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;
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

    public ShopGenerator(Map<ZSkeleton, WB_Polygon> blockSkeletonMap) {
        this.polyLineToGenerate = new ArrayList<>();
        this.splitPoints = new ArrayList<>();

        for (ZSkeleton skel : blockSkeletonMap.keySet()) {
            WB_PolyLine polyLine = ZGeoFactory.createWB_PolyLine(skel.getRidges());
            polyLineToGenerate.add(polyLine);
        }
        splitPolyLine();
    }

    public void splitPolyLine() {
        for (WB_PolyLine pl : polyLineToGenerate) {
            splitPoints.add(ZGeoMath.splitPolyLineEdge(pl, 8));
        }
    }

    public void display(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.noStroke();
        for (List<ZPoint> splitPoint : splitPoints) {
            app.fill(0, 0, 255);
            for (ZPoint p : splitPoint) {
                p.displayAsPoint(app, 10);
            }
        }
        app.popStyle();
    }
}
