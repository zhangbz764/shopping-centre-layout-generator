package formInteractive.blockSplit;

import processing.core.PApplet;
import formInteractive.graphAdjusting.TrafficGraph;
import render.JtsRender;
import wblut.geom.WB_Polygon;

import java.util.List;

/**
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/30
 * @time 11:13
 * @description
 */
public interface Split {
    public void init(TrafficGraph graph);

    public WB_Polygon getPublicBlockPoly();

    public List<WB_Polygon> getShopBlockPolys();

    public int getShopBlockNum();

    public void display(JtsRender render, PApplet app);

//    public void performSplit();
}