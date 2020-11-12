package formInteractive.graphAdjusting;

import geometry.ZNode;
import geometry.ZPoint;
import processing.core.PApplet;
import wblut.geom.WB_Point;

import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/25
 * @time 13:25
 * @description
 */
public abstract class TrafficNode extends ZNode {
    private double regionR = 25;  // affect radius
    private boolean activate = false;

    /* ------------- constructor ------------- */

    public TrafficNode() {

    }

    public TrafficNode(double x, double y) {
        super(x, y);
    }

    public TrafficNode(WB_Point p) {
        super(p);
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

    public abstract List<ZPoint> getJoints();

    public abstract String getNodeType();

    /* ------------- draw -------------*/

    public abstract void displayJoint(PApplet app, float r);
}
