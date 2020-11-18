package formInteractive;

import floors.FirstFloor;
import floors.Floor;
import floors.MidFloor;
import floors.TopFloor;
import formInteractive.graphAdjusting.TrafficGraph;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/11/18
 * @time 21:25
 * @description generate all floors in the shopping mall
 */
public class FloorGenerator {
    Floor[] allFloors;

    // statistics


    /* ------------- constructor & initialize ------------- */

    public FloorGenerator(int num, TrafficGraph mainGraph) {

    }

//    public void init(int num, TrafficGraph mainGraph) {
//        this.allFloors = new Floor[num];
//        allFloors[0] = new FirstFloor(mainGraph);
//        for (int i = 1; i < num - 1; i++) {
//            allFloors[i] = new MidFloor(i + 1, mainGraph);
//        }
//        allFloors[num - 1] = new TopFloor(num, mainGraph);
//    }
}
