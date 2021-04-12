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
import gurobi.*;
import igeo.IVecR;
import main.ArchiJSON;
import main.ImportData;
import math.ZGeoMath;
import math.ZGraphMath;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import processing.core.PApplet;
import render.JtsRender;
import subdivision.ZSD_SkeOBBStrip;
import subdivision.ZSD_SkeVorStrip;
import subdivision.ZSubdivision;
import transform.ZTransform;
import wblut.geom.*;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;
import wblut.processing.WB_Render;

import java.util.*;

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
        List<Geometry> geos = new ArrayList<>(graph.toLineStrings());
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
    private ZSubdivision sub1;
    private ZSubdivision sub2;
    private ZSubdivision sub3;

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
            double areaRatio = shopBlockPolys.get(i).getArea() / MinimumDiameter.getMinimumRectangle(shopBlockPolys.get(i)).getArea();
            if (areaRatio < 0.8) {
                ZSD_SkeVorStrip divTool = new ZSD_SkeVorStrip(shopBlockPolys.get(i));
                divTool.setSpan(8.4);
                divTool.performDivide();
                if (i == 0) {
                    sub1 = divTool;
                } else if (i == 1) {
                    sub2 = divTool;
                } else {
                    sub3 = divTool;
                }
                shopCells.add(divTool.getAllSubPolygons());
            } else {
                ZSD_SkeOBBStrip divTool = new ZSD_SkeOBBStrip(shopBlockPolys.get(i));
                divTool.setSpan(8.4);
                divTool.performDivide();
                if (i == 0) {
                    sub1 = divTool;
                } else if (i == 1) {
                    sub2 = divTool;
                } else {
                    sub3 = divTool;
                }
                shopCells.add(divTool.getAllSubPolygons());
            }
        }
    }

    private List<ZPoint> evacuationPoint;
    private ZGraph tempGraph;
    private boolean drawGraph = false;

    /**
     * description
     *
     * @param
     * @return void
     */
    public void generateEvacuation() {
        // 构造HE_Mesh来找到boundary vertices
        List<WB_Polygon> cells = new ArrayList<>();
        for (List<WB_Polygon> polygonList : shopCells) {
            cells.addAll(polygonList);
        }
        WB_Polygon publicBlock = ZTransform.jtsPolygonToWB_Polygon(publicBlockPoly);
        cells.add(publicBlock);
        HEC_FromPolygons hec = new HEC_FromPolygons(cells);
        HE_Mesh floorMesh = hec.create();

        List<HE_Vertex> allVertices = floorMesh.getAllBoundaryVertices();
        List<ZPoint> allPossiblePoints = new ArrayList<>();
        for (HE_Vertex v : allVertices) {
            if (!WB_GeometryOp.contains2D(v, publicBlock)) {
                // 排除与公共动线区域相接的点
                allPossiblePoints.add(new ZPoint(v));
            }
        }

        // 构造临时的graph
        tempGraph = graph.duplicate();
        List<ZNode> nodesToCal = new ArrayList<>(); // possible points在tempGraph上的替身
        for (ZPoint pp : allPossiblePoints) {
            ZPoint closest = ZGeoMath.closestPointToLineList(pp, graph.getAllEdges());
            ZNode closestAsNode = new ZNode(closest.xd(), closest.yd(), closest.zd());
            nodesToCal.add(closestAsNode);
            tempGraph.addNodeByDist(closestAsNode);
        }
        System.out.println("tempGraph nodes after first rebuild:  " + tempGraph.getNodesNum());
        List<ZPoint> splitPoints = ZGraphMath.splitGraphEachEdgeByStep(tempGraph, 2);
        splitPoints.removeAll(tempGraph.getNodes());
        System.out.println("splitPoints.size()  " + splitPoints.size());
        for (ZPoint sp : splitPoints) {
            tempGraph.addNodeByDist(sp);
        }
        System.out.println("tempGraph nodes after second rebuild:  " + tempGraph.getNodesNum());

        // 计算每个possible points能够服务到多少target node
        Map<ZNode, List<Integer>> targetNodeMap = new HashMap<>();
        for (ZNode n : tempGraph.getNodes()) {
            targetNodeMap.put(n, new ArrayList<>());
        }
        double evacuationDist = 56;
        for (int i = 0; i < nodesToCal.size(); i++) {
            double dist = evacuationDist - allPossiblePoints.get(i).distance(nodesToCal.get(i));
            if (dist > 0) {
                List<ZNode> targetReached = ZGraphMath.nodesOnGraphByDist(nodesToCal.get(i), null, dist);
                for (ZNode n : targetReached) {
                    if (targetNodeMap.containsKey(n)) {
                        targetNodeMap.get(n).add(i);
                    }
                }
            }
        }
        for (ZNode n : targetNodeMap.keySet()) {
            System.out.println(n.toString() + "   " + targetNodeMap.get(n));
        }

        // gurobi optimizer
        System.out.println("********* gurobi optimizing *********" + "\n");
        try {
            // Create empty environment, set options, and start
            GRBEnv env = new GRBEnv(true);
            env.set("logFile", "mip1.log");
            env.start();
            // Create empty model
            GRBModel model = new GRBModel(env);
            // Create variables
            GRBVar[] vars = new GRBVar[allPossiblePoints.size()];
            for (int i = 0; i < vars.length; i++) {
                vars[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "var" + i);
            }
            // Set objective
            GRBLinExpr expr = new GRBLinExpr();
            for (int i = 0; i < vars.length; i++) {
                expr.addTerm(1.0, vars[i]);
            }
            model.setObjective(expr, GRB.MINIMIZE);
            // Add constraint
            for (ZNode n : targetNodeMap.keySet()) {
                expr = new GRBLinExpr();
                if (targetNodeMap.get(n).size() > 0) {
                    for (int i : targetNodeMap.get(n)) {
                        expr.addTerm(1.0, vars[i]);
                    }
                }
                model.addConstr(expr, GRB.GREATER_EQUAL, 1, "cons" + n.toString());
            }
            // Optimize model
            model.optimize();
            // output
            System.out.println("\n" + "******* result output *******");
            System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
            evacuationPoint = new ArrayList<>();
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].get(GRB.DoubleAttr.X) > 0.5) {
                    evacuationPoint.add(allPossiblePoints.get(i));
                }
            }
            // Dispose of model and environment
            model.dispose();
            env.dispose();

            System.out.println("evacuationPoint.size()" + evacuationPoint.size());
