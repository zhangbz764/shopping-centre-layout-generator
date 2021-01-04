package formInteractive.blockSplit;

import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import geometry.ZEdge;
import geometry.ZLine;
import geometry.ZPoint;
import math.ZGeoMath;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import processing.core.PApplet;
import render.JtsRender;
import transform.ZTransform;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * split boundary by bisector joints
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/19
 * @time 14:56
 */
public class SplitBisector implements Split {
    // boundary convert to jts LineString
    private final LineString boundaryLineString;
    // select joint connection for each edge
    private List<ZLine> connectedLinesToSplit;

    // output
    private Polygon publicBlockPoly; // 交通区域多边形
    private List<Polygon> shopBlockPolys; // 店铺区域多边形
    private int shopBlockNum; // 店铺区域数量

    /* ------------- constructor & initialize ------------- */

    public SplitBisector(WB_Polygon boundary, TrafficGraph graph) {
        // initialize input data
        this.boundaryLineString = ZTransform.WB_PolyLineToJtsLineString(boundary);
        setNodeJoints(graph);
        // connect joints
        this.connectedLinesToSplit = connectJoints(boundary, graph);
        // get split result
        performSplit(boundaryLineString, connectedLinesToSplit, graph.getTreeNodes().get(0));
        shopBlockNum = shopBlockPolys.size();
        System.out.println("---> traffic space block num: " + 1 +
                "\n" + "---> shop space block num: " + shopBlockNum);
    }

    /* ------------- initialize & get (public) ------------- */

    public void init(WB_Polygon boundary, TrafficGraph graph) {
        // refresh input data
        setNodeJoints(graph);
        // refresh joints connection
        this.connectedLinesToSplit = connectJoints(boundary, graph);
        // refresh split result
        performSplit(boundaryLineString, connectedLinesToSplit, graph.getTreeNodes().get(0));
        // print if number change
        if (shopBlockNum != shopBlockPolys.size()) {
            shopBlockNum = shopBlockPolys.size();
            System.out.println("---> shop space block num: " + shopBlockNum);
        }
    }

    @Override
    public WB_Polygon getPublicBlockPoly() {
        if (this.publicBlockPoly != null) {
            return ZTransform.jtsPolygonToWB_Polygon(this.publicBlockPoly);
        } else {
            throw new NullPointerException("can't find public space block polygon");
        }
    }

    @Override
    public List<WB_Polygon> getShopBlockPolys() {
        if (this.shopBlockPolys != null && this.shopBlockPolys.size() != 0) {
            List<WB_Polygon> result = new ArrayList<>();
            for (Polygon p : shopBlockPolys) {
                result.add(ZTransform.jtsPolygonToWB_Polygon(p));
            }
            return result;
        } else {
            throw new NullPointerException("can't find shop space block polygons");
        }
    }

    @Override
    public int getShopBlockNum() {
        return this.shopBlockNum;
    }

    /*-------- print & draw --------*/

    @Override
    public void display(JtsRender jtsRender, PApplet app) {
        app.pushStyle();
        app.fill(255);
        jtsRender.drawGeometry(publicBlockPoly);
        for (Polygon p : shopBlockPolys) {
            app.fill(220);
            jtsRender.drawGeometry(p);
        }
        app.popStyle();
    }

    /*-------- perform split --------*/

    /**
     * @return java.util.Collection<org.locationtech.jts.geom.Polygon>
     * @description split to polygons, input a boundary LineString ang multiple inner LineStrings
     */
    public void performSplit(LineString outer, List<ZLine> inner, ZPoint verify) {
        Polygonizer pr = new Polygonizer();
        Geometry nodedLineStrings = outer;
        for (ZLine e : inner) {
            nodedLineStrings = nodedLineStrings.union(e.toJtsLineString());
        }
        pr.add(nodedLineStrings);
        Collection<Polygon> allPolys = pr.getPolygons();
        // find which is public and which are stores
        for (Polygon p : allPolys) {
            if (p.contains(verify.toJtsPoint())) {
                this.publicBlockPoly = p;
                break;
            }
        }
        allPolys.remove(publicBlockPoly);
        this.shopBlockPolys = (List<Polygon>) allPolys;
    }

    /**
     * @return void
     * @description initialize node's joints
     */
    private void setNodeJoints(TrafficGraph graph) {
        // set treeNodes joint point
        for (TrafficNode n : graph.getTreeNodes()) {
            n.setJoints();
        }
        // set fixedNodes joint point
        for (TrafficNode n : graph.getFixedNodes()) {
            n.setJoints();
        }
    }

