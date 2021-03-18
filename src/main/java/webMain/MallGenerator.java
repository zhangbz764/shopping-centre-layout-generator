package webMain;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import converter.WB_Converter;
import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.TrafficNodeFixed;
import formInteractive.graphAdjusting.TrafficNodeTree;
import geometry.Segments;
import geometry.ZEdge;
import geometry.ZGeoFactory;
import main.ArchiJSON;
import main.ImportData;
import org.locationtech.jts.geom.*;
import processing.core.PApplet;
import transform.ZTransform;
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
    private boolean draw = false;

    private final ImportData input = new ImportData();
    public List<WB_Point> innerNode_receive;
    public List<WB_Polygon> polyAtrium_receive;


    public TrafficGraph graph;
    public List<WB_Polygon> bufferPolys;

    /* ------------- constructor ------------- */

    public MallGenerator() {

    }

    public void init() {
        String path = "./src/main/resources/0310.3dm";
        input.loadData(path, 1);
    }

    /* ------------- member function ------------- */

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

    public void generateBuffer() {
        List<Geometry> geos = new ArrayList<>(getLineStrings());
        if (polyAtrium_receive != null && polyAtrium_receive.size() > 0) {
            for (WB_Polygon p : polyAtrium_receive) {
                geos.add(ZTransform.WB_PolygonToJtsPolygon(p));
            }
        }
        Geometry[] geometries = geos.toArray(new Geometry[0]);
        GeometryCollection collection = ZGeoFactory.jtsgf.createGeometryCollection(geometries);
        Geometry buffer = collection.buffer(10);

        this.bufferPolys = new ArrayList<>();
        if (buffer instanceof Polygon) {
            bufferPolys.add(ZTransform.jtsPolygonToWB_Polygon((Polygon) buffer));
        }
    }

    /**
     * description
     *
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSON(String clientID, Gson gson) {
        // preparing data
        List<WB_Point> fixedNodes = getInnerNode();
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
            elements.add(gson.toJsonTree(segments));
        }
        Segments poly = WB_Converter.toSegments(boundary);
        elements.add(gson.toJsonTree(poly));

        for(WB_Polygon p: bufferPolys){
            Segments bufferPoly = WB_Converter.toSegments(p);
            elements.add(gson.toJsonTree(bufferPoly));
        }

        json.setGeometryElements(elements);
        return json;
    }

    /* ------------- setter & getter ------------- */

    public List<WB_Point> getInnerNode() {
        List<WB_Point> pts = new ArrayList<>();
        for (TrafficNode n : graph.getFixedNodes()) {
            pts.add(n.toWB_Point());
        }
        return pts;
    }

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

    public List<LineString> getBufferedLineString() {
        List<LineString> ls = new ArrayList<>();
        for (ZEdge e : graph.getTreeEdges()) {
            ls.add(e.toJtsLineString());
        }
        for (ZEdge e : graph.getFixedEdges()) {
            ls.add(e.toJtsLineString());
        }
        return null;
    }

    /* ------------- draw ------------- */

    public void draw(PApplet app) {
        if (draw) {
            WB_Render3D render = new WB_Render3D(app);

            app.stroke(0);
            app.noFill();

            render.drawPolygonEdges(input.getInputBoundary());
            for (ZEdge e : graph.getTreeEdges()) {
                e.display(app);
            }
        }
    }
}