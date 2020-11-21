package formInteractive.spacialElements;

import formInteractive.graphAdjusting.TrafficNodeTree;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 10:00
 * @description
 */
public class Atrium {
    private TrafficNodeTree treeNode;
    private WB_Polygon polygon;

    private Escalator escalator;

    /* ------------- constructor ------------- */

    public Atrium(TrafficNodeTree treeNode, WB_Polygon polygon) {
        this.treeNode = treeNode;
        this.polygon = polygon;
    }

    /* ------------- set & get (public) ------------- */

    public void setTreeNode(TrafficNodeTree treeNode) {
        this.treeNode = treeNode;
    }

    public void setEscalator(Escalator escalator) {
        this.escalator = escalator;
    }

    public void setPolygon(WB_Polygon polygon) {
        this.polygon = polygon;
    }

    public TrafficNodeTree getTreeNode() {
        return treeNode;
    }

    public Escalator getEscalator() {
        return escalator;
    }

    public WB_Polygon getPolygon() {
        return polygon;
    }

    /* ------------- draw ------------- */

    public void display(WB_Render3D render) {
        render.drawPolygonEdges2D(polygon);
    }
}
