package main;

import basicGeometry.*;
import mallElementNew.TrafficGraph;
import mallElementNew.TrafficNode;
import mallElementNew.TrafficNodeFixed;
import mallElementNew.TrafficNodeTree;
import mallElementNew.StructureGrid;
import mallElementNew.Shop;
import math.ZGeoMath;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import transform.ZTransform;
import wblut.geom.*;

import java.util.*;

/**
 * a single floor of a shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/4/16
 * @time 18:26
 */
public class MallFloor {
    private int floorNum; // 层数
    private Polygon boundary; // 外轮廓
    private int status; // 本层当前编辑状态

    // public space
//    private TrafficGraph graph; // 动线轴线
    private List<List<WB_Coord>> bufferControlPoints; // 动线边界的细分控制点

    // split
    private Polygon publicBlock; // 公共分区
    private List<Polygon> shopBlocks; // 初始店铺分区

    // subdivision
    private Point verify;
    private List<ZPoint> turning; // 记录需要拐角的剖分线所属的轴网交点
    private List<LineString> allSubLines; // 初次剖分线
    private List<Shop> allShops; // 初次剖分结果

    // evacuation
    private List<WB_Point> evacuationPoint; // 疏散楼梯位置

    /* ------------- constructor ------------- */

    public MallFloor(int floorNum, Polygon boundary_receive) {
        setFloorNum(floorNum);
        setBoundary(boundary_receive);
    }

    /* ------------- member function ------------- */

//    /**
//     * update traffic graph
//     *
//     * @param innerNode_receive inner node from frontend
//     * @param entryNode_receive entry node from frontend
//     * @return void
//     */
//    public void updateGraph(List<WB_Point> innerNode_receive, List<WB_Point> entryNode_receive) {
//        List<TrafficNode> innerNodes = new ArrayList<>();
//        for (WB_Point p : innerNode_receive) {
//            TrafficNodeTree treeNode = new TrafficNodeTree(p);
//            treeNode.setBoundary(boundary);
//            treeNode.setByRestriction(p.xd(), p.yd());
//            innerNodes.add(treeNode);
//        }
//        List<TrafficNode> entryNodes = new ArrayList<>();
//        for (WB_Point p : entryNode_receive) {
//            TrafficNodeFixed fixedNode = new TrafficNodeFixed(p, boundary);
//            fixedNode.setByRestriction(p.xd(), p.yd());
//            entryNodes.add(fixedNode);
//        }
//        this.graph = new TrafficGraph(innerNodes, entryNodes);
//    }
//
//    /**
//     * generate buffer geometries of public space
//     *
//     * @param polyAtrium_receive atriums shape received from frontend
//     * @param dist               buffer distance
//     * @param curvePtsNum        curve subdivide number
//     * @return void
//     */
//    public void updateBuffer(List<WB_Polygon> polyAtrium_receive, double dist, int curvePtsNum) {
//        // receive geometries and generate buffer
//        List<Geometry> geos = new ArrayList<>(graph.toLineStrings());
//        if (polyAtrium_receive != null && polyAtrium_receive.size() > 0) {
//            for (WB_Polygon p : polyAtrium_receive) {
//                geos.add(ZTransform.WB_PolygonToPolygon(p));
//            }
//        }
//        Geometry[] geometries = geos.toArray(new Geometry[0]);
//        GeometryCollection collection = ZFactory.jtsgf.createGeometryCollection(geometries);
//        Geometry originBuffer = collection.buffer(dist);
//
//        // record control points
//        // make intersection if floor is 1
//        // otherwise, use the original buffer polygon
//        this.bufferControlPoints = new ArrayList<>();
//        if (this.floorNum == 1) {
//            if (originBuffer instanceof Polygon) {
//                LineString bufferLS = ZTransform.PolygonToLineString((Polygon) originBuffer).get(0);
//                Polygon b =boundary;
//                Geometry intersection = bufferLS.intersection(b);
//                if (intersection instanceof MultiLineString) {
//                    for (int i = 0; i < intersection.getNumGeometries(); i++) {
//                        List<ZPoint> splitPoints = ZGeoMath.splitPolyLineEdge((LineString) intersection.getGeometryN(i), curvePtsNum);
//                        List<WB_Coord> splitPointsEach = new ArrayList<>();
//                        for (ZPoint p : splitPoints) {
//                            splitPointsEach.add(p.toWB_Point());
//                        }
//                        bufferControlPoints.add(splitPointsEach);
//                    }
//                } else {
//                    System.out.println("not MultiLineString");
//                }
//            }
//        } else {
//            if (originBuffer instanceof Polygon) {
//                List<ZPoint> splitPoints = ZGeoMath.splitPolygonEdge((Polygon) originBuffer, curvePtsNum);
//                List<WB_Coord> splitPointsEach = new ArrayList<>();
//                for (ZPoint p : splitPoints) {
//                    splitPointsEach.add(p.toWB_Point());
//                }
//                bufferControlPoints.add(splitPointsEach);
//            }
//        }
//
//    }

