package formInteractive.graphAdjusting;

import geometry.ZPoint;
import math.ZGeoMath;
import processing.core.PApplet;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/21
 * @time 10:24
 * @description
 */
public class TrafficNodeTree extends TrafficNode {
    private final WB_Polygon boundary;
    private List<ZPoint> bisectors;  // angular bisectors from this node
    private List<ZPoint> joints;  // join points on bisectors

    /* ------------- constructor ------------- */

    public TrafficNodeTree(double x, double y, WB_Polygon boundary) {
        super(x, y);
        this.boundary = boundary;
    }

    public TrafficNodeTree(WB_Point p, WB_Polygon boundary) {
        super(p);
        this.boundary = boundary;
    }

    /* ------------- set & get ------------- */

    @Override
    public void setByRestriction(double mouseX, double mouseY) {
        WB_Point point = new WB_Point(mouseX, mouseY);
        if (WB_GeometryOp.contains2D(point, boundary) && WB_GeometryOp.getDistance2D(point, boundary) > this.getRegionR()) {
            this.set(mouseX, mouseY);
        }
    }

    /**
     * @return void
     * @description set join points on each bisector, distance = regionR
     */
    @Override
    public void setJoints() {
        findBisectors();
        if (this.bisectors != null) {
            this.joints = new ArrayList<>();
            for (ZPoint bis : bisectors) {
                joints.add(this.add(bis.scaleTo(super.getRegionR())));
            }
        }
    }

    @Override
    public List<ZPoint> getJoints() {
        return this.joints;
    }

    @Override
    public String getNodeType() {
        return "TrafficNodeTree";
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

    /* ------------- compute bisector -------------*/

    /**
     * @return void
     * @description set all bisectors start from this node (must set relations first)
     */
    private void findBisectors() {
        if (this.getNeighbor() != null && this.getNeighbor().size() != 0) {
            this.bisectors = new ArrayList<>();
            if (this.getNeighbor().size() == 1) {  // only has 1 neighbor, make its bisectors to an square end
                ZPoint reverse = this.sub(this.getNeighbor().get(0)).unit();
                bisectors.add(reverse.rotate2D(Math.PI / 4));
                bisectors.add(reverse.rotate2D(Math.PI / -4));
            } else {  // 2 or more neighbors, re-order vectors and get each angular bisector
                List<ZPoint> vecToNeighbor = new ArrayList<>();
                for (ZPoint nei : this.getNeighbor()) {
                    vecToNeighbor.add(nei.sub(this));
                }
                ZPoint[] order = ZGeoMath.sortPolarAngle(vecToNeighbor);
                for (int i = 0; i < order.length; i++) {
                    bisectors.add(ZGeoMath.getAngleBisectorOrdered(order[i], order[(i + 1) % order.length]));
                }
            }
        }
    }


}
