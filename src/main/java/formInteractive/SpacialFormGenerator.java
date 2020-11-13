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
import wblut.geom.WB_PolyLine;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 8:30
 * @description main control of public space generator
 */
public class SpacialFormGenerator {
    // geometry input 3dm file
    private InputData input;

    // traffic mini spanning tree
    private TrafficGraph mainGraph;

    // split block, could be variable types
    private Split blockSplit;

    // output
    private WB_Polygon publicBlock;
    private List<WB_Polygon> shopBlock;
    private List<ZSkeleton> skeletons;

    /* ------------- constructor ------------- */

    public SpacialFormGenerator(InputData input) {
        System.out.println("* GENERATING PUBLIC SPACE *");

        init(input);
    }

    /* ------------- initialize & get (public) ------------- */

    /**
     * @return void
     * @description initialize generator with input file & tree graph
     */
    public void init(InputData input) {
        // catch input data
        this.input = input;
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
        this.mainGraph = new TrafficGraph(innerNodes, entryNodes);

        // compute split block
        this.blockSplit = new SplitBisector(input.getInputBoundary(), mainGraph);

        // get output
        catchOutput();
    }

    /**
     * @return void
     * @description catch output from Split and perform skeleton
     */
    private void catchOutput() {
        this.publicBlock = blockSplit.getPublicBlockPoly();
        this.shopBlock = blockSplit.getShopBlockPolys();
        // compute straight skeleton for each shop block
        this.skeletons = new ArrayList<>();

        for (WB_Polygon polygon : shopBlock) {
            ZSkeleton skeleton = new ZSkeleton(polygon);
            skeletons.add(skeleton);
        }
    }

    public WB_Polygon getPublicBlock() {
        return this.publicBlock;
    }

    public List<WB_Polygon> getShopBlock() {
        return this.shopBlock;
    }

    public int getShopBlockNum() {
        return this.blockSplit.getShopBlockNum();
    }

    public List<ZSkeleton> getSkeletons() {
        return this.skeletons;
    }

    /* ------------- mouse & key interaction at TRAFFIC GRAPH STEP ------------- */

    /**
     * @return void
     * @description update tree node location and graph, split polygon
     */
    public void dragUpdate(int pointerX, int pointerY) {
        mainGraph.setTreeNode(pointerX, pointerY);
        mainGraph.setFixedNode(pointerX, pointerY);
        if (mainGraph.update) {
            blockSplit.init(mainGraph);
            catchOutput();
            mainGraph.update = false;
        }
    }

    /**
     * @return void
     * @description reset fixed node to not active
     */
    public void releaseUpdate() {
        mainGraph.resetActive();
    }

    /**
     * @return void
     * @description all keyboard interaction
     */
    public void keyUpdate(int pointerX, int pointerY, PApplet app) {
        // add a TrafficNode to graph
        if (app.key == 'a' || app.key == 'A') {
            mainGraph.addTreeNode(pointerX, pointerY, input.getInputBoundary());
            blockSplit.init(mainGraph);
            catchOutput();
            mainGraph.update = false;
        }
        // remove a TrafficNode to graph (mouse location)
        if (app.key == 's' || app.key == 'S') {
            mainGraph.removeTreeNode(pointerX, pointerY);
            blockSplit.init(mainGraph);
            catchOutput();
            mainGraph.update = false;
        }
        // add a fixed TrafficNode to graph
        if (app.key == 'q' || app.key == 'Q') {
            mainGraph.addFixedNode(pointerX, pointerY, input.getInputBoundary());
            blockSplit.init(mainGraph);
            catchOutput();
            mainGraph.update = false;
        }
        // remove a fixed TrafficNode to graph (mouse location)
        if (app.key == 'w' || app.key == 'W') {
            mainGraph.removeFixedNode(pointerX, pointerY);
            blockSplit.init(mainGraph);
            catchOutput();
            mainGraph.update = false;
        }
        // increase TrafficNode's regionR
        if (app.key == 'z' || app.key == 'Z') {
            mainGraph.changeR(pointerX, pointerY, 2);
            blockSplit.init(mainGraph);
            catchOutput();
            mainGraph.update = false;
        }
        // decrease TrafficNode's regionR
        if (app.key == 'x' || app.key == 'X') {
            mainGraph.changeR(pointerX, pointerY, -2);
            blockSplit.init(mainGraph);
            catchOutput();
            mainGraph.update = false;
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
        mainGraph.display(app);
    }

    private void displayBlock(JtsRender render, PApplet app) {
        blockSplit.display(render, app);
    }

    private void displaySkeleton(PApplet app) {
        for (ZSkeleton skeleton : skeletons) {
            skeleton.display(app);
        }
    }
}
