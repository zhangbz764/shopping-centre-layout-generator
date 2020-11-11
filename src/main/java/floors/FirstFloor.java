package floors;

import formInteractive.graphAdjusting.TrafficGraph;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:49
 * @description first floor of a shopping mall
 */
public class FirstFloor extends Floor {

    /* ------------- constructor ------------- */

    public FirstFloor() {
        super.setFloorNum(1);
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
    public void display() {

    }
}
