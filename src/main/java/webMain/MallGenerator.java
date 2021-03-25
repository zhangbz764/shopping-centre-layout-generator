package webMain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import converter.JTS_Converter;
import converter.WB_Converter;
import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.TrafficNodeFixed;
import formInteractive.graphAdjusting.TrafficNodeTree;
import geometry.*;
import main.ArchiJSON;
import main.ImportData;
import math.ZGeoMath;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.buffer.BufferOp;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Segment;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project archijson
 * @date 2021/3/10
 * @time 17:55
 */
public class MallGenerator {
    private boolean draw = false; // display switch

    private final ImportData input = new ImportData(); // (temp) input data from backend
    public List<WB_Point> innerNode_receive;
    public List<WB_Polygon> polyAtrium_receive;

    private TrafficGraph graph;
    private List<LineString> bufferLineStrings;
    private Geometry buffer;
    private List<List<WB_Coord>> bufferControlPoints;

    /* ------------- constructor ------------- */

    public MallGenerator() {

    }

    public void init() {
        String path = "./src/main/resources/0310.3dm";
        input.loadData(path, 1);
    }

    /* ------------- member function ------------- */

    /**
     * generate TrafficGraph
     *
     * @return void
     */
    public void generateGraph() {
        List<TrafficNode> innerNodes = new ArrayList<>();
        for (WB_Point p : innerNode_receive) {
            TrafficNodeTree treeNode = new TrafficNodeTree(p);
            treeNode.setBoundary(input.getInputBoundary());
            innerNodes.add(treeNode);
        }
        List<TrafficNode> entryNodes = new ArrayList<>();
        for (WB_Point p : this.input.getInputEntries()) {
            TrafficNode fixedNode = new TrafficNodeFixed(p, this.input.getInputBoundary());
            entryNodes.add(fixedNode);
        }
        this.graph = new TrafficGraph(innerNodes, entryNodes);

        draw = true;
    }

    /**
     * generate buffer geometries of public space
     *
     * @param dist buffer distance
     * @return void
     */
    public void generateBuffer(double dist) {
        List<Geometry> geos = new ArrayList<>(getLineStrings());
//        System.out.println(polyAtrium_receive);
        if (polyAtrium_receive != null && polyAtrium_receive.size() > 0) {
            for (WB_Polygon p : polyAtrium_receive) {
                geos.add(ZTransform.WB_PolygonToJtsPolygon(p));
            }
        }
        Geometry[] geometries = geos.toArray(new Geometry[0]);
        GeometryCollection collection = ZGeoFactory.jtsgf.createGeometryCollection(geometries);
        Geometry buffer = collection.buffer(dist);
//        BufferOp bop = new BufferOp(collection);
//        bop.setEndCapStyle(BufferParameters.CAP_SQUARE);
//        Geometry buffer = bop.getResultGeometry(dist);


//        if (buffer instanceof Polygon) {
//            LineString bufferLS = ZTransform.PolygonToLineString((Polygon) buffer).get(0);
//            Polygon boundary = ZTransform.WB_PolygonToJtsPolygon(input.getInputBoundary());
//            Geometry intersection = bufferLS.intersection(boundary);
//            if (intersection instanceof MultiLineString) {
//                for (int i = 0; i < intersection.getNumGeometries(); i++) {
//                    bufferLineStrings.add((LineString) intersection.getGeometryN(i));
//                }
//            } else {
//                System.out.println("not MultiLineString");
//            }
//        }

        this.bufferControlPoints = new ArrayList<>();
        if (buffer instanceof Polygon) {
            LineString bufferLS = ZTransform.PolygonToLineString((Polygon) buffer).get(0);
            Polygon boundary = ZTransform.WB_PolygonToJtsPolygon(input.getInputBoundary());
            Geometry intersection = bufferLS.intersection(boundary);
            this.buffer = intersection;
            if (intersection instanceof MultiLineString) {
                for (int i = 0; i < intersection.getNumGeometries(); i++) {
                    List<ZPoint> splitPoints = ZGeoMath.splitLineString((LineString) intersection.getGeometryN(i), 6);
                    List<WB_Coord> splitPointsEach = new ArrayList<>();
                    for (ZPoint p : splitPoints) {
                        splitPointsEach.add(p.toWB_Point());
                    }
                    bufferControlPoints.add(splitPointsEach);
                }
            } else {
                System.out.println("not MultiLineString");
            }
        }
    }

