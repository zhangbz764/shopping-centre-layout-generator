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

    /* ------------- constructor ------------- */

    public MiddleFloor(int floorNum) {
        super.setFloorNum(floorNum);
    }

    /* ------------- set & get ------------- */

    @Override
    public int getShopBlockNum() {
        return 0;
    }

    @Override
    public int getShopNum() {
        return 0;
    }

    /* ------------- draw ------------- */

    @Override
    public void display(){

    }
}
