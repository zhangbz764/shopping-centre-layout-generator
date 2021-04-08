package webMain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import org.locationtech.jts.operation.polygonize.Polygonizer;
import processing.core.PApplet;
import render.JtsRender;
import subdivision.ZSD_SkeVorStrip;
import transform.ZTransform;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.geom.WB_Segment;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * main generator
 * catch data from front-end
 * convert data to front-end
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project archijson
 * @date 2021/3/10
 * @time 17:55
 */
public class MallGenerator {
    private boolean draw = false; // display switch

    private final ImportData input = new ImportData(); // (temp) input data from backend

    // geometries from front-end
    public List<WB_Point> innerNode_receive;
    public List<WB_Polygon> polyAtrium_receive;
    public List<LineString> bufferCurve_receive;

    // generator
    private TrafficGraph graph;
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
        // receive geometries and generate buffer
        List<Geometry> geos = new ArrayList<>(getLineStrings());
        if (polyAtrium_receive != null && polyAtrium_receive.size() > 0) {
            for (WB_Polygon p : polyAtrium_receive) {
                geos.add(ZTransform.WB_PolygonToJtsPolygon(p));
            }
        }
        Geometry[] geometries = geos.toArray(new Geometry[0]);
        GeometryCollection collection = ZFactory.jtsgf.createGeometryCollection(geometries);
        Geometry originBuffer = collection.buffer(dist);

        // make intersection and record control points
        this.bufferControlPoints = new ArrayList<>();
        if (originBuffer instanceof Polygon) {
            LineString bufferLS = ZTransform.PolygonToLineString((Polygon) originBuffer).get(0);
            Polygon boundary = ZTransform.WB_PolygonToJtsPolygon(input.getInputBoundary());
            Geometry intersection = bufferLS.intersection(boundary);
            if (intersection instanceof MultiLineString) {
                for (int i = 0; i < intersection.getNumGeometries(); i++) {
                    List<ZPoint> splitPoints = ZGeoMath.splitPolyLineEdge((LineString) intersection.getGeometryN(i), 6);
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

    private Polygon publicBlockPoly;
    private List<Polygon> shopBlockPolys;
    private List<List<WB_Polygon>> shopCells;
    private ZSD_SkeVorStrip sub1;
    private ZSD_SkeVorStrip sub2;
    private ZSD_SkeVorStrip sub3;

    /**
     * split the whole boundary and generate first-level subdivision
     *
     * @return void
     */
    public void generateSplit() {
        // spilt blocks
        Polygonizer pr = new Polygonizer();
        Geometry nodedLineStrings = ZTransform.WB_PolyLineToJtsLineString(input.getInputBoundary());
        for (LineString ls : bufferCurve_receive) {
            LineString newLs = ZFactory.createExtendedLineString(ls, 0.1);
            nodedLineStrings = nodedLineStrings.union(newLs);
        }
        pr.add(nodedLineStrings);
        Collection<Polygon> allPolys = pr.getPolygons();
        Point verify = ZTransform.WB_CoordToPoint(innerNode_receive.get(0));

        for (Polygon p : allPolys) {
            if (p.contains(verify)) {
                this.publicBlockPoly = (Polygon) p;
                break;
            }
        }
        allPolys.remove(publicBlockPoly);
        this.shopBlockPolys = (List<Polygon>) allPolys;
        System.out.println("shop blocks: " + shopBlockPolys.size());

        // perform first-level shop partition
        this.shopCells = new ArrayList<>();
        for (int i = 0; i < shopBlockPolys.size(); i++) {
            ZSD_SkeVorStrip divTool = new ZSD_SkeVorStrip(shopBlockPolys.get(i));
            divTool.setSpan(8);
            divTool.performDivide();
            if (i == 0) {
                sub1 = divTool;
            } else if (i == 1) {
                sub2 = divTool;
            } else {
                sub3 = divTool;
            }
            shopCells.add(divTool.getAllSubPolygons());
            System.out.println(divTool.getAllSubPolygons().size());
        }
    }

    /* ------------- JSON converting ------------- */

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
        List<JsonElement> elements = new ArrayList<>();

        // converting to json
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

        for (List<WB_Coord> splitPointsEach : bufferControlPoints) {
            Vertices bufferControlPointsEach = WB_Converter.toVertices(splitPointsEach, 3);
            JsonObject prop3 = new JsonObject();
            prop3.addProperty("name", "bufferControl");
            bufferControlPointsEach.setProperties(prop3);
            elements.add(gson.toJsonTree(bufferControlPointsEach));
        }

        // setup json
        json.setGeometryElements(elements);
        return json;
    }

    /**
     * convert backend geometries to ArchiJSON
     *
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSON2(String clientID, Gson gson) {
        // initializing
        ArchiJSON json = new ArchiJSON();
        json.setId(clientID);
        List<JsonElement> elements = new ArrayList<>();

        // converting to json
        for (List<WB_Polygon> cellList : shopCells) {
            for (WB_Polygon p : cellList) {
                Segments cell = WB_Converter.toSegments(p);
                JsonObject prop = new JsonObject();
                prop.addProperty("name", "shopCell");
                cell.setProperties(prop);
                elements.add(gson.toJsonTree(cell));
            }
        }

        // setup json
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

    public void setBufferCurve_receive(List<LineString> bufferCurve_receive) {
        this.bufferCurve_receive = bufferCurve_receive;
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
            WB_Render render = new WB_Render(app);
            JtsRender jtsRender = new JtsRender(app);

            app.stroke(0);
            app.noFill();

            render.drawPolygonEdges(input.getInputBoundary());
            for (ZEdge e : graph.getTreeEdges()) {
                e.display(app);
            }

            if (bufferCurve_receive != null) {
                for (LineString ls : bufferCurve_receive) {
                    jtsRender.drawGeometry(ls);
                }
            }

//            if (subdivisions != null) {
//                for (ZSD_SkeVorStrip sd : subdivisions) {
//                    sd.display(app, render);
//                }
//            }

            if (sub1 != null) {
                sub1.display(app, render);
            }
            if (sub2 != null) {
                sub2.display(app, render);
            }
            if (sub3 != null) {
                sub3.display(app, render);
            }

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