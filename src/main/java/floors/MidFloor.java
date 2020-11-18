package floors;

import formInteractive.graphAdjusting.TrafficGraph;
import processing.core.PApplet;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render3D;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/9
 * @time 10:50
 * @description middle floor of a shopping mall
 */
public class MidFloor extends Floor {

    /* ------------- constructor ------------- */

    public MidFloor(int floorNum, TrafficGraph mainGraph, WB_Polygon boundary) {
        super(floorNum, mainGraph, boundary);
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
    public void display(WB_Render3D render, PApplet app) {
        super.display(render, app);
    }
}
