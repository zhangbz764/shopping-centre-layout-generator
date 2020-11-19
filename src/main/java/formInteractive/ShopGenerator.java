package formInteractive;

import geometry.ZGeoFactory;
import geometry.ZLine;
import geometry.ZPoint;
import geometry.ZSkeleton;
import math.ZGeoMath;
import processing.core.PApplet;
import wblut.geom.*;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/8
 * @time 21:35
 * @description
 */

// TODO: 2020/11/8 好多代码结构和健壮性问题
public class ShopGenerator {
    List<WB_PolyLine> polyLineToGenerate;
    List<List<ZPoint>> splitPoints;
    List<WB_Voronoi2D> voronois;
    List<HE_Mesh> meshes;

    WB_Polygon publicBlock;
    HE_Mesh newMeshTest;

    /* ------------- constructor ------------- */

    public ShopGenerator() {

    }

    public ShopGenerator(List<WB_Polygon> shopBlock, WB_Polygon publicBlock, List<ZSkeleton> skeletons) {
        assert shopBlock.size() == skeletons.size();
        this.polyLineToGenerate = new ArrayList<>();
        this.splitPoints = new ArrayList<>();
        this.voronois = new ArrayList<>();

        for (int i = 0; i < skeletons.size(); i++) {
            // maybe null
            List<ZLine> centerSegments = skeletons.get(i).getRidges();
            centerSegments.addAll(skeletons.get(i).getExtendedRidges());
            WB_PolyLine polyLine = ZGeoFactory.createWB_PolyLine(centerSegments);
            if (polyLine != null) {
                polyLineToGenerate.add(polyLine);

                List<ZPoint> splitResult = ZGeoMath.splitWB_PolyLineEdgeByThreshold(polyLine, 17, 16);
                if (splitResult.size() != 0) {
                    splitResult.remove(splitResult.size() - 1);
                    splitResult.remove(0);
                }
                splitPoints.add(splitResult);

                List<WB_Point> points = new ArrayList<>();
                for (ZPoint p : splitResult) {
                    points.add(p.toWB_Point());
                }
                WB_Voronoi2D voronoi = WB_VoronoiCreator.getClippedVoronoi2D(points, shopBlock.get(i));
                voronois.add(voronoi);
            }
        }
        // convert to mesh
        this.meshes = new ArrayList<>();
        List<WB_Polygon> cellPolygons = new ArrayList<>();
        for (WB_Voronoi2D voronoi : voronois) {
            for (WB_VoronoiCell2D cell : voronoi.getCells()) {
                cellPolygons.add(cell.getPolygon());
            }
            meshes.add(new HEC_FromPolygons(cellPolygons).create());
        }

        this.newMeshTest = meshes.get(0);
        this.publicBlock = publicBlock;
//        this.newMeshTest = optimizeShape(newMeshTest, publicBlock);
    }

    /**
     * @return boolean
     * @description test if mesh need to optimize
     */
    private boolean testMesh(HE_Mesh mesh, WB_Polygon publicBlock) {
        boolean result = false;
        for (HE_Face face : mesh.getFaces()) {
            for (HE_Vertex vertex : face.getFaceVertices()) {
                // run if any face's vertex isn't boundary
                result = !WB_GeometryOp2D.contains2D(vertex, publicBlock);
            }
            if (result) {
                return true;
            }
        }
        return false;
    }

    public void dede() {
        optimizeShape(newMeshTest, publicBlock);
    }

    /**
     * @return void
     * @description
     */
    private HE_Mesh optimizeShape(HE_Mesh mesh, WB_Polygon publicBlock) {
//        HE_Face problemFace = null;
//        HE_Face neighborProblemFace = null;
////            Set<HE_Face> neighbor = new HashSet<>();
//
//
//        for (HE_Face face : mesh.getFaces()) {
//            boolean result = true;
//            for (HE_Vertex vertex : face.getFaceVertices()) {
//                // run if any face's vertex isn't boundary
//                result = result && !WB_GeometryOp2D.contains2D(vertex, publicBlock);
//            }
//            if (result) {
//                // remove faces
//                problemFace = face;
//                System.out.println(face.getNeighborFaces().size());
//                neighborProblemFace = face.getNeighborFaces().get(0);
//
//                mesh.remove(problemFace);
//                mesh.remove(neighborProblemFace);
//                break;
//            }
//        }
//
//        // union problem faces, create new mesh
//        assert problemFace != null;
//        List<WB_Polygon> union = ZGeoFactory.wbgf.unionPolygons2D(problemFace.getPolygon(), neighborProblemFace.getPolygon());
//        assert union.size() == 1;
//        List<WB_Polygon> newPolygons = mesh.getPolygonList();
//        newPolygons.add(union.get(0));
//
//        mesh = new HEC_FromPolygons(newPolygons).create();
//        this.newMeshTest = mesh;
        if (testMesh(mesh, publicBlock)) {
            HE_Face problemFace = null;
            HE_Face neighborProblemFace = null;
//            Set<HE_Face> neighbor = new HashSet<>();

            boolean result = false;
            for (HE_Face face : mesh.getFaces()) {
                for (HE_Vertex vertex : face.getFaceVertices()) {
                    // run if any face's vertex isn't boundary
                    result = !WB_GeometryOp2D.contains2D(vertex, publicBlock);
                }
                if (result) {
                    // remove faces
                    problemFace = face;
                    System.out.println(face.getNeighborFaces().size());
                    neighborProblemFace = face.getNeighborFaces().get(0);


                    mesh.remove(problemFace);
                    mesh.remove(neighborProblemFace);
                    break;
                }
            }

            // union problem faces, create new mesh
            assert problemFace != null;
            List<WB_Polygon> union = ZGeoFactory.wbgf.unionPolygons2D(problemFace.getPolygon(), neighborProblemFace.getPolygon());
            assert union.size() == 1;
            List<WB_Polygon> newPolygons = mesh.getPolygonList();
            newPolygons.add(union.get(0));

            mesh = new HEC_FromPolygons(newPolygons).create();
            mesh = optimizeShape(mesh, publicBlock);
        }
        return mesh;
    }

    public void display(WB_Render3D render, PApplet app) {
        app.pushStyle();
        // draw voronoi
        app.fill(128);
        app.stroke(0);
        app.strokeWeight(3);
        for (HE_Mesh mesh : meshes) {
            render.drawEdges(mesh);
        }

        if (newMeshTest != null) {
            app.pushMatrix();
            app.translate(0, 500);
            render.drawEdges(newMeshTest);
            app.translate(0, 500);
            app.popMatrix();
        }

        // draw point
//        app.noStroke();
//        app.fill(0, 0, 255);
//        for (List<ZPoint> splitPoint : splitPoints) {
//            for (ZPoint p : splitPoint) {
//                p.displayAsPoint(app, 10);
//            }
//        }
        app.popStyle();
    }
}
