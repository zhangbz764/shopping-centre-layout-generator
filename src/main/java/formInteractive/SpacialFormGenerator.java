package formInteractive;

import formInteractive.blockSplit.Split;
import formInteractive.blockSplit.SplitBisector;
import formInteractive.graphAdjusting.TrafficGraph;
import formInteractive.graphAdjusting.TrafficNode;
import formInteractive.graphAdjusting.TrafficNodeFixed;
import formInteractive.graphAdjusting.TrafficNodeTree;
import geometry.ZSkeleton;
import processing.core.PApplet;
import render.JtsRender;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 8:30
 * @description main control of public space generator
 */
public class SpacialFormGenerator {
    // input file absolute path from main class
    private final String filePath;
    // geometry input 3dm file
    private final InputData input;

    // traffic mini spanning tree
    private TrafficGraph graph;

    // split block, could be variable types
    private Split splitBlock;

    // output
    private WB_Polygon publicBlock;
    private List<WB_Polygon> shopBlock;
    private List<ZSkeleton> skeletons;
    private Map<ZSkeleton, WB_Polygon> blockSkeletonMap;

    /* ------------- constructor ------------- */

    public SpacialFormGenerator(String filePath) {
        System.out.println("* GENERATING PUBLIC SPACE *");
        this.filePath = filePath;
        this.input = new InputData();

        init();
    }

    /* ------------- initialize & get (public) ------------- */

    /**
     * @return void
     * @description initialize generator with input file & tree graph
     */
    public void init() {
        // catch input data
        this.input.loadData(filePath);
        System.out.println("** ADJUSTING TRAFFIC GRAPH AND SPLITTING BLOCKS **");

        // compute traffic mini spanning tree, nodes input from input data
        List<TrafficNode> innerNodes = new ArrayList<>();
        for (WB_Point p : input.getInputInnerNodes()) {
            innerNodes.add(new TrafficNodeTree(p, input.getInputBoundary()));
        }
        List<TrafficNode> entryNodes = new ArrayList<>();
        for (WB_Point p : input.getInputEntries()) {
            entryNodes.add(new TrafficNodeFixed(p, input.getInputBoundary()));
        }
        this.graph = new TrafficGraph(innerNodes, entryNodes);

        // compute split block
        this.splitBlock = new SplitBisector(input.getInputBoundary(), graph);

        // get output
        catchOutput();
    }

    /**
     * @return void
     * @description catch output from Split and perform skeleton
     */
    private void catchOutput() {
        this.publicBlock = splitBlock.getPublicBlockPoly();
        this.shopBlock = splitBlock.getShopBlockPolys();
        // compute straight skeleton for each shop block
        this.skeletons = new ArrayList<>();

        this.blockSkeletonMap = new HashMap<>();
        for (WB_Polygon polygon : shopBlock) {
            ZSkeleton skeleton = new ZSkeleton(polygon);
            skeletons.add(skeleton);
            blockSkeletonMap.put(skeleton, polygon);
        }
    }

    public WB_Polygon getPublicBlock() {
        return this.publicBlock;
    }

    public List<WB_Polygon> getShopBlock() {
        return this.shopBlock;
    }

    public int getShopBlockNum() {
        return this.splitBlock.getShopBlockNum();
    }

    public Map<ZSkeleton, WB_Polygon> getBlockSkeletonMap() {
        return this.blockSkeletonMap;
    }

    /* ------------- mouse & key interaction at TRAFFIC GRAPH STEP ------------- */

    /**
     * @return void
     * @description update tree node location and graph, split polygon
     */
    public void mouseDrag(PApplet app) {
        graph.dragTreeNode(app);
        graph.dragFixedNode(app);
        if (graph.update) {
            splitBlock.init(graph);
            catchOutput();
            graph.update = false;
        }
    }

    /**
     * @return void
     * @description reset fixed node to not active
     */
    public void mouseRelease(PApplet app) {
        graph.resetActive(app);
    }

    /**
     * @return void
     * @description all keyboard interaction
     */
    public void keyInteract(PApplet app) {
        // reload input file
        if (app.key == 'r' || app.key == 'R') {
            init();
        }
        // add a TrafficNode to graph
        if (app.key == 'a' || app.key == 'A') {
            graph.addTreeNode(app, input.getInputBoundary());
            splitBlock.init(graph);
            catchOutput();
            graph.update = false;
        }
        // remove a TrafficNode to graph (mouse location)
        if (app.key == 's' || app.key == 'S') {
            graph.removeTreeNode(app);
            splitBlock.init(graph);
            catchOutput();
            graph.update = false;
        }
        // add a fixed TrafficNode to graph
        if (app.key == 'q' || app.key == 'Q') {
            graph.addFixedNode(app, input.getInputBoundary());
            splitBlock.init(graph);
            catchOutput();
            graph.update = false;
        }
        // remove a fixed TrafficNode to graph (mouse location)
        if (app.key == 'w' || app.key == 'W') {
            graph.removeFixedNode(app);
            splitBlock.init(graph);
            catchOutput();
            graph.update = false;
        }
        // increase TrafficNode's regionR
        if (app.key == 'z' || app.key == 'Z') {
            graph.increaseR(app, 2);
            splitBlock.init(graph);
            catchOutput();
            graph.update = false;
        }
        // decrease TrafficNode's regionR
        if (app.key == 'x' || app.key == 'X') {
            graph.decreaseR(app, -2);
            splitBlock.init(graph);
            catchOutput();
            graph.update = false;
        }
    }

    /* ------------- draw ------------- */

    public void display(JtsRender jrender, WB_Render3D render, PApplet app) {
        displayInputData(render, app);
        displayBlock(jrender, app);
        displayGraph(app);
        displaySkeleton(app);
    }

    private void displayInputData(WB_Render3D render, PApplet app) {
        input.display(render, app);
    }

    private void displayGraph(PApplet app) {
        graph.display(app);
    }

    private void displayBlock(JtsRender render, PApplet app) {
        splitBlock.display(render, app);
    }

    private void displaySkeleton(PApplet app) {
        for (ZSkeleton skeleton : skeletons) {
            skeleton.display(app);
        }
    }
}
