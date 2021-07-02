package webMain;

import Guo_Cam.CameraController;
import processing.core.PApplet;
import render.JtsRender;
import wblut.processing.WB_Render;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project archijson
 * @date 2021/3/10
 * @time 17:59
 */
public class MallShow extends PApplet {
    private MallServer server;
    private CameraController cam;

    private WB_Render render;
    private JtsRender jtsRender;

    public void settings() {
        size(600, 800, P3D);
    }

    public void setup() {
        render = new WB_Render(this);
        jtsRender = new JtsRender(this);
        cam = new CameraController(this, 1000);
        server = new MallServer();
    }

    public void draw() {
        background(221);
        cam.drawSystem(1000);
//        server.generator.display(this, render, jtsRender);
    }
}
