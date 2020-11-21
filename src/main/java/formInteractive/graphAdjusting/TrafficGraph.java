package formInteractive.graphAdjusting;

import geometry.ZEdge;
import math.ZMath;
import org.locationtech.jts.geom.LineString;
import processing.core.PApplet;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;


/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/11
 * @time 14:59
 * @description generate minimum spanning tree from a series of input ZNodes
 */
public class TrafficGraph {
    private static final float MAXVALUE = Float.MAX_VALUE;
    public boolean update = false;

    // adjacency matrix
    private double[][] matrix;
    // nodes and edges
    private final List<TrafficNode> fixedNodes, treeNodes;
    private List<ZEdge> fixedEdges, treeEdges;

    /* ------------- constructor ------------- */

    public TrafficGraph() {
        this.treeNodes = new ArrayList<>();
        this.fixedNodes = new ArrayList<>();
        this.treeEdges = new ArrayList<>();
        this.fixedEdges = new ArrayList<>();
    }

    public TrafficGraph(List<TrafficNode> treeNodes, List<TrafficNode> fixedNodes) {
        this.treeNodes = treeNodes;
        this.fixedNodes = fixedNodes;
        init();
    }

    public TrafficGraph(List<TrafficNode> treeNodes) {
        this.treeNodes = treeNodes;
        this.fixedNodes = new ArrayList<>();
        init();
    }

    /* ------------- initialize & get (public) ------------- */

    /**
     * @return void
     * @description initialize adjacency matrix and tree, including fixed nodes if necessary
     */
    public void init() {
        update = true; // changed
        if (treeNodes.size() > 0) {
            if (fixedNodes.size() > 0) {
                getFixedLink();
            } else {
                this.fixedEdges = new ArrayList<>();
            }
            if (treeNodes.size() > 1) {
                matrixInit();
                getTree();
            } else {
                this.treeEdges = new ArrayList<>();
            }
        } else {
            this.treeEdges = new ArrayList<>();
            this.fixedEdges = new ArrayList<>();
        }
        setRelations();
    }

    public List<TrafficNode> getTreeNodes() {
        return treeNodes;
    }

    public List<TrafficNode> getFixedNodes() {
        return fixedNodes;
    }

    public List<ZEdge> getTreeEdges() {
        return treeEdges;
    }

    public List<ZEdge> getFixedEdges() {
        return fixedEdges;
    }

    public void clearFixed() {
        for (TrafficNode fixed : fixedNodes) {
            fixed.getNeighbor().get(0).removeNeighbor(fixed);
        }
        this.fixedNodes.clear();
        this.fixedEdges.clear();
    }

    /**
     * @return java.util.List<org.locationtech.jts.geom.LineString>
     * @description transform all edges to a list of LineStrings
     */
    public List<LineString> toLineStrings() {
        List<LineString> ls = new ArrayList<>();
        for (ZEdge te : treeEdges) {
            ls.add(te.toJtsLineString());
        }
        for (ZEdge fe : fixedEdges) {
            ls.add(fe.toJtsLineString());
        }
        return ls;
    }

    /* ------------- move, add & remove (public) ------------- */

    public void resetActive() {
        for (TrafficNode n : fixedNodes) {
            n.setActivate(false);
        }
        for (TrafficNode n : treeNodes) {
            n.setActivate(false);
        }
    }

    public void setTreeNode(int pointerX, int pointerY) {
        for (TrafficNode n : treeNodes) {
            if (n.isMoused(pointerX, pointerY)) {
                n.setActivate(true);
                n.setLastPosition(n.x(), n.y(), n.z());
                n.setByRestriction(pointerX, pointerY);
                init();
                break;
            }
            if (n.isActivate()) {
                n.setLastPosition(n.x(), n.y(), n.z());
                n.setByRestriction(pointerX, pointerY);
                init();
                break;
            }
        }
    }

    public void addAtrium(int pointerX, int pointerY) {
        for (TrafficNode n : treeNodes) {
            if (n.isMoused(pointerX, pointerY)) {
                n.setAtrium();
            }
        }
    }

    public void setAtrium() {
        for (TrafficNode n : treeNodes) {
            if (n.getAtrium() != null) {
                n.setAtrium();
            }
        }
    }

    public void setFixedNode(int pointerX, int pointerY) {
        for (TrafficNode n : fixedNodes) {
            if (n.isMoused(pointerX, pointerY)) {
                n.setActivate(true);
                n.setLastPosition(n.x(), n.y(), n.z());
                n.setByRestriction(pointerX, pointerY);
                init();
                break;
            }
            if (n.isActivate()) {
                n.setLastPosition(n.x(), n.y(), n.z());
                n.setByRestriction(pointerX, pointerY);
                init();
                break;
            }
        }
    }

    public void addTreeNode(int pointerX, int pointerY, WB_Polygon boundary) {
        TrafficNodeTree tree = new TrafficNodeTree(pointerX, pointerY, boundary);
        if (WB_GeometryOp.contains2D(tree.toWB_Point(), boundary) && WB_GeometryOp.getDistance2D(tree.toWB_Point(), boundary) > tree.getRegionR()) {
            treeNodes.add(tree);
            init();
        }

    }

    public void removeTreeNode(int pointerX, int pointerY) {
        for (int i = 0; i < treeNodes.size(); i++) {
            if (treeNodes.get(i).isMoused(pointerX, pointerY)) {
                treeNodes.remove(i--);
                init();
            }
        }
    }

