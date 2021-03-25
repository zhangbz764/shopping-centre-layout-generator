package webMain;

import Guo_Cam.CameraController;
import processing.core.PApplet;

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

    public void settings () {
        size(600, 800, P3D);
    }

    public void setup() {
        cam = new CameraController(this, 1000);
        server = new MallServer();
    }

    public void draw() {
        background(221);
        cam.drawSystem(1000);
        server.generator.draw(this);
    }
}
