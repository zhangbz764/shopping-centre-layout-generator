package testPrototype;

import guo_cam.CameraController;
import igeo.IG;
import oldVersion.mallElements.TrafficGraph;
import processing.core.PApplet;
import render.JtsRender;
import wblut.processing.WB_Render;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/6/1
 * @time 16:38
 */
public class TestMST extends PApplet {

    /* ------------- settings ------------- */

    public void settings() {
        size(1000, 1000, P3D);
    }

    /* ------------- setup ------------- */

    private TrafficGraph graph;

    private CameraController gcam;
    private JtsRender jtsRender;
    private WB_Render render;

    public void setup() {
        gcam = new CameraController(this);
        jtsRender = new JtsRender(this);
        gcam.top();
        render = new WB_Render(this);

        String path = ".\\src\\test\\resources\\testSub.3dm";
        IG.init();
        IG.open(path);

        this.graph = new TrafficGraph();
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
    }

}
