package oldVersion;

import mallIO.ImportData;
import oldVersion.mallElements.TrafficGraph;
import oldVersion.mallElements.TrafficNode;
import oldVersion.mallElements.TrafficNodeFixed;
import oldVersion.mallElements.TrafficNodeTree;
import processing.core.PApplet;
import render.JtsRender;
import wblut.geom.WB_Point;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * main control of public space spacial form
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 8:30
 */
public class SpacialFormGenerator {
    // geometry input 3dm file
    private ImportData input;

    // traffic mini spanning tree
    private TrafficGraph mainGraph;
    private TrafficGraph floorGraph;

    /* ------------- constructor ------------- */

    public SpacialFormGenerator(ImportData input) {
        System.out.println("* GENERATING PUBLIC SPACE *");

        init(input);
    }

    /* ------------- initialize & get (public) ------------- */

    /**
     * initialize generator with input file & tree graph
     *
     * @param input input geometry
     * @return void
     */
    public void init(ImportData input) {
        // catch input data
        this.input = input;
        System.out.println("** ADJUSTING TRAFFIC GRAPH AND SPLITTING BLOCKS **");

        // compute traffic mini spanning tree, nodes input from input data
        List<TrafficNode> innerNodes = new ArrayList<>(); // main graph
        List<TrafficNode> innerNodes2 = new ArrayList<>(); // floor graph
        for (WB_Point p : this.input.getInputInnerNodes()) {
            TrafficNode treeNode1 = new TrafficNodeTree(p, this.input.getInputBoundary());
            TrafficNode treeNode2 = new TrafficNodeTree(p, this.input.getInputBoundary());
            treeNode1.setRegionR(MallConstant.MAIN_TRAFFIC_WIDTH * 0.5 * MallConstant.SCALE);
            treeNode2.setRegionR(MallConstant.MAIN_TRAFFIC_WIDTH * 0.5 * MallConstant.SCALE);
            innerNodes.add(treeNode1);
            innerNodes2.add(treeNode2);
        }
        List<TrafficNode> entryNodes = new ArrayList<>();
        for (WB_Point p : this.input.getInputEntries()) {
            TrafficNode fixedNode = new TrafficNodeFixed(p, this.input.getInputBoundary());
            fixedNode.setRegionR(MallConstant.MAIN_TRAFFIC_WIDTH * 0.5 * MallConstant.SCALE);
            entryNodes.add(fixedNode);
        }
        this.mainGraph = new TrafficGraph(innerNodes, entryNodes);
        this.floorGraph = new TrafficGraph(innerNodes2, new ArrayList<TrafficNode>());
    }

    public void setGraphSwitch(boolean update) {
        this.mainGraph.update = update;
        this.floorGraph.update = update;
    }

    public TrafficGraph getMainGraph() {
        return mainGraph;
    }

    public TrafficGraph getFloorGraph() {
        return floorGraph;
    }

    /* ------------- mouse & key interaction at TRAFFIC GRAPH STEP ------------- */

    /**
     * update tree node location and graph, split polygon
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public void dragUpdate(int pointerX, int pointerY) {
        mainGraph.setTreeNode(pointerX, pointerY);
        mainGraph.setFixedNode(pointerX, pointerY);
        floorGraph.setTreeNode(pointerX, pointerY);
        floorGraph.setAtrium();
    }

    /**
     * reset fixed node to not active
     *
     * @param
     * @return void
     */
    public void releaseUpdate() {
        mainGraph.resetActive();
        floorGraph.resetActive();
    }

    /**
     * all keyboard interaction
     *
     * @param pointerX x
     * @param pointerY y
     * @param app      PApplet
     * @return void
     */
    public void keyUpdate(int pointerX, int pointerY, PApplet app) {
        // add a TrafficNode to graph
        if (app.key == 'a' || app.key == 'A') {
            mainGraph.addTreeNode(pointerX, pointerY, input.getInputBoundary());
            floorGraph.addTreeNode(pointerX, pointerY, input.getInputBoundary());
        }
        // remove a TrafficNode to graph (mouse location)
        if (app.key == 's' || app.key == 'S') {
            mainGraph.removeTreeNode(pointerX, pointerY);
            floorGraph.removeTreeNode(pointerX, pointerY);
        }
        // add a fixed TrafficNode to graph
        if (app.key == 'q' || app.key == 'Q') {
            mainGraph.addFixedNode(pointerX, pointerY, input.getInputBoundary());
        }
        // remove a fixed TrafficNode to graph (mouse location)
        if (app.key == 'w' || app.key == 'W') {
            mainGraph.removeFixedNode(pointerX, pointerY);
        }
        // increase TrafficNode's regionR
        if (app.key == 'z' || app.key == 'Z') {
            mainGraph.changeR(pointerX, pointerY, 2);
            floorGraph.changeR(pointerX, pointerY, 2);
        }
        // decrease TrafficNode's regionR
        if (app.key == 'x' || app.key == 'X') {
            mainGraph.changeR(pointerX, pointerY, -2);
            floorGraph.changeR(pointerX, pointerY, -2);
        }
        // add an atrium to treeNode
        if (app.key == 'e' || app.key == 'E') {
            floorGraph.addOrRemoveAtrium(pointerX, pointerY);
        }
        // add an atrium to treeNode
        if (app.key == 'f' || app.key == 'F') {
            floorGraph.addOrRemoveAtriumEscalator();
        }
        // increase atrium's length along edge
        if (app.key == 'j' || app.key == 'J') {
            floorGraph.updateSelectedAtriumLength(1);
        }
        // decrease atrium's length along edge
        if (app.key == 'k' || app.key == 'K') {
            floorGraph.updateSelectedAtriumLength(-1);
        }
        // increase atrium's width perpendicular to linked edge
        if (app.key == 'u' || app.key == 'U') {
            floorGraph.updateSelectedAtriumWidth(1);
        }
        // decrease atrium's width perpendicular to linked edge
        if (app.key == 'i' || app.key == 'I') {
            floorGraph.updateSelectedAtriumWidth(-1);
        }
    }

    /**
     * start editing atrium
     *
     * @param pointerX x
     * @param pointerY y
     * @return void
     */
    public void atriumEdit(int pointerX, int pointerY) {
        floorGraph.chooseAtrium(pointerX, pointerY);
    }

    /**
     * atrium editing end
     *
     * @param
     * @return void
     */
    public void atriumEditEnd() {
        floorGraph.clearSelectAtrium();
    }

    /* ------------- draw ------------- */

    public void display(JtsRender jtsRender, WB_Render render, PApplet app) {
        displayInputData(render, app);
        displayGraph(render, app);
    }

    private void displayInputData(WB_Render render, PApplet app) {
        input.display(render, app);
    }

    private void displayGraph(WB_Render render, PApplet app) {
        mainGraph.display(render, app);
    }

}