    /**
     * convert backend geometries to ArchiJSON
     *
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSON(String clientID, Gson gson) {
        // preparing data
        List<WB_Point> fixedNodes = getFixedNode();
        List<WB_Segment> graphSegments = getSegments();
        WB_Polygon boundary = input.getInputBoundary();

        // initializing
        ArchiJSON json = new ArchiJSON();
        json.setId(clientID);

        // converting to json
        List<JsonElement> elements = new ArrayList<>();

//        Vertices vertices = WB_Converter.toVertices(fixedNodes);
//        elements.add(gson.toJsonTree(vertices));
        for (WB_Segment seg : graphSegments) {
            Segments segments = WB_Converter.toSegments(seg);
            JsonObject prop1 = new JsonObject();
            prop1.addProperty("name", "tree");
            segments.setProperties(prop1);
            elements.add(gson.toJsonTree(segments));
        }
        Segments poly = WB_Converter.toSegments(boundary);
        JsonObject prop2 = new JsonObject();
        prop2.addProperty("name", "boundary");
        poly.setProperties(prop2);
        elements.add(gson.toJsonTree(poly));

//        for (LineString ls : bufferLineStrings) {
//            Segments bufferPoly = JTS_Converter.toSegments(ls);
//            JsonObject prop3 = new JsonObject();
//            prop3.addProperty("name", "buffer");
//            bufferPoly.setProperties(prop3);
//            elements.add(gson.toJsonTree(bufferPoly));
//        }

        for (List<WB_Coord> splitPointsEach : bufferControlPoints) {
            Vertices bufferControlPointsEach = WB_Converter.toVertices(splitPointsEach, 3);
            JsonObject prop3 = new JsonObject();
            prop3.addProperty("name", "bufferControl");
            bufferControlPointsEach.setProperties(prop3);
            elements.add(gson.toJsonTree(bufferControlPointsEach));
        }

        json.setGeometryElements(elements);

        return json;
    }

    /* ------------- setter & getter ------------- */

    public void setInnerNode_receive(List<WB_Point> innerNode_receive) {
        this.innerNode_receive = innerNode_receive;
    }

    public void setPolyAtrium_receive(List<WB_Polygon> polyAtrium_receive) {
        this.polyAtrium_receive = polyAtrium_receive;
    }

    /**
     * fixed node in traffic graph
     *
     * @return java.util.List<wblut.geom.WB_Point>
     */
    public List<WB_Point> getFixedNode() {
        List<WB_Point> pts = new ArrayList<>();
        for (TrafficNode n : graph.getFixedNodes()) {
            pts.add(n.toWB_Point());
        }
        return pts;
    }

    /**
     * convert traffic edge to WB_Segment
     *
     * @return java.util.List<wblut.geom.WB_Segment>
     */
    public List<WB_Segment> getSegments() {
        List<WB_Segment> segments = new ArrayList<>();
        for (ZEdge e : graph.getTreeEdges()) {
            segments.add(e.toWB_Segment());
        }
        for (ZEdge e : graph.getFixedEdges()) {
            segments.add(e.toWB_Segment());
        }
        return segments;
    }

    /**
     * convert traffic edge to LineString
     *
     * @return java.util.List<org.locationtech.jts.geom.LineString>
     */
    public List<LineString> getLineStrings() {
        List<LineString> lineStrings = new ArrayList<>();
        for (ZEdge e : graph.getTreeEdges()) {
            lineStrings.add(e.toJtsLineString());
        }
        for (ZEdge e : graph.getFixedEdges()) {
            lineStrings.add(e.toJtsLineString());
        }
        return lineStrings;
    }

    /* ------------- draw ------------- */

    public void draw(PApplet app) {
        if (draw) {
            WB_Render3D render = new WB_Render3D(app);
            JtsRender jtsRender = new JtsRender(app);

            app.stroke(0);
            app.noFill();

            render.drawPolygonEdges(input.getInputBoundary());
            for (ZEdge e : graph.getTreeEdges()) {
                e.display(app);
            }

            jtsRender.drawGeometry(buffer);
            if (bufferControlPoints != null) {
                for (List<WB_Coord> list : bufferControlPoints) {
                    for (WB_Coord c : list) {
                        render.drawPoint(c, 5);
                    }
                }
            }
        }
    }
}