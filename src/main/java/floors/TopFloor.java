package floors;

import formInteractive.graphAdjusting.TrafficGraph;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:50
 * @description top floor of a shopping mall
 */
public class TopFloor extends Floor {
    private TrafficGraph topFloorGraph;
    private final int floorNum;

    /* ------------- constructor ------------- */

    public TopFloor(int floorNum) {
        this.floorNum = floorNum;
    }

    /* ------------- set & get ------------- */

    @Override
    public void setTrafficGraph(TrafficGraph mainGraph) {
        this.topFloorGraph = mainGraph;
    }

    @Override
    public int getShopBlockNum() {
        return 0;
    }

    @Override
    public int getShopNum() {
        return 0;
    }

    @Override
    public TrafficGraph getTrafficGraph() {
        return this.topFloorGraph;
    }

    @Override
    public int getFloorNum() {
        return this.floorNum;
    }

    /* ------------- draw ------------- */

    @Override
    public void display(){

    }
}
