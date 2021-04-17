package webMain;

import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.TrafficNodeFixed;
import formInteractive.graphAdjusting.TrafficNodeTree;
import geometry.ZFactory;
import geometry.ZGraph;
import geometry.ZNode;
import geometry.ZPoint;
import gurobi.*;
import math.ZGeoMath;
import math.ZGraphMath;
import org.locationtech.jts.algorithm.MinimumDiameter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import subdivision.ZSD_SkeOBBStrip;
import subdivision.ZSD_SkeVorStrip;
import transform.ZTransform;
import wblut.geom.WB_Coord;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Mesh;
import wblut.hemesh.HE_Vertex;

import java.util.*;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/4/16
 * @time 18:26
 */
public class MallFloor {
    private int floorNum; // 层数
    private WB_Polygon boundary; // 外轮廓
    private int status; // 本层当前编辑状态

    // public space
    private TrafficGraph graph; // 动线轴线
    private List<List<WB_Coord>> bufferControlPoints; // 动线边界的细分控制点

    // split
    private Polygon publicBlock; // 公共分区
    private List<Polygon> shopBlocks; // 初始店铺分区

    // subdivision
    private List<WB_Polygon> allCells; // 初步剖分结果

    // evacuation
    private List<WB_Point> evacuationPoint; // 疏散楼梯位置

    /* ------------- constructor ------------- */

    public MallFloor(int floorNum, WB_Polygon boundary_receive) {
        setFloorNum(floorNum);
        setBoundary(boundary_receive);
    }

    /* ------------- member function ------------- */

    /**
     * update traffic graph
     *
     * @param innerNode_receive inner node from frontend
     * @param entryNode_receive entry node from frontend
     * @return void
     */
    public void updateGraph(List<WB_Point> innerNode_receive, List<WB_Point> entryNode_receive) {
        List<TrafficNode> innerNodes = new ArrayList<>();
        for (WB_Point p : innerNode_receive) {
            TrafficNodeTree treeNode = new TrafficNodeTree(p);
            treeNode.setBoundary(boundary);
            treeNode.setByRestriction(p.xd(), p.yd());
            innerNodes.add(treeNode);
        }
        List<TrafficNode> entryNodes = new ArrayList<>();
        for (WB_Point p : entryNode_receive) {
            TrafficNodeFixed fixedNode = new TrafficNodeFixed(p, boundary);
            fixedNode.setByRestriction(p.xd(), p.yd());
            entryNodes.add(fixedNode);
        }
        this.graph = new TrafficGraph(innerNodes, entryNodes);
    }