//            for (ZPoint p : evacuationPoint) {
//                System.out.println(p.toString());
//            }
        } catch (GRBException e) {
            System.out.println(
                    "Error code: "
                            + e.getErrorCode()
                            + ". "
                            + e.getMessage()
            );
        }
        drawGraph = true;
    }

    // TODO: 2021/4/9 数据指标显示 
    // stats
    private double totalArea; // 当前层总面积
    private double shopArea; // 店铺面积
    private int shopNum; // 店铺数量
    private double shopRatioTotal; // 综合得铺率
    private double shopRatioFloor; // 分层得铺率
    private double smallShopRatio; // 小铺率（数量占比）

    public void generateStats() {
        this.totalArea = input.getBoundaryArea();
    }

    /* ------------- JSON converting ------------- */

    /**
     * convert backend geometries to ArchiJSON
     * boundary, graph segments, buffer control points
     *
     * @param clientID
     * @param gson
     * @return main.ArchiJSON
     */
    public ArchiJSON toArchiJSON1(String clientID, Gson gson) {
        // preparing data
        List<WB_Point> fixedNodes = getFixedNode();
        List<WB_Segment> graphSegments = graph.toWB_Segments();
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
     * first-level subdivision cells
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

            if (drawGraph) {
                app.pushMatrix();
                app.pushStyle();
                tempGraph.display(app);
                app.fill(0, 0, 255);
                if (evacuationPoint != null) {
                    for (ZPoint p : evacuationPoint) {
                        p.displayAsPoint(app, 8);
                    }
                }
                app.popStyle();
                app.popMatrix();
            }
        }
    }
}