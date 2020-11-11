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

    /* ------------- constructor ------------- */

    public TopFloor(int floorNum) {
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
