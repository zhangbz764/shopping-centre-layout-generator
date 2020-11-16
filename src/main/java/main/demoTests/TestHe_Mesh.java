package main.demoTests;

import Guo_Cam.CameraController;
import geometry.ZPoint;
import org.locationtech.jts.geom.Polygon;
import processing.core.PApplet;
import transform.ZTransform;
import wblut.geom.WB_Point;
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Ray;
import wblut.hemesh.*;
import wblut.processing.WB_Render;

import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/7
 * @time 20:00
 * @description
 */
public class TestHe_Mesh extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    WB_Polygon poly1;
    WB_Polygon poly2;
    WB_Polygon poly3;
    WB_Polygon poly4;

    HE_Mesh mesh;

    WB_Render render;
    CameraController gcam;

    /* ------------- setup ------------- */

    private void setPolys() {
        WB_Point[] pts1 = new WB_Point[5];
        pts1[0] = new WB_Point(100, 100);
        pts1[1] = new WB_Point(700, 100);
        pts1[2] = new WB_Point(800, 400);
        pts1[3] = new WB_Point(200, 400);
        pts1[4] = new WB_Point(100, 100);
        poly1 = new WB_Polygon(pts1);

        WB_Point[] pts2 = new WB_Point[5];
        pts2[0] = new WB_Point(100, 100);
        pts2[1] = new WB_Point(100, -100);
        pts2[2] = new WB_Point(700, -100);
        pts2[3] = new WB_Point(700, 100);
        pts2[4] = new WB_Point(100, 100);
        poly2 = new WB_Polygon(pts2);

        WB_Point[] pts3 = new WB_Point[6];
        pts3[0] = new WB_Point(700, 100);
        pts3[1] = new WB_Point(700, -100);
        pts3[2] = new WB_Point(1000, -100);
        pts3[3] = new WB_Point(1200, 0);
        pts3[4] = new WB_Point(1000, 100);
        pts3[5] = new WB_Point(700, 100);
        poly3 = new WB_Polygon(pts3);

        WB_Point[] pts4 = new WB_Point[6];
        pts4[0] = new WB_Point(800, 400);
        pts4[1] = new WB_Point(700, 100);
        pts4[2] = new WB_Point(1000, 100);
        pts4[3] = new WB_Point(1200, 0);
        pts4[4] = new WB_Point(1200, 400);
        pts4[5] = new WB_Point(800, 400);
        poly4 = new WB_Polygon(pts4);
    }

    WB_Polygon polygon;

    public void setup() {
        gcam = new CameraController(this);
        render = new WB_Render(this);
        setPolys();

        WB_Polygon[] polys = new WB_Polygon[]{poly1, poly2, poly3, poly4};
        mesh = new HEC_FromPolygons(polys).create();

        System.out.println(mesh.getVertices().size());
        System.out.println(mesh.getAllBoundaryVertices().size());


        // *******************
        WB_Point[] pts1 = new WB_Point[5];
        WB_Point[] pts2 = new WB_Point[5];
        pts2[0] = pts1[0] = new WB_Point(100, 100);
        pts2[1] = pts1[1] = new WB_Point(700, 100);
        pts2[2] = pts1[2] = new WB_Point(800, 400);
        pts2[3] = pts1[3] = new WB_Point(500, 800);
        pts2[4] = pts1[4] = new WB_Point(100, 600);
//        pts1[5] = new WB_Point(100, 100);
        polygon = new WB_Polygon(pts1);
        println(polygon.getNumberOfPoints());
        println(polygon.getNumberSegments());
    }

    private void optimize() {
        HE_Face f1 = mesh.getFaces().get(0);
        HE_Face f2 = mesh.getFaces().get(0).getNeighborFaces().get(0);
        Polygon p1 = ZTransform.WB_PolygonToJtsPolygon(f1.getPolygon());
        Polygon p2 = ZTransform.WB_PolygonToJtsPolygon(f2.getPolygon());

        mesh.remove(f1);
        mesh.remove(f2);
        WB_Polygon union = ZTransform.JtsPolygonToWB_Polygon((Polygon) p1.union(p2));

        List<WB_Polygon> newPolygons = mesh.getPolygonList();
        newPolygons.add(union);
        mesh = new HEC_FromPolygons(newPolygons).create();
    }

    /* ------------- draw ------------- */


    public void draw() {
        background(255);
        box(20, 20, 20);
        strokeWeight(1);
        for (int i = 0; i < mesh.getEdges().size(); i++) {
            render.drawEdge(mesh.getEdges().get(i));
        }
        fill(0);
        for (int j = 0; j < mesh.getFaces().size(); j++) {
            text(j, mesh.getFaceWithIndex(j).getFaceCenter().xf(), mesh.getFaceWithIndex(j).getFaceCenter().yf());
        }
//        for (HE_Face nei : mesh.getFaceWithIndex(3).getNeighborFaces()) {
//            render.drawFace(nei);
//        }
        strokeWeight(5);
        for (int k = 0; k < mesh.getNumberOfVertices(); k++) {
            if (!mesh.getVertexWithIndex(k).isBoundary()) {
                ellipse(mesh.getVertexWithIndex(k).xf(), mesh.getVertexWithIndex(k).yf(), 100, 100);
                for (HE_Vertex vertex : mesh.getVertexWithIndex(k).getNeighborVertices()) {
                    if (!vertex.isBoundary()) {
                        line(mesh.getVertexWithIndex(k).xf(), mesh.getVertexWithIndex(k).yf(), vertex.xf(), vertex.yf());
                    }
                }
            }
        }
        WB_Ray ray = new WB_Ray(new WB_Point(800, 800), new WB_Point(100, 0));
        render.drawRay(ray, 1000);

        translate(0, 1000);
        noFill();
        render.drawPolygonEdges2D(polygon);
    }

    public void keyPressed() {
        optimize();
    }

}