    /**
     * split boundary by Polygonizer, find public block and shop blocks
     *
     * @param bufferCurve_receive traffic curves from frontend
     * @return void
     */
    private void updatePolygonizer(List<LineString> bufferCurve_receive) {
        // spilt blocks
        Polygonizer pr = new Polygonizer();
        Geometry nodedLineStrings = ZTransform.PolygonToLineString(boundary).get(0);
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
//        this.verify = graph.getTreeNodes().get(0).toJtsPoint();
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
     * @param bufferCurve_receive buffer curve from front-end
     * @param grid                construction grid
     * @return void
     */
    public void updateSubdivision(List<LineString> bufferCurve_receive, StructureGrid[] grid) {
        updatePolygonizer(bufferCurve_receive);

        this.allShops = new ArrayList<>();
        this.allSubLines = new ArrayList<>();

        Map<ZPoint, ZLine> intersectGrid = selectIntersection(bufferCurve_receive, grid);

        if (this.floorNum == 1) {
            List<List<LineString>> subLines = new ArrayList<>();
            int shopBlockNum = this.shopBlocks.size();
            for (int i = 0; i < shopBlockNum; i++) {
                subLines.add(new ArrayList<>());
            }

            for (ZPoint p : intersectGrid.keySet()) {
                int index = -1;
                // 根据是否朝向判断方向
                ZLine lineValue = intersectGrid.get(p);
                ZPoint[] ray = new ZPoint[]{p, lineValue.getDirection()};
                Point testP = p.add(lineValue.getDirectionNor().scaleTo(1)).toJtsPoint();
                boolean flag = true;
                if (publicBlock.contains(testP)) {
                    // reverse
                    ray = new ZPoint[]{p, lineValue.getDirection().scaleTo(-1)};
                    testP = p.add(lineValue.getDirectionNor().scaleTo(-1)).toJtsPoint();

                    for (int i = 0; i < shopBlockNum; i++) {
                        if (shopBlocks.get(i).contains(testP)) {
                            index = i;
                            break;
                        }
                    }
                } else {
                    flag = false;
                    for (int i = 0; i < shopBlockNum; i++) {
                        if (shopBlocks.get(i).contains(testP)) {
                            index = i;
                            break;
                        }
                    }
                }

                // 端头是否需要转弯相交
                if (turning.contains(p)) {
                    int a = -1; // 所在轴网标号
                    int[] interPos = {-1, -1};
                    for (int i = 0; i < grid.length; i++) {
                        interPos = grid[i].getLineIndex(lineValue);
                        if (interPos[0] != -1) {
                            a = i;
                            break;
                        }
                    }
                    if (a != -1) {
                        StructureGrid g = grid[a];

                        ZPoint nextNode = null;
                        if (interPos[0] == 0) {
                            // 01上
                            ZPoint start = lineValue.getPt0();

                            double unitLength = g.getUnit12().getLength();
                            double distance = start.distance(p);
                            int num = 0;
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num + 1];
                            }
                        } else {
                            // 12上
                            ZPoint start = lineValue.getPt0();

                            int num = 0;
                            double unitLength = g.getUnit10().getLength();
                            double distance = start.distance(p);
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num][interPos[1]];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num + 1][interPos[1]];
                            }
                        }
                        // 两段线：延长线和最近点连线 合成一条LineString
                        ZPoint[] newRay = new ZPoint[]{nextNode, ray[1]};
                        ZLine extend = ZGeoMath.extendSegmentToPolygon(newRay, ZTransform.PolygonToWB_Polygon(boundary));
                        if (extend != null) {
                            if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                                // 剔除过长剖分线
                                WB_Point closest = WB_GeometryOp.getClosestPoint2D(nextNode.toWB_Point(), ZTransform.PolygonToWB_PolyLine(publicBlock).get(0));
                                Coordinate[] coords = new Coordinate[3];
                                coords[0] = new Coordinate(closest.xd(), closest.yd());
                                coords[1] = new Coordinate(extend.getPt0().xd(), extend.getPt0().yd());
                                coords[2] = new Coordinate(extend.getPt1().xd(), extend.getPt1().yd());

                                LineString subLine = ZFactory.jtsgf.createLineString(coords);
                                subLines.get(index).add(subLine);
                                allSubLines.add(subLine);
                            }
                        }
                    }
                } else {
                    ZLine extend = ZGeoMath.extendSegmentToPolygon(ray, ZTransform.PolygonToWB_Polygon(boundary));
                    if (extend != null) {
                        if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                            // 剔除过长剖分线
                            LineString subLine = extend.toJtsLineString();
                            subLines.get(index).add(subLine);
                            allSubLines.add(subLine);
                        }
                    }
                }
            }

            // polygonizer to get cells
            for (int i = 0; i < shopBlockNum; i++) {
                Polygonizer pr = new Polygonizer();
                Geometry nodedLineStrings = ZTransform.PolygonToLineString(shopBlocks.get(i)).get(0);
                List<LineString> blockSL = subLines.get(i);
                for (int j = 0; j < blockSL.size(); j++) {
                    LineString sl = ZFactory.createExtendedLineString(blockSL.get(j), 0.1);
                    nodedLineStrings = nodedLineStrings.union(sl);
                }
                pr.add(nodedLineStrings);
                Collection<Polygon> allPolys = pr.getPolygons();
                for (Polygon poly : allPolys) {
                    if (poly.getArea() > 1.0) {
                        // 防止 polygonizer 时出现极小误差
                        allShops.add(new Shop(poly));
                    }
                }
            }
        } else {
            List<LineString> subLines = new ArrayList<>();

            for (ZPoint p : intersectGrid.keySet()) {
                ZLine lineValue = intersectGrid.get(p);
                ZPoint[] ray = new ZPoint[]{p, lineValue.getDirection()};
                Point testP = p.add(lineValue.getDirectionNor().scaleTo(1)).toJtsPoint();
                boolean flag = false;
                if (publicBlock.contains(testP)) {
                    // reverse
                    ray = new ZPoint[]{p, lineValue.getDirection().scaleTo(-1)};
                    flag = true;
                }
                // 端头是否需要转弯相交
                if (turning.contains(p)) {
                    int a = -1; // 所在轴网标号
                    int[] interPos = {-1, -1};
                    for (int i = 0; i < grid.length; i++) {
                        interPos = grid[i].getLineIndex(lineValue);
                        if (interPos[0] != -1) {
                            a = i;
                            break;
                        }
                    }
                    if (a != -1) {
                        StructureGrid g = grid[a];

                        ZPoint nextNode = null;
                        if (interPos[0] == 0) {
                            // 01上
                            ZPoint start = lineValue.getPt0();

                            double unitLength = g.getUnit12().getLength();
                            double distance = start.distance(p);
                            int num = 0;
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num + 1];
                            }
                        } else {
                            // 12上
                            ZPoint start = lineValue.getPt0();

                            int num = 0;
                            double unitLength = g.getUnit10().getLength();
                            double distance = start.distance(p);
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num][interPos[1]];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num + 1][interPos[1]];
                            }
                        }
                        // 两段线：延长线和最近点连线 合成一条LineString
                        ZPoint[] newRay = new ZPoint[]{nextNode, ray[1]};
                        ZLine extend = ZGeoMath.extendSegmentToPolygon(newRay, ZTransform.PolygonToWB_Polygon(boundary));
                        if (extend != null) {
                            if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                                // 剔除过长剖分线
//                                ZPoint closestP = ZGeoMath.closestPointToLineList(nextNode, ZFactory.breakWB_PolyLine(ZTransform.PolygonToWB_PolyLine(publicBlock).get(0)));
                                WB_Point closestP = WB_GeometryOp.getClosestPoint2D(nextNode.toWB_Point(), ZTransform.PolygonToWB_PolyLine(publicBlock).get(0));
                                if (closestP != null && !Double.isNaN(closestP.xd())) {
                                    Coordinate[] coords = new Coordinate[3];
                                    coords[0] = new Coordinate(closestP.xd(), closestP.yd());
                                    coords[1] = new Coordinate(extend.getPt0().xd(), extend.getPt0().yd());
                                    coords[2] = new Coordinate(extend.getPt1().xd(), extend.getPt1().yd());

                                    LineString subLine = ZFactory.jtsgf.createLineString(coords);
                                    subLines.add(subLine);
                                    allSubLines.add(subLine);
                                }
                            }
                        }
                    }
                } else {
                    ZLine extend = ZGeoMath.extendSegmentToPolygon(ray, ZTransform.PolygonToWB_Polygon(boundary));
                    if (extend != null) {
                        if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                            // 剔除过长剖分线
                            LineString subLine = extend.toJtsLineString();
                            subLines.add(subLine);
                            allSubLines.add(subLine);
                        }
                    }
                }
            }

            // polygonizer to get cells
            Polygonizer pr = new Polygonizer();
            List<LineString> shopBlockLS = ZTransform.PolygonToLineString(shopBlocks.get(0));
            Geometry nodedLineStrings = shopBlockLS.get(0);
            if (shopBlockLS.size() > 1) {
                for (int i = 1; i < shopBlockLS.size(); i++) {
                    nodedLineStrings = nodedLineStrings.union(shopBlockLS.get(i));
                }
            }
            for (int j = 0; j < subLines.size(); j++) {
                LineString sl = ZFactory.createExtendedLineString(subLines.get(j), 0.1);
                nodedLineStrings = nodedLineStrings.union(sl);
            }
            pr.add(nodedLineStrings);
            Collection<Polygon> allPolys = pr.getPolygons();

            boolean doIf = true;
