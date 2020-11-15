package formInteractive.graphAdjusting;

import formInteractive.spacialElements.Atrium;
import geometry.ZPoint;
import math.ZGeoMath;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_GeometryOp2D;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/25
 * @time 13:34
 * @description
 */
public class TrafficNodeFixed extends TrafficNode {
    private final WB_Polygon boundary;
    private List<ZPoint> joints;  // join points on boundary

    /* ------------- constructor ------------- */

    public TrafficNodeFixed(double x, double y, WB_Polygon boundary) {
        super(x, y);
        this.boundary = boundary;
    }

    public TrafficNodeFixed(WB_Point p, WB_Polygon boundary) {
        super(p);
        this.boundary = boundary;
    }

    /* ------------- set & get ------------- */

    /**
     * @return void
     * @description set location to the closest point on boundary polygon
     */
    @Override
    public void setByRestriction(double mouseX, double mouseY) {
        WB_Point point = new WB_Point(mouseX, mouseY);
        this.set(WB_GeometryOp2D.getClosestPoint2D(point, ZTransform.WB_PolygonToPolyLine(boundary)));
    }

    /**
     * @return void
     * @description set join points on each bisector, distance = regionR
     */
    @Override
    public void setJoints() {
        this.joints = new ArrayList<>();
        ZPoint[] besides = ZGeoMath.pointsOnEdgeByDist(this, boundary, getRegionR());
        joints.add(besides[1]);
        joints.add(besides[0]);
    }

    @Override
    public List<ZPoint> getJoints() {
        return this.joints;
    }

    @Override
    public String getNodeType() {
        return "TrafficNodeFixed";
    }

    @Override
    @Deprecated
    public Atrium getAtrium() {
        return null;
    }

    /* ------------- draw -------------*/

    @Override
    public void displayJoint(PApplet app, float r) {
        if (joints != null && joints.size() != 0) {
            for (ZPoint joint : joints) {
                joint.displayAsPoint(app, r);
//                app.line((float) this.x(), (float) this.y(), (float) joint.x(), (float) joint.y());
            }
        }
    }
}
