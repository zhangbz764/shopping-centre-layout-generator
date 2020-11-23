package formInteractive.graphAdjusting;

import formInteractive.spacialElements.Atrium;
import geometry.ZNode;
import geometry.ZPoint;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.processing.WB_Render3D;

import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/25
 * @time 13:25
 * @description
 */
public abstract class TrafficNode extends ZNode {
    private double regionR = 18;  // affect radius
    private boolean activate = false;
    private double[] lastPosition;

    /* ------------- constructor ------------- */

    public TrafficNode() {

    }

    public TrafficNode(double x, double y) {
        super(x, y);
        this.lastPosition = new double[]{0, 0, 0};
    }

    public TrafficNode(WB_Point p) {
        super(p);
        this.lastPosition = new double[]{0, 0, 0};
    }

    /* ------------- mouse interaction ------------- */

    /**
     * @return boolean
     * @description test whether mouse is in the node (square)
     */
    public boolean isMoused(int pointerX, int pointerY) {
        return pointerX > super.x() - super.r() && pointerX < super.x() + super.r() && pointerY > super.y() - super.r() && pointerY < super.y() + super.r();
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public boolean isActivate() {
        return activate;
    }

    /* ------------- set & get (public) ------------- */

    public void setRegionR(double r) {
        this.regionR = r;
    }

    public void updateRegionR(double r) {
        setRegionR(regionR + r);
    }

    public double getRegionR() {
        return this.regionR;
    }

    public abstract void setByRestriction(double mouseX, double mouseY);

    public abstract void setJoints();

    public void setLastPosition(double x, double y, double z) {
        this.lastPosition[0] = x;
        this.lastPosition[1] = y;
        this.lastPosition[2] = z;
    }

    public abstract void setAtrium();

    public abstract Atrium getAtrium();

    public abstract boolean hasAtrium();

    public abstract List<ZPoint> getJoints();

    public abstract String getNodeType();

    public boolean isMoved() {
        return this.x() != lastPosition[0] || this.y() != lastPosition[1] || this.z() != lastPosition[2];
    }

    /* ------------- draw -------------*/

    public abstract void displayJoint(PApplet app, float r);

    public void displayAtrium(WB_Render3D render, PApplet app) {
    }
}