    /**
     * @return generalTools.ZPoint[]
     * @description fine joint which need to be connected (0 -> positive, 1 -> negative)
     */
    private ZPoint[] selectJoint(TrafficNode curr, ZPoint lineDir) {
        List<ZPoint> joints = curr.getJoints();

        if (joints.size() == 1) { // 如果只有一个点，两侧要连的点为同一个
            return new ZPoint[]{joints.get(0), joints.get(0)};
        } else if (joints.size() >= 2) { // 需找到两侧与edge夹角最小的
            List<ZPoint> pos = new ArrayList<>();  // 叉积为正
            List<ZPoint> neg = new ArrayList<>();  // 叉积为负
            for (ZPoint j : joints) {
                ZPoint currToJoint = j.sub(curr); // 向量
                if (currToJoint.cross2D(lineDir) > 0) {
                    pos.add(currToJoint);
                } else if (currToJoint.cross2D(lineDir) < 0) {
                    neg.add(currToJoint);
                }
            }

            // 判断fixed node的连接点是否在同侧，不同侧则重新连线到两点之间
            if ((pos.size() == 0 || neg.size() == 0) && curr.getNodeType().equals("TrafficNodeFixed") && joints.size() == 2) {
                ZPoint newLineDir = joints.get(0).centerWith(joints.get(1)).sub(curr);
                if (newLineDir.dot2D(lineDir) < 0) {
                    newLineDir.scaleSelf(-1);
                }
                return selectJoint(curr, newLineDir);
            } else {
                ZPoint closet_pos_v = ZGeoMath.findClosetVec(lineDir, pos);
                ZPoint closet_neg_v = ZGeoMath.findClosetVec(lineDir, neg);
                return new ZPoint[]{curr.add(closet_pos_v), curr.add(closet_neg_v)};
            }
        } else {
            return null;
        }
    }

    /**
     * @return java.util.List<geometry.ZLine>
     * @description get all connect lines
     */
    private List<ZLine> connectJoints(WB_Polygon boundary, TrafficGraph graph) {
        List<ZLine> connections = new ArrayList<>();
        // connect joints from tree edges
        for (ZEdge edge : graph.getTreeEdges()) {
            TrafficNode start = (TrafficNode) edge.getStart();
            TrafficNode end = (TrafficNode) edge.getEnd();

            ZPoint[] startSelect = selectJoint(start, end.sub(start));
            ZPoint[] endSelect = selectJoint(end, start.sub(end));

            if (startSelect != null && endSelect != null) {
                connections.add(new ZLine(startSelect[0], endSelect[1]));
                connections.add(new ZLine(startSelect[1], endSelect[0]));
            }

            // add cap if tree node is an dead end
            if (graph.getFixedNodes().size() >= 1) {
                if (start.getLinkedEdgeNum() == 1) {
                    ZLine cap = new ZLine(start.getJoints().get(0), start.getJoints().get(1));
                    connections.add(cap);
                }
                if (end.getLinkedEdgeNum() == 1) {
                    ZLine cap = new ZLine(end.getJoints().get(0), end.getJoints().get(1));
                    connections.add(cap);
                }
            } else {
                if (start.getNeighbors().size() == 1) {
                    ZLine cap = new ZLine(start.getJoints().get(0), start.getJoints().get(1));
                    connections.add(cap);
                }
                if (end.getNeighbors().size() == 1) {
                    ZLine cap = new ZLine(end.getJoints().get(0), end.getJoints().get(1));
                    connections.add(cap);
                }
            }
        }
        // connect joints from fixed edges (scale 1.1 for better intersection)
        for (ZEdge edge : graph.getFixedEdges()) {
            TrafficNode start = (TrafficNode) edge.getStart();
            TrafficNode end = (TrafficNode) edge.getEnd();

            ZPoint[] startSelect = selectJoint(start, end.sub(start));
            ZPoint[] endSelect = selectJoint(end, start.sub(end));

            if (startSelect != null && endSelect != null) {
                connections.add(new ZLine(startSelect[0], endSelect[1]).scaleTo(1.1));
                connections.add(new ZLine(startSelect[1], endSelect[0]).scaleTo(1.1));
            }
        }

        // add atrium offset edges
        for (TrafficNode treeNode : graph.getTreeNodes()) {
            if (treeNode.getAtrium() != null) {
                connections.addAll(treeNode.getAtrium().getOffsetSegmentsFromAtrium());
            }
        }

        return connections;
    }
}