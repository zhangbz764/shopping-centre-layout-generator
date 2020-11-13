package main;

import Guo_Cam.CameraController;
import formInteractive.InputData;
import formInteractive.SpacialFormGenerator;
import formInteractive.StructureGenerator;
import processing.core.PApplet;
import render.DisplayBasic;
import render.JtsRender;
import formInteractive.ShopGenerator;
import wblut.processing.WB_Render3D;

public class Test extends PApplet {
    // model scale from input file
    private final static double scale = 2;
    // input file path
    public final String path = "E:\\AAA_Project\\202009_Shuishi\\codefiles\\1029.3dm";
    public final InputData input = new InputData();

    // switch toggle
    public boolean publicSpaceAdjust = false;
    public boolean shopSpaceGenerate = false;
    public boolean publicSpaceDraw = true;
    public boolean shopSpaceDraw = false;

    // generate steps
    public SpacialFormGenerator spacialFormGenerator;
    public StructureGenerator structureGenerator;
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

        input.loadData(path, scale);
        spacialFormGenerator = new SpacialFormGenerator(input);
        structureGenerator = new StructureGenerator(input.getInputBoundary(), 8.5 * scale, spacialFormGenerator.getShopBlock());
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
        DisplayBasic.drawAxis(this, 50);

        gcam.begin2d();
        pushMatrix();
        scale(1, -1);
        translate(0, -height);
        draw2D(jtsRender, render, this);
        popMatrix();
        showText();

        gcam.begin3d();
        draw3D(jtsRender, render, this);
    }

    public void draw2D(JtsRender jrender, WB_Render3D render, PApplet app) {
        if (publicSpaceDraw) {
            spacialFormGenerator.display(jtsRender, render, this);
        }
        if (shopSpaceDraw) {
            shopGenerator.display(render, this);
        }
        structureGenerator.display(this);
    }

    public void draw3D(JtsRender jrender, WB_Render3D render, PApplet app) {
        if (publicSpaceDraw) {
            pushMatrix();
            structureGenerator.display(this);
            spacialFormGenerator.display(jtsRender, render, this);
            translate(0, 0, 100);
            structureGenerator.display(this);
            spacialFormGenerator.display(jtsRender, render, this);
            translate(0, 0, 100);
            structureGenerator.display(this);
            spacialFormGenerator.display(jtsRender, render, this);
            translate(0, 0, 100);
            structureGenerator.display(this);
            spacialFormGenerator.display(jtsRender, render, this);
            popMatrix();
        }
    }

    /* ------------- print & text ------------- */

    public void showText() {
        if (publicSpaceDraw) {
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
    }

    /* ------------- mouse & key interaction ------------- */

    public void mouseDragged() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            // drag a node of traffic graph
            spacialFormGenerator.dragUpdate(mouseX, -1 * mouseY + height);
            shopGenerator = new ShopGenerator(spacialFormGenerator.getShopBlock(), spacialFormGenerator.getSkeletons());
//            shopGenerator = new ShopGenerator();
//            shopSpaceDraw = false;
        }
    }

    public void mouseReleased() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            spacialFormGenerator.releaseUpdate();
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
        if (key == '2') {
            shopGenerator = new ShopGenerator(spacialFormGenerator.getShopBlock(), spacialFormGenerator.getSkeletons());
            shopSpaceDraw = !shopSpaceDraw;
        }

        // reload input file
        if (key == 'r' || key == 'R') {
            input.loadData(path, scale);
            spacialFormGenerator.init(input);
        }
        // interact control
        if (publicSpaceAdjust) {
            spacialFormGenerator.keyUpdate(mouseX, -1 * mouseY + height, this);
            shopGenerator = new ShopGenerator(spacialFormGenerator.getShopBlock(), spacialFormGenerator.getSkeletons());
        }
    }
}
