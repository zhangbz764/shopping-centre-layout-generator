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

    /* ------------- constructor ------------- */

    public Floor() {

    }

    /* ------------- set & get ------------- */

    public abstract void setTrafficGraph(TrafficGraph mainGraph);

    public abstract int getShopBlockNum();

    public abstract int getShopNum();

    public abstract TrafficGraph getTrafficGraph();

    public abstract int getFloorNum();

    /* ------------- draw ------------- */

    public abstract void display();
}
