package mallIO;

import igeo.ICurve;
import igeo.IG;
import igeo.IPoint;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<Geometry> inputSite;
    private WB_Polygon inputBlock;
    private WB_Polygon inputBoundary;
    private List<WB_Point> inputEntries;
    private List<WB_Point> inputInnerNodes;

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
            this.inputBlock = (WB_Polygon) ZTransform.ICurveToWB(site[0]);
        }


//        // load boundary polygon
//        ICurve[] boundary = IG.layer("tempBoundary").curves();
//        if (boundary.length > 0) {
//            this.inputBoundary = (WB_Polygon) ZTransform.ICurveToWB(boundary[0]);
//        }
    }

    /**
     * load Shaoxing site
     *
     * @param path input file path
     * @return void
     */
    public void loadDataShaoxing(String path) {
        System.out.println("** LOADING FILE **");
        IG.init();
        IG.open(path);

        // load site
        ICurve[] site = IG.layer("testSite").curves();
        if (site.length > 0) {
            for (ICurve c : site) {
                this.inputSite.add(ZTransform.ICurveToJts(c));
            }
        }

        // load red line
        ICurve[] redLine = IG.layer("testRedLine").curves();
        if (redLine.length > 0) {
            this.inputBlock = (WB_Polygon) ZTransform.ICurveToWB(redLine[0]);
        }

        // load boundary
        ICurve[] boundary = IG.layer("testBoundary").curves();
        if (boundary.length > 0) {
            this.inputBoundary = (WB_Polygon) ZTransform.ICurveToWB(boundary[0]);
        }
    }

    public void loadDataSite(String path){
        System.out.println("** LOADING FILE **");
        IG.init();
        IG.open(path);

        // load red line
        ICurve[] redLine = IG.layer("testRedLine").curves();
        if (redLine.length > 0) {
            this.inputBlock = (WB_Polygon) ZTransform.ICurveToWB(redLine[0]);
        }
        // load boundary
        ICurve[] boundary = IG.layer("site5").curves();
        System.out.println(boundary.length);
        if (boundary.length > 0) {
            this.inputBoundary = (WB_Polygon) ZTransform.ICurveToWB(boundary[0]);
        }
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

    public List<Geometry> getInputSite() {
        return inputSite;
    }

    public WB_Polygon getInputBlock() {
        return inputBlock;
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
