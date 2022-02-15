package main;

import igeo.ICurve;
import igeo.IG;
import igeo.IPoint;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * input geometry from .3dm file
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 11:06
 */
public class ImportData {
    // geometries
    private WB_Polygon inputSite;
    private WB_Polygon inputBoundary;
    private List<WB_Point> inputEntries;
    private List<WB_Point> inputInnerNodes;

    private WB_Polygon inputAtriumTemp;
    // statistics
    private double siteArea;

    /* ------------- constructor ------------- */

    public ImportData() {

    }

    /* ------------- loader & get (public) ------------- */

    /**
     * load geometry from .3dm file
     *
     * @param path 3dm file path
     * @return void
     */
    public void loadData(String path) {
        System.out.println("** LOADING FILE **");
        IG.init();
        IG.open(path);

        // load entries
        this.inputEntries = new ArrayList<>();
        IPoint[] entries = IG.layer("entry").points();
        for (IPoint p : entries) {
            inputEntries.add(ZTransform.IPointToWB_Point(p));
        }
        // load inner nodes
        this.inputInnerNodes = new ArrayList<>();
        IPoint[] inners = IG.layer("inner").points();
        for (IPoint p : inners) {
            inputInnerNodes.add(ZTransform.IPointToWB_Point(p));
        }
        // load site polygon
        ICurve[] site = IG.layer("site").curves();
        if (site.length > 0) {
            this.inputSite = (WB_Polygon) ZTransform.ICurveToWB(site[0]);
        }
        // load boundary polygon
        ICurve[] boundary = IG.layer("tempBoundary").curves();
        if (boundary.length > 0) {
            this.inputBoundary = (WB_Polygon) ZTransform.ICurveToWB(boundary[0]);
        }

        // temp: load public space polygon
        ICurve[] publicSpace = IG.layer("tempAtrium").curves();
        if (publicSpace.length > 0) {
            this.inputAtriumTemp = (WB_Polygon) ZTransform.ICurveToWB(publicSpace[0]);
        }

        // print
//        String inputInfo = "\n" + "*** IMPORT STATS ***"
//                + "\n" + "boundary points " + "---> " + inputBoundary.getNumberOfPoints()
//                + "\n" + "entries " + "---> " + inputEntries.size()
//                + "\n" + "inner nodes " + "---> " + inputInnerNodes.size()
//                + "\n";
//        System.out.println(inputInfo + "**  LOADING SUCCESS **" + "\n" + "----------------------" + "\n");
    }

    /**
     * load geometry from .3dm file
     *
     * @param path  3dm file path
     * @param scale scale
     * @return void
     */
    @Deprecated
    public void loadData(String path, double scale) {
        System.out.println("** LOADING FILE **");
        IG.init();
        IG.open(path);

        // load entries
        this.inputEntries = new ArrayList<>();
        IPoint[] entries = IG.layer("entry").points();
        for (IPoint p : entries) {
            inputEntries.add(ZTransform.IPointToWB_Point(p, scale));
        }
        // load inner nodes
        this.inputInnerNodes = new ArrayList<>();
        IPoint[] inners = IG.layer("inner").points();
        for (IPoint p : inners) {
            inputInnerNodes.add(ZTransform.IPointToWB_Point(p, scale));
        }
        // load boundary polygon
        ICurve[] boundary = IG.layer("boundary").curves();
        this.inputBoundary = (WB_Polygon) ZTransform.ICurveToWB(boundary[0], scale);

        // print
        String inputInfo = "\n" + "*** IMPORT STATS ***"
                + "\n" + "boundary points " + "---> " + inputBoundary.getNumberOfPoints()
                + "\n" + "entries " + "---> " + inputEntries.size()
                + "\n" + "inner nodes " + "---> " + inputInnerNodes.size()
                + "\n";
        System.out.println(inputInfo + "**  LOADING SUCCESS **" + "\n" + "----------------------" + "\n");
    }

    public WB_Polygon getInputSite() {
        return inputSite;
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

    public WB_Polygon getInputAtriumTemp() {
        return inputAtriumTemp;
    }

    /*-------- print & draw --------*/

    /**
     * @return void
     * @description draw input geometries
     */
    public void display(WB_Render render, PApplet app) {
        app.pushStyle();
        app.fill(255);
        app.strokeWeight(4);
        app.stroke(0);
        render.drawPolygonEdges2D(inputBoundary);
        app.popStyle();
    }
}
