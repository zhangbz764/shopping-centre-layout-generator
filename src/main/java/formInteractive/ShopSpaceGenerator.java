package formInteractive;

import geometry.ZSkeleton;
import processing.core.PApplet;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/31
 * @time 21:54
 * @description
 */
public class ShopSpaceGenerator {
    private List<ZSkeleton> skeletons;

    /* ------------- constructor ------------- */

    public ShopSpaceGenerator(List<WB_Polygon> originalBlocks) {
        init(originalBlocks);
    }

    /* ------------- initialize & get (public) ------------- */

    public void init(List<WB_Polygon> originalBlocks) {
        skeletons = new ArrayList<>();
        for (WB_Polygon polygon : originalBlocks) {
            skeletons.add(new ZSkeleton(polygon));
        }
    }

    /* ------------- draw ------------- */

    public void display(PApplet app) {
        for (ZSkeleton skeleton : skeletons) {
            skeleton.display(app);
        }
    }

    /* ------------- mouse & key interaction at TRAFFIC GRAPH STEP ------------- */


}
