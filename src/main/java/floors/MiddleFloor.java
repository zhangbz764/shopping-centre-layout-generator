package floors;

import formInteractive.graphAdjusting.TrafficGraph;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:50
 * @description middle floor of a shopping mall
 */
public class MiddleFloor extends Floor {
    private TrafficGraph middleFloorGraph;
    private final int floorNum;

    /* ------------- constructor ------------- */

    public MiddleFloor(int floorNum) {
        this.floorNum = floorNum;
    }

    /* ------------- set & get ------------- */

    @Override
    public void setTrafficGraph(TrafficGraph mainGraph) {
        this.middleFloorGraph = mainGraph;
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
        return this.middleFloorGraph;
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
