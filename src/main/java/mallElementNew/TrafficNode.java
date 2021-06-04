package mallElementNew;

import basicGeometry.ZNode;
import basicGeometry.ZPoint;
import oldVersion.mallElements.Atrium;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.processing.WB_Render;

import java.util.List;

/**
 * an abstract class of control node in the plan of shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/25
 * @time 13:25
 */
public abstract class TrafficNode extends ZNode {
    private static double originalRegionR = 5;
    private double regionR = originalRegionR;  // affect radius
    private boolean active = false;
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
     * test whether mouse is in the node (square)
     *
     * @param pointerX x
     * @param pointerY y
     * @return boolean
     */
    public boolean isMoused(int pointerX, int pointerY) {
        return pointerX > super.xd() - super.rd() && pointerX < super.xd() + super.rd() && pointerY > super.yd() - super.rd() && pointerY < super.yd() + super.rd();
    }

    /**
     * set active status of this node
     *
     * @param active boolean
     * @return void
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    // only for tree node

    /**
     * set atrium active status of this node
     *
     * @param active boolean
     * @return void
     */
    public void setAtriumActive(boolean active) {

    }

    public abstract boolean isAtriumActive();

    /**
     * switch control point of atrium
     *
     * @param
     * @return void
     */
    public void switchAtriumControl() {

    }

    /**
     * update atrium length along linked edge
     *
     * @param delta length delta
     * @return void
     */
    public void updateAtriumLength(double delta) {
    }

    /**
     * update atrium width perpendicular to linked edge
     *
     * @param delta width delta
     * @return void
     */
    public void updateAtriumWidth(double delta) {
    }

    /* ------------- set & get (public) ------------- */

    public static void setOriginalRegionR(double originalRegionR) {
        TrafficNode.originalRegionR = originalRegionR;
    }

    /**
     * set control radius of node
     *
     * @param r radius
     * @return void
     */
    public void setRegionR(double r) {
        this.regionR = r;
    }

    /**
     * update control radius of node
     *
     * @param r radius delta
     * @return void
     */
    public void updateRegionR(double r) {
        setRegionR(regionR + r);
    }

    public double getRegionR() {
        return this.regionR;
    }

    /**
     * set the node by restriction
     * tree node should be within the boundary polygon
     * fixed node should be on the boundary edges
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public abstract void setByRestriction(double pointerX, double pointerY);

    /**
     * set joints to split boundary
     *
     * @param
     * @return void
     */
    public abstract void setJoints();

    /**
     * record last position
     *
     * @param x x
     * @param y y
     * @param z z
     * @return void
     */
    public void setLastPosition(double x, double y, double z) {
        this.lastPosition[0] = x;
        this.lastPosition[1] = y;
        this.lastPosition[2] = z;
    }

    public abstract void setAtrium();

    public abstract void clearAtrium();

    public abstract Atrium getAtrium();

    public abstract boolean hasAtrium();

    public abstract List<ZPoint> getJoints();

    public abstract String getNodeType();

    @Deprecated
    public boolean isMoved() {
        return this.xd() != lastPosition[0] || this.yd() != lastPosition[1] || this.zd() != lastPosition[2];
    }

    /* ------------- draw -------------*/

    public abstract void displayJoint(PApplet app, float r);

    public void displayAtrium(WB_Render render, PApplet app) {
    }
}
