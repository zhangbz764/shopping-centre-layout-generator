package main;

import Guo_Cam.CameraController;
import formInteractive.SpacialFormGenerator;
import processing.core.PApplet;
import render.DisplayBasic;
import render.JtsRender;
import shoptype.ShopGenerator;
import wblut.processing.WB_Render3D;

public class Test extends PApplet {
    // input file path
    public final String path = "E:\\AAA_Project\\202009_Shuishi\\codefiles\\1029.3dm";

    // switch toggle
    public boolean publicSpaceAdjust = false;
    public boolean publicSpaceDraw = true;
    public boolean shopSpaceDraw = true;

    // generate steps
    public SpacialFormGenerator spacialFormGenerator;
    public ShopGenerator shopGenerator;

    // utils
    public WB_Render3D render;
    public JtsRender jtsRender;
    public CameraController gcam;

    /* ------------- settings ------------- */

    public void settings() {
        size(1800, 1000, P3D);
    }

    /* ------------- setup ------------- */

    public void setup() {
        render = new WB_Render3D(this);
        jtsRender = new JtsRender(this);
        gcam = new CameraController(this);

        spacialFormGenerator = new SpacialFormGenerator(path);
        shopGenerator = new ShopGenerator(spacialFormGenerator.getShopBlock(), spacialFormGenerator.getSkeletons());
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        DisplayBasic.drawAxis(this, 50);

        gcam.begin2d();
//        gcam.top();
        draw2D(jtsRender, render, this);

        gcam.begin3d();
        draw3D(jtsRender, render, this);
    }

    public void draw2D(JtsRender jrender, WB_Render3D render, PApplet app) {
        if (publicSpaceDraw) {
            showText();
            spacialFormGenerator.display(jtsRender, render, this);
        }
        if(shopSpaceDraw){
            shopGenerator.display(render, this);
        }
    }

    public void draw3D(JtsRender jrender, WB_Render3D render, PApplet app) {
        if (publicSpaceDraw) {
            spacialFormGenerator.display(jtsRender, render, this);
        }
    }

    /* ------------- print & text ------------- */

    public void showText() {
        fill(0);
        if (publicSpaceAdjust) {
            String string = "** ADJUSTING TRAFFIC GRAPH **"
                    + "\n" + "Press 'r' to reload input file"
                    + "\n"
                    + "\n" + "Press 'a' to add a tree node at mouse location"
                    + "\n" + "Press 's' to remove a tree node at mouse location"
                    + "\n" + "Press 'q' to add a node at mouse location"
                    + "\n" + "Press 'w' to remove a node at mouse location"
                    + "\n"
                    + "\n" + "Press 'z' to increase node region radius"
                    + "\n" + "Press 'x' to decrease node region radius";
            //textSize(15);
            text(string, 10, 20);
        }
    }

    /* ------------- mouse & key interaction ------------- */

    public void mouseDragged() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            // drag a node of traffic graph
            spacialFormGenerator.mouseDrag(this);
            shopGenerator = new ShopGenerator(spacialFormGenerator.getShopBlock(), spacialFormGenerator.getSkeletons());
        }
    }

    public void mouseReleased() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            spacialFormGenerator.mouseRelease(this);
        }
    }

    public void keyPressed() {
        // display control
//        if (key == '+') {
//
//        }
//        if (key == '-') {
//
//        }
        if (key == '7') {
            publicSpaceDraw = !publicSpaceDraw;
        }
        if (key == '8') {
            shopSpaceDraw = !shopSpaceDraw;
        }

        // switch control
        if (key == '1') {
            publicSpaceAdjust = !publicSpaceAdjust;
        }

        // interact control
        if (publicSpaceAdjust) {
            spacialFormGenerator.keyInteract(this);
            shopGenerator = new ShopGenerator(spacialFormGenerator.getShopBlock(), spacialFormGenerator.getSkeletons());
        }
    }
}
