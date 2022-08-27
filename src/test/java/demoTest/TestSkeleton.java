package demoTest;

import advancedGeometry.ZSkeleton;
import basicGeometry.ZFactory;
import guo_cam.CameraController;
import igeo.ICurve;
import igeo.IG;
import igeo.IMesh;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Mesh;
import wblut.processing.WB_Render;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/8/12
 * @time 21:03
 */
public class TestSkeleton extends PApplet {
    public static void main(String[] args) {
        PApplet.main("demoTest.TestSkeleton");
    }

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */
    WB_Polygon inputBoundary;
    ZSkeleton skeleton;
    HE_Mesh skeletonMesh;

    CameraController gcam;
    WB_Render render;

    HE_Mesh mesh;

    HE_Mesh polyMesh;

    public void setup() {
        this.gcam = new CameraController(this);
        this.render = new WB_Render(this);

        IG.init();
        IG.open("./src/main/resources/20220406.3dm");
        // load boundary
        ICurve[] boundary = IG.layer("testBoundary").curves();
        if (boundary.length > 0) {
            this.inputBoundary = (WB_Polygon) ZTransform.ICurveToWB(boundary[0]);
        }

        this.skeleton = new ZSkeleton(inputBoundary);
        System.out.println(skeleton.getAllEdges().size());

        this.skeletonMesh = skeleton.getSkeletonMesh();
        System.out.println(skeletonMesh.getNumberOfVertices());
        System.out.println(skeletonMesh.getNumberOfFaces());
        System.out.println(skeletonMesh.getNumberOfEdges());

        IG.init();
        IG.open("E:\\0_code\\Ztools\\src\\test\\resources\\test_mesh_trans.3dm");

        IMesh[] meshes = IG.layer("mesh").meshes();
        this.mesh = ZTransform.IMeshToHE_Mesh(meshes[0]);


        Polygon p1 = ZFactory.jtsgf.createPolygon(
                new Coordinate[]{
                        new Coordinate(0, 0),
                        new Coordinate(100, 0),
                        new Coordinate(100, 100),
                        new Coordinate(0, 100),
                        new Coordinate(0, 0),
                }
        );
        Polygon p2 = ZFactory.jtsgf.createPolygon(
                new Coordinate[]{
                        new Coordinate(100, 0),
                        new Coordinate(200, 0),
                        new Coordinate(200, 100),
                        new Coordinate(100, 100),
                        new Coordinate(100, 0),
                }
        );
        WB_Polygon pl1 = ZTransform.PolygonToWB_Polygon(p1);
        WB_Polygon pl2 = ZTransform.PolygonToWB_Polygon(p2);
        System.out.println(pl1.isSimple());
        System.out.println(pl1.getNumberOfHoles());
        this.polyMesh = new HEC_FromPolygons(
                new WB_Polygon[]{
                        pl1,
                        pl2
                }).create();
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        skeleton.display(this);


        // draw mesh
        strokeWeight(1);
        stroke(0);
        render.drawEdges(skeletonMesh);
        fill(200, 150);
        noStroke();
        render.drawFaces(skeletonMesh);

        // draw mesh
        strokeWeight(1);
        stroke(0);
        render.drawEdges(mesh);
        fill(200, 150);
        noStroke();
        render.drawFaces(mesh);

        // draw mesh
        strokeWeight(1);
        stroke(0);
        render.drawEdges(polyMesh);
        fill(200, 150);
        noStroke();
        render.drawFaces(polyMesh);
    }

}
