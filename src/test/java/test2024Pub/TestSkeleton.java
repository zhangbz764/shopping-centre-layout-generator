package test2024Pub;

import advancedGeometry.ZBSpline;
import advancedGeometry.ZSkeleton;
import basicGeometry.ZEdge;
import basicGeometry.ZFactory;
import basicGeometry.ZGraph;
import basicGeometry.ZLine;
import guo_cam.CameraController;
import igeo.ICurve;
import igeo.IG;
import mallElementNew.MainTraffic;
import math.ZGraphMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Polygon;

import java.util.List;

/**
 * description
 *
 * @author Baizhou Zhang zhangbz
 * @project shopping_mall
 * @date 2024/4/5
 * @time 14:30
 */
public class TestSkeleton extends PApplet {
    public static void main(String[] args) {
        PApplet.main(TestSkeleton.class.getName());
    }

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
//        size(1280, 720, SVG, ".\\src\\test\\resources\\010203skel.svg");
    }

    /* ------------- setup ------------- */

    private Polygon boundary;
    private ZSkeleton skeleton;
    private List<ZLine> tops;
    private List<ZLine> alls;
    private LineString centralLs;
    private LineString centralCurve;
    private Geometry centralCurveBuffer;

    private JtsRender jtsRender;
    private CameraController gcam;

    public void setup() {
        this.jtsRender = new JtsRender(this);
        this.gcam = new CameraController(this);
        gcam.top();


        String path = "./src/main/resources/20220406.3dm";
        IG.init();
        IG.open(path);

        ICurve[] boundaries = IG.layer("testBoundary").curves();
        this.boundary = (Polygon) ZTransform.ICurveToJts(boundaries[0]);

//        this.boundary = new WB_Polygon(new WB_Point[]{
//                new WB_Point(0, 100),
//                new WB_Point(-250, 0),
//                new WB_Point(-270, -150),
//                new WB_Point(0, -70),
//                new WB_Point(250, -160),
//                new WB_Point(400, -40),
//                new WB_Point(450, 70),
//                new WB_Point(0, 100),
//        });

        this.skeleton = new ZSkeleton(boundary);
        tops = skeleton.getTopEdges();
        alls = skeleton.getAllEdges();

        List<ZLine> centralSegs = skeleton.getRidges();
        ZGraph ridgeGraph = ZFactory.createZGraphFromSegments(centralSegs);
        ridgeGraph.checkPath();
        ridgeGraph.checkLoop();

        // check if the ridge graph has loops or is a path
        if (!ridgeGraph.isLoop()) {
            if (ridgeGraph.isPath()) {
                // single path
                centralLs = ZFactory.createLineString(centralSegs);
            } else {
                // has forks, find the longest chain
                List<ZEdge> longestChain = ZGraphMath.longestChain(ridgeGraph);
                centralLs = ZFactory.createLineString(longestChain);
            }
        }
        Coordinate[] coords = centralLs.getCoordinates();
        this.centralCurve = new ZBSpline(coords, 3, 50, ZBSpline.CLAMPED).getAsLineString();
        this.centralCurveBuffer = centralCurve.buffer(8);
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
//        skeleton.display(this);

        stroke(0);
        strokeWeight(4);
        jtsRender.drawGeometry(boundary);

        stroke(0, 0, 255);
        strokeWeight(1);
        for (ZLine top : alls) {
            line(top.getPt0().xf(), top.getPt0().yf(), top.getPt1().xf(), top.getPt1().yf());
        }
        strokeWeight(4);
        for (ZLine top : tops) {
            line(top.getPt0().xf(), top.getPt0().yf(), top.getPt1().xf(), top.getPt1().yf());
        }

        translate(500,0);
        stroke(0);
        strokeWeight(4);
        jtsRender.drawGeometry(boundary);
        strokeWeight(1);
        stroke(0, 0, 255);
        jtsRender.drawGeometry(centralLs);

        translate(500,0);
        stroke(0);
        strokeWeight(4);
        jtsRender.drawGeometry(boundary);
        strokeWeight(1);
        stroke(0, 0, 255);
        jtsRender.drawGeometry(centralCurve);
        jtsRender.drawGeometry(centralCurve.buffer(9));
    }

    public void keyPressed() {
        exit();
    }

}
