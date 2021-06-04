package oldVersion;

import mallElementNew.TrafficGraph;
import wblut.geom.WB_Polygon;

/**
 * generator of a series of floors
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/20
 * @time 0:36
 */
public class FloorGenerator {
    Floor[] floors;

    public FloorGenerator(int num, TrafficGraph mainGraph, WB_Polygon boundary, double scale) {
        this.floors = new Floor[num];
        for (int i = 0; i < floors.length; i++) {
            floors[i] = new Floor();
        }
    }
}
