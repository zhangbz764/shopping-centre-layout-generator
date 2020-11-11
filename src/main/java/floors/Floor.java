package floors;

import formInteractive.graphAdjusting.TrafficGraph;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:49
 * @description abstract class for floors in the shopping mall
 */
public abstract class Floor {
    private TrafficGraph trafficGraph;
    private int floorNum;

    /* ------------- constructor ------------- */

    public Floor() {

    }

    /* ------------- set & get ------------- */

    public void setTrafficGraph(TrafficGraph mainGraph) {
        this.trafficGraph = mainGraph;
    }

    public TrafficGraph getTrafficGraph() {
        return this.trafficGraph;
    }

    public void setFloorNum(int floorNum) {
        this.floorNum = floorNum;
    }

    public int getFloorNum() {
        return floorNum;
    }

    public abstract int getShopBlockNum();

    public abstract int getShopNum();

    /* ------------- draw ------------- */

    public abstract void display();
}
