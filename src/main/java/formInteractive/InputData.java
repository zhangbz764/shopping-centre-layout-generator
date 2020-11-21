package formInteractive;

import igeo.ICurve;
import igeo.IG;
import igeo.IPoint;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 11:06
 * @description geometry from .3dm file
 */
public class InputData {
    // geometries
    private WB_Polygon inputBoundary;
    private List<WB_Point> inputEntries;
    private List<WB_Point> inputInnerNodes;

    // statistics
    private double boundaryArea;

    /* ------------- constructor ------------- */

    public InputData() {

    }

    /* ------------- loader & get (public) ------------- */

    /**
     * @return void
     * @description load geometry from .3dm file
     */
    public void loadData(String path,double scale) {
        System.out.println("** LOADING FILE **");
        IG.init();
        IG.open(path);

        // load entries
        this.inputEntries = new ArrayList<>();
        IPoint[] entries = IG.layer("entry").points();
        for (IPoint p : entries) {
            inputEntries.add(ZTransform.IPointToWB(p,scale));
        }
        // load inner nodes
        this.inputInnerNodes = new ArrayList<>();
        IPoint[] inners = IG.layer("inner").points();
        for (IPoint p : inners) {
            inputInnerNodes.add(ZTransform.IPointToWB(p,scale));
        }
        // load boundary polygon
        ICurve[] boundary = IG.layer("boundary").curves();
        this.inputBoundary = (WB_Polygon) ZTransform.ICurveToWB(boundary[0],scale);

        // print
        assert inputBoundary != null;
        this.boundaryArea = Math.abs(inputBoundary.getSignedArea());
        String inputInfo = "\n" + "*** STATISTICS ***"
                + "\n" + "boundary points " + "---> " + inputBoundary.getNumberOfPoints()
                + "\n" + "entries " + "---> " + inputEntries.size()
                + "\n" + "inner nodes " + "---> " + inputInnerNodes.size()
                + "\n" + "input boundary area " + "---> " + boundaryArea + " ㎡"
                + "\n";
        System.out.println(inputInfo);
        System.out.println("**  LOADING SUCCESS **" + "\n" + "----------------------" + "\n");
    }

    public WB_Polygon getInputBoundary() {
        return this.inputBoundary;
    }

    public List<WB_Point> getInputEntries() {
        return this.inputEntries;
    }

    public List<WB_Point> getInputInnerNodes() {
        return this.inputInnerNodes;
    }

    public double getBoundaryArea() {
        return this.boundaryArea;
    }

    /*-------- print & draw --------*/

    /**
     * @return void
     * @description draw input geometries
     */
    public void display(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.fill(255);
        app.strokeWeight(4);
        app.stroke(0);
        render.drawPolygonEdges2D(inputBoundary);
        app.popStyle();
    }
}