    /**
     * generate buffer geometries of public space
     *
     * @param polyAtrium_receive atriums shape received from frontend
     * @param dist               buffer distance
     * @param curvePtsNum        curve subdivide number
     * @return void
     */
    public void updateBuffer(List<WB_Polygon> polyAtrium_receive, double dist, int curvePtsNum) {
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

        // record control points
        // make intersection if floor is 1
        // otherwise, use the original buffer polygon
        this.bufferControlPoints = new ArrayList<>();
        if (this.floorNum == 1) {
            if (originBuffer instanceof Polygon) {
                LineString bufferLS = ZTransform.PolygonToLineString((Polygon) originBuffer).get(0);
                Polygon b = ZTransform.WB_PolygonToJtsPolygon(boundary);
                Geometry intersection = bufferLS.intersection(b);
                if (intersection instanceof MultiLineString) {
                    for (int i = 0; i < intersection.getNumGeometries(); i++) {
                        List<ZPoint> splitPoints = ZGeoMath.splitPolyLineEdge((LineString) intersection.getGeometryN(i), curvePtsNum);
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
        } else {
            if (originBuffer instanceof Polygon) {
                List<ZPoint> splitPoints = ZGeoMath.splitPolygonEdge((Polygon) originBuffer, curvePtsNum);
                List<WB_Coord> splitPointsEach = new ArrayList<>();
                for (ZPoint p : splitPoints) {
                    splitPointsEach.add(p.toWB_Point());
                }
                bufferControlPoints.add(splitPointsEach);
            }
        }

    }

    /**
     * split boundary by Polygonizer, find public block and shop blocks
     *
     * @param bufferCurve_receive traffic curves from frontend
     * @return void
     */
    public void updatePolygonizer(List<LineString> bufferCurve_receive) {
        // spilt blocks
        Polygonizer pr = new Polygonizer();
        Geometry nodedLineStrings = ZTransform.WB_PolyLineToJtsLineString(boundary);
        if (this.floorNum == 1) {
            for (LineString ls : bufferCurve_receive) {
                LineString newLs = ZFactory.createExtendedLineString(ls, 0.1);
                nodedLineStrings = nodedLineStrings.union(newLs);
            }
        } else {
            for (LineString ls : bufferCurve_receive) {
                nodedLineStrings = nodedLineStrings.union(ls);
            }
        }
        pr.add(nodedLineStrings);
        Collection<Polygon> allPolys = pr.getPolygons();

        // verify one tree node to find public poly, others are shop blocks
        Point verify = graph.getTreeNodes().get(0).toJtsPoint();
        for (Polygon p : allPolys) {
            if (p.contains(verify)) {
                this.publicBlock = p;
                break;
            }
        }
        if (publicBlock != null) {
            allPolys.remove(publicBlock);
            this.shopBlocks = new ArrayList<>();
            shopBlocks.addAll(allPolys);
        } else {
            throw new NullPointerException("can't find public space block polygon");
        }
    }

    /**
     * perform first-level shop subdivision
     *
     * @param span span of each cell
     * @return void
     */
    public void updateSubdivision(double span) {
        this.allCells = new ArrayList<>();

        if (this.floorNum == 1) {
            for (Polygon shopBlock : shopBlocks) {
                double area = Math.abs(shopBlock.getArea());
                double obbArea = MinimumDiameter.getMinimumRectangle(shopBlock).getArea();
                double areaRatio = area / obbArea;
                if (areaRatio < 0.75) {
                    ZSD_SkeVorStrip divTool = new ZSD_SkeVorStrip(shopBlock);
                    divTool.setSpan(span);
                    divTool.performDivide();
                    allCells.addAll(divTool.getAllSubPolygons());
                } else {
                    ZSD_SkeOBBStrip divTool = new ZSD_SkeOBBStrip(shopBlock);
                    divTool.setSpan(span);
                    divTool.performDivide();
                    allCells.addAll(divTool.getAllSubPolygons());
                }
            }
        } else {
            for (Polygon shopBlock : shopBlocks) {
                ZSD_SkeVorStrip divTool = new ZSD_SkeVorStrip(shopBlock);
                divTool.setSpan(span);
                divTool.performDivide();
                allCells.addAll(divTool.getAllSubPolygons());
            }
        }
    }

    /**
     * calculate evacuation points
     *
     * @param cellPolys_receive shop cells after adjust from frontend
     * @return void
     */
    public void updateEvacuation(List<WB_Polygon> cellPolys_receive) {
        // 构造HE_Mesh来找到boundary vertices
        List<WB_Polygon> cells = new ArrayList<>(cellPolys_receive);
        WB_Polygon pb = ZTransform.jtsPolygonToWB_Polygon(publicBlock);
        cells.add(pb);
        HEC_FromPolygons hec = new HEC_FromPolygons(cells);
        HE_Mesh floorMesh = hec.create();

        List<HE_Vertex> allVertices = floorMesh.getAllBoundaryVertices();
        List<ZPoint> allPossiblePoints = new ArrayList<>();
        for (HE_Vertex v : allVertices) {
            if (!WB_GeometryOp.contains2D(v, pb)) {
                // 排除与公共动线区域相接的点
                allPossiblePoints.add(new ZPoint(v));
            }
        }

        // 构造临时的graph
        ZGraph tempGraph = graph.duplicate();
        List<ZNode> nodesToCal = new ArrayList<>(); // possible points在tempGraph上的替身
        for (ZPoint pp : allPossiblePoints) {
            ZPoint closest = ZGeoMath.closestPointToLineList(pp, graph.getAllEdges());
            ZNode closestAsNode = new ZNode(closest.xd(), closest.yd(), closest.zd());
            nodesToCal.add(closestAsNode);
            tempGraph.addNodeByDist(closestAsNode);
        }
//        System.out.println("tempGraph nodes after first rebuild:  " + tempGraph.getNodesNum());
        List<ZPoint> splitPoints = ZGraphMath.splitGraphEachEdgeByStep(tempGraph, 2);
        splitPoints.removeAll(tempGraph.getNodes());
//        System.out.println("splitPoints.size()  " + splitPoints.size());
        for (ZPoint sp : splitPoints) {
            tempGraph.addNodeByDist(sp);
        }
//        System.out.println("tempGraph nodes after second rebuild:  " + tempGraph.getNodesNum());

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
//        for (ZNode n : targetNodeMap.keySet()) {
//            System.out.println(n.toString() + "   " + targetNodeMap.get(n));
//        }

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
            for (GRBVar var : vars) {
                expr.addTerm(1.0, var);
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
            this.evacuationPoint = new ArrayList<>();
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].get(GRB.DoubleAttr.X) > 0.5) {
                    evacuationPoint.add(allPossiblePoints.get(i).toWB_Point());
                }
            }
            // Dispose of model and environment
            model.dispose();
            env.dispose();

//            System.out.println("evacuationPoint.size()" + evacuationPoint.size());
        } catch (GRBException e) {
            System.out.println(
                    "Error code: "
                            + e.getErrorCode()
                            + ". "
                            + e.getMessage()
            );
        }
    }

    /* ------------- clear ------------- */

    public void disposeSplit() {
        this.publicBlock = null;
        this.shopBlocks = new ArrayList<>();
    }

    public void disposeSubdivision() {
        this.allCells = new ArrayList<>();
    }

    /* ------------- setter & getter ------------- */

    public void setFloorNum(int floorNum) {
        this.floorNum = floorNum;
    }

    public void setBoundary(WB_Polygon boundary) {
        this.boundary = boundary;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public TrafficGraph getGraph() {
        return graph;
    }

    public List<List<WB_Coord>> getBufferControlPoints() {
        return bufferControlPoints;
    }

    public List<WB_Polygon> getAllCells() {
        return allCells;
    }

    public List<WB_Point> getEvacuationPoint() {
        return evacuationPoint;
    }

    /* ------------- draw ------------- */

}