    public void addFixedNode(int pointerX, int pointerY, WB_Polygon boundary) {
        TrafficNodeFixed fixed = new TrafficNodeFixed(pointerX, pointerY, boundary);
        fixed.setByRestriction(pointerX, pointerY);
        fixedNodes.add(fixed);
        init();
    }

    public void removeFixedNode(int pointerX, int pointerY) {
        for (int i = 0; i < fixedNodes.size(); i++) {
            if (fixedNodes.get(i).isMoused(pointerX, pointerY)) {
                fixedNodes.remove(i--);
                init();
            }
        }
    }

    public void changeR(int pointerX, int pointerY, double r) {
        for (TrafficNode fixedNode : fixedNodes) {
            if (fixedNode.isMoused(pointerX, pointerY)) {
                fixedNode.updateRegionR(r);
                init();
            }
        }
        for (TrafficNode treeNode : treeNodes) {
            if (treeNode.isMoused(pointerX, pointerY)) {
                treeNode.updateRegionR(r);
                init();
            }
        }
    }

    /* ------------- draw -------------*/

    /**
     * @return void
     * @description draw all nodes and edges
     */
    public void display(WB_Render3D render, PApplet app) {
        app.pushStyle();
        app.pushStyle();
        app.stroke(24, 169, 222);
        app.strokeWeight(2);
        for (ZEdge e : treeEdges) {
            e.display(app);
        }

        for (ZEdge e : fixedEdges) {
            e.display(app);
        }
        app.popStyle();
        app.fill(255,97,136);
        for (TrafficNode n : treeNodes) {
            n.displayAsPoint(app);
//            n.displayJoint(app, 5);
            n.displayAtrium(render, app);
        }
        app.fill(128);
        for (TrafficNode n : fixedNodes) {
            n.displayAsPoint(app);
//            n.displayJoint(app, 5);
        }
//        displayNeighbor(app);
        app.popStyle();
    }

    /**
     * @return void
     * @description draw neighbor nodes of one node where mouse at
     */
    public void displayNeighbor(int pointerX, int pointerY, PApplet app) {
        for (TrafficNode n : treeNodes) {
            if (n.isMoused(pointerX, pointerY)) {
                n.displayNeighbor(app);
            }
        }
    }

    /* ------------- compute the graph as minimum spanning tree (private) ------------- */

    /**
     * @return void
     * @description initialize adjacency matrix of all nodes
     */
    private void matrixInit() {
        this.matrix = new double[treeNodes.size()][treeNodes.size()];
        for (int i = 0; i < treeNodes.size(); i++) {
            for (int j = i; j < treeNodes.size(); j++) {
                if (i == j) {
                    matrix[i][j] = -1;
                } else {
                    matrix[i][j] = matrix[j][i] = treeNodes.get(i).distance(treeNodes.get(j));
                }
            }
        }
    }

    /**
     * @return void
     * @description compute minimum spanning tree
     */
    private void getTree() {
        treeEdges = new ArrayList<>();
        // list to count each node
        List<Integer> list = new ArrayList<>();
        list.add(0);

        int begin = 0;
        int end = 0;
        float weight;

        // final tree indices (n-1)
        int[] parent = new int[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            parent[i] = -1;
        }

        while (list.size() < matrix.length) {
            weight = MAXVALUE;
            for (Integer row : list) {
                for (int i = 0; i < matrix.length; i++) {
                    if (!list.contains(i)) {
                        if (i >= row + 1) {
                            if (matrix[row][i] >= 0 && matrix[row][i] < weight) {
                                begin = row;
                                end = i;
                                weight = (float) matrix[row][i];
                            }
                        } else if (i <= row - 1) {
                            if (matrix[i][row] >= 0 && matrix[i][row] < weight) {
                                begin = row;
                                end = i;
                                weight = (float) matrix[i][row];
                            }
                        }
                    }
                }
            }
            list.add(end);
            parent[end] = begin;
        }

        // add to ArrayList
        for (int i = 1; i < parent.length; i++) {
            ZEdge edge = new ZEdge(treeNodes.get(i), treeNodes.get(parent[i]));
            treeEdges.add(edge);
        }
    }

    /**
     * @return void
     * @description add edges from entries to their nearest node
     */
    private void getFixedLink() {
        fixedEdges = new ArrayList<>();
        for (TrafficNode f : fixedNodes) {
            ZEdge edge = new ZEdge(getNearestNode(f, treeNodes), f);
            fixedEdges.add(edge);
        }
    }

    /**
     * @return publicSpace.TreeNode
     * @description find the nearest node
     */
    private TrafficNode getNearestNode(TrafficNode curr, List<TrafficNode> other) {
        double[] dist = new double[other.size()];
        for (int i = 0; i < other.size(); i++) {
            dist[i] = curr.distance(other.get(i));
        }
        int nearestIndex = ZMath.getMinIndex(dist);
        return other.get(nearestIndex);
    }

    /**
     * @return void
     * @description set node relations and join point on each bisector
     */
    private void setRelations() {
        // clear current node relationship
        for (TrafficNode n : treeNodes) {
            n.setRelationReady();
        }
        for (TrafficNode n : fixedNodes) {
            n.setRelationReady();
        }

        // set relations
        for (ZEdge e : treeEdges) {
            e.getStart().setNeighbor(e.getEnd());
            e.getEnd().setNeighbor(e.getStart());

            e.getStart().setLinkedEdge(e);
            e.getEnd().setLinkedEdge(e);
        }
        for (ZEdge e : fixedEdges) {
            e.getStart().setNeighbor(e.getEnd());
            e.getEnd().setNeighbor(e.getStart());

            e.getStart().setLinkedEdge(e);
            e.getEnd().setLinkedEdge(e);
        }
    }
}