//            Point verify = graph.getTreeNodes().get(0).toJtsPoint();
            for (Polygon poly : allPolys) {
                if (poly.getArea() > 1.0) {
                    // 防止 polygonizer 时出现极小误差
                    if (doIf) {
                        if (poly.contains(verify)) {
                            doIf = false;
                            continue;
                        }
                    }
                    allShops.add(new Shop(poly));
                }
            }
        }
    }

    /**
     * calculate evacuation points
     *
     * @param cellPolys_receive shop cells after adjust from frontend
     * @return void
     */
//    public void updateEvacuation(List<WB_Polygon> cellPolys_receive) {
//        // 构造HE_Mesh来找到boundary vertices
//        List<WB_Polygon> cells = new ArrayList<>(cellPolys_receive);
//        WB_Polygon pb = ZTransform.PolygonToWB_Polygon(publicBlock);
//        cells.add(pb);
//        HEC_FromPolygons hec = new HEC_FromPolygons(cells);
//        HE_Mesh floorMesh = hec.create();
//
//        List<HE_Vertex> allVertices = floorMesh.getAllBoundaryVertices();
//        List<ZPoint> allPossiblePoints = new ArrayList<>();
//        for (HE_Vertex v : allVertices) {
//            if (!WB_GeometryOp.contains2D(v, pb)) {
//                // 排除与公共动线区域相接的点
//                allPossiblePoints.add(new ZPoint(v));
//            }
//        }
//
//        // 构造临时的graph
//        ZGraph tempGraph = graph.duplicate();
//        List<ZNode> nodesToCal = new ArrayList<>(); // possible points在tempGraph上的替身
//        for (ZPoint pp : allPossiblePoints) {
//            ZPoint closest = ZGeoMath.closestPointToLineList(pp, graph.getAllEdges());
//            ZNode closestAsNode = new ZNode(closest.xd(), closest.yd(), closest.zd());
//            nodesToCal.add(closestAsNode);
//            tempGraph.addNodeByDist(closestAsNode);
//        }
////        System.out.println("tempGraph nodes after first rebuild:  " + tempGraph.getNodesNum());
//        List<ZPoint> splitPoints = ZGraphMath.splitGraphEachEdgeByStep(tempGraph, 2);
//        splitPoints.removeAll(tempGraph.getNodes());
////        System.out.println("splitPoints.size()  " + splitPoints.size());
//        for (ZPoint sp : splitPoints) {
//            tempGraph.addNodeByDist(sp);
//        }
////        System.out.println("tempGraph nodes after second rebuild:  " + tempGraph.getNodesNum());
//
//        // 计算每个possible points能够服务到多少target node
//        Map<ZNode, List<Integer>> targetNodeMap = new HashMap<>();
//        for (ZNode n : tempGraph.getNodes()) {
//            targetNodeMap.put(n, new ArrayList<>());
//        }
//        double evacuationDist = 56;
//        for (int i = 0; i < nodesToCal.size(); i++) {
//            double dist = evacuationDist - allPossiblePoints.get(i).distance(nodesToCal.get(i));
//            if (dist > 0) {
//                List<ZNode> targetReached = ZGraphMath.nodesOnGraphByDist(nodesToCal.get(i), null, dist);
//                for (ZNode n : targetReached) {
//                    if (targetNodeMap.containsKey(n)) {
//                        targetNodeMap.get(n).add(i);
//                    }
//                }
//            }
//        }
////        for (ZNode n : targetNodeMap.keySet()) {
////            System.out.println(n.toString() + "   " + targetNodeMap.get(n));
////        }
//
//        // gurobi optimizer
//        System.out.println("********* gurobi optimizing *********" + "\n");
//        try {
//            // Create empty environment, set options, and start
//            GRBEnv env = new GRBEnv(true);
//            env.set("logFile", "mip1.log");
//            env.start();
//            // Create empty model
//            GRBModel model = new GRBModel(env);
//            // Create variables
//            GRBVar[] vars = new GRBVar[allPossiblePoints.size()];
//            for (int i = 0; i < vars.length; i++) {
//                vars[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "var" + i);
//            }
//            // Set objective
//            GRBLinExpr expr = new GRBLinExpr();
//            for (GRBVar var : vars) {
//                expr.addTerm(1.0, var);
//            }
//            model.setObjective(expr, GRB.MINIMIZE);
//            // Add constraint
//            for (ZNode n : targetNodeMap.keySet()) {
//                expr = new GRBLinExpr();
//                if (targetNodeMap.get(n).size() > 0) {
//                    for (int i : targetNodeMap.get(n)) {
//                        expr.addTerm(1.0, vars[i]);
//                    }
//                }
//                model.addConstr(expr, GRB.GREATER_EQUAL, 1, "cons" + n.toString());
//            }
//            // Optimize model
//            model.optimize();
//            // output
//            System.out.println("\n" + "******* result output *******");
//            System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));
//            this.evacuationPoint = new ArrayList<>();
//            for (int i = 0; i < vars.length; i++) {
//                if (vars[i].get(GRB.DoubleAttr.X) > 0.5) {
//                    evacuationPoint.add(allPossiblePoints.get(i).toWB_Point());
//                }
//            }
//            // Dispose of model and environment
//            model.dispose();
//            env.dispose();
//
////            System.out.println("evacuationPoint.size()" + evacuationPoint.size());
//        } catch (GRBException e) {
//            System.out.println(
//                    "Error code: "
//                            + e.getErrorCode()
//                            + ". "
//                            + e.getMessage()
//            );
//        }
//    }

    /**
     * select intersection points of the grid and buffer curves
     *
     * @param bufferCurve_receive buffer curves from front end
     * @param grid                structure grid
     * @return java.util.Map<basicGeometry.ZPoint, basicGeometry.ZLine>
     */
    private Map<ZPoint, ZLine> selectIntersection(List<LineString> bufferCurve_receive, StructureGrid[] grid) {
        Map<ZPoint, ZLine> intersectGrid = new HashMap<>();

        this.turning = new ArrayList<>();
        if (this.floorNum == 1) {
            // 1层 若干条LineString
            for (LineString ls : bufferCurve_receive) {
                for (StructureGrid g : grid) {
                    for (ZLine l : g.getAllLines()) {
                        ZPoint[] linePD = l.toLinePD();
                        for (int i = 0; i < ls.getCoordinates().length - 2; i++) {
                            ZLine polySeg = new ZLine(
                                    ls.getCoordinateN(i),
                                    ls.getCoordinateN(i + 1)
                            );

                            double cos = l.angleCosWith(polySeg);
                            if (cos <= 0.5) {
                                // 大于60度，才取相交 否则舍弃
                                ZPoint[] polySegPD = polySeg.toLinePD();
                                ZPoint intersect = null;

                                ZPoint delta = polySegPD[0].sub(linePD[0]);
                                double crossBase = linePD[1].cross2D(polySegPD[1]);
                                double crossDelta0 = delta.cross2D(linePD[1]);
                                double crossDelta1 = delta.cross2D(polySegPD[1]);

                                if (Math.abs(crossBase) >= 0.00000001) {
                                    double s = crossDelta1 / crossBase; // seg
                                    double t = crossDelta0 / crossBase; // polySeg
                                    if (s >= 0 && s <= 1 && t >= 0 && t < 1) {
                                        intersect = polySegPD[0].add(polySegPD[1].scaleTo(t));
                                    }
                                }
                                if (intersect != null) {
                                    // 大于75度 直接取相交点 若60~75则另做记录
                                    if (cos > 0.26) {
                                        turning.add(intersect);
                                    }
                                    intersectGrid.put(intersect, l);
                                }
                            }
                        }
                        // final polyline segment: [--]
                        ZLine polySeg = new ZLine(
                                ls.getCoordinateN(ls.getCoordinates().length - 2),
                                ls.getCoordinateN(ls.getCoordinates().length - 1)
                        );
                        double cos = l.angleCosWith(polySeg);
                        if (cos <= 0.5) {
                            // 大于60度，才取相交 否则舍弃
                            ZPoint[] polySegPD = polySeg.toLinePD();
                            ZPoint intersect = null;

                            ZPoint delta = polySegPD[0].sub(linePD[0]);
                            double crossBase = linePD[1].cross2D(polySegPD[1]);
                            double crossDelta0 = delta.cross2D(linePD[1]);
                            double crossDelta1 = delta.cross2D(polySegPD[1]);

                            if (Math.abs(crossBase) >= 0.00000001) {
                                double s = crossDelta1 / crossBase; // seg
                                double t = crossDelta0 / crossBase; // polySeg
                                if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
                                    intersect = polySegPD[0].add(polySegPD[1].scaleTo(t));
                                }
                            }
                            if (intersect != null) {
                                // 大于75度 直接取相交点 若60~75则另做记录
                                if (cos > 0.26) {
                                    turning.add(intersect);
                                }
                                intersectGrid.put(intersect, l);
                            }
                        }
                    }
                }
            }
        } else {
            // 2层及以上 闭合LineString
            LineString ls = bufferCurve_receive.get(0);
            for (StructureGrid g : grid) {
                for (ZLine l : g.getAllLines()) {
                    ZPoint[] linePD = l.toLinePD();
                    for (int i = 0; i < ls.getCoordinates().length - 1; i++) {
                        ZLine polySeg = new ZLine(
                                ls.getCoordinateN(i),
                                ls.getCoordinateN(i + 1)
                        );
                        double cos = l.angleCosWith(polySeg);
                        if (cos <= 0.5) {
                            // 大于60度，才取相交 否则舍弃
                            ZPoint[] polySegPD = polySeg.toLinePD();
                            ZPoint intersect = null;

                            ZPoint delta = polySegPD[0].sub(linePD[0]);
                            double crossBase = linePD[1].cross2D(polySegPD[1]);
                            double crossDelta0 = delta.cross2D(linePD[1]);
                            double crossDelta1 = delta.cross2D(polySegPD[1]);

                            if (Math.abs(crossBase) >= 0.00000001) {
                                double s = crossDelta1 / crossBase; // seg
                                double t = crossDelta0 / crossBase; // polySeg
                                if (s >= 0 && s <= 1 && t >= 0 && t < 1) {
                                    intersect = polySegPD[0].add(polySegPD[1].scaleTo(t));
                                }
                            }
                            if (intersect != null) {
                                // 大于75度 直接取相交点 若60~75则另做记录
                                if (cos > 0.26) {
                                    turning.add(intersect);
                                }
                                intersectGrid.put(intersect, l);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("valid intersection points: " + intersectGrid.size());
        return intersectGrid;
    }

    /* ------------- clear ------------- */

    public void disposeSplit() {
        this.publicBlock = null;
        this.shopBlocks = null;
    }

    public void disposeSubdivision() {
        this.allShops = new ArrayList<>();
    }

    public void disposeEvacuation() {
        this.evacuationPoint = null;
    }

    /* ------------- setter & getter ------------- */

    public void setFloorNum(int floorNum) {
        this.floorNum = floorNum;
    }

    public void setBoundary(Polygon boundary) {
        this.boundary = boundary;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAllShops(List<Shop> allShops) {
        this.allShops = allShops;
    }

    public int getStatus() {
        return status;
    }

//    public TrafficGraph getGraph() {
//        return graph;
//    }

    public List<Polygon> getShopBlocks() {
        return shopBlocks;
    }

    public List<List<WB_Coord>> getBufferControlPoints() {
        return bufferControlPoints;
    }

    public List<Shop> getAllShops() {
        return allShops;
    }

    public List<LineString> getAllSubLines() {
        return allSubLines;
    }

    public List<WB_Point> getEvacuationPoint() {
        return evacuationPoint;
    }

    public void setVerify(Point verify) {
        this.verify = verify;
    }

    public Point getVerify() {
        return verify;
    }

    /* ------------- draw ------------- */

}
