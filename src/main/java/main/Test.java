package main;

import Guo_Cam.CameraController;
import floors.Floor;
import formInteractive.InputData;
import formInteractive.SpacialFormGenerator;
import processing.core.PApplet;
import render.DisplayBasic;
import render.JtsRender;
import wblut.processing.WB_Render3D;

public class Test extends PApplet {
    // model scale from input file
    private final static double scale = 2;

    // input file
    public final String path = "E:\\AAA_Project\\202009_Shuishi\\codefiles\\1029.3dm";
    public final InputData input = new InputData();

    // generate elements
    public SpacialFormGenerator spacialFormGenerator;
    public Floor[] floors;

    // switch toggle
    public boolean publicSpaceAdjust = false;
    public boolean publicSpaceDraw = true;

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

        floors = new Floor[4];
        for (int i = 0; i < floors.length; i++) {
            println("generating floor " + (i + 1));
            floors[i] = new Floor(i + 1, spacialFormGenerator.getMainGraph(), input.getInputBoundary(), scale);
        }
    }

    /* ------------- draw ------------- */

    public void draw() {
        background(255);
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

    public void draw2D(JtsRender jtsRender, WB_Render3D render, PApplet app) {
        pushStyle();
        noStroke();
        fill(200);
        rect(0, 0, 700, 1000);
        popStyle();
        if (publicSpaceDraw) {
            spacialFormGenerator.display(jtsRender, render, this);
        }
        for (Floor f : floors) {
            if (f.activate) {
                f.display(render, jtsRender, app);
            }
        }
    }

    public void draw3D(JtsRender jtsRender, WB_Render3D render, PApplet app) {
        DisplayBasic.drawAxis(this, 50);
        if (publicSpaceDraw) {
            pushMatrix();
            for (int i = 0; i < floors.length; i++) {
                floors[i].display(render, jtsRender, app);
                translate(0, 0, 500);
            }
            popMatrix();
        }
    }

    /* ------------- print & text ------------- */

    public void showText() {
        if (publicSpaceDraw) {
            if (publicSpaceAdjust) {
                for (Floor f : floors) {
                    if (f.activate) {
                        text(f.getTextInfo(), 30, 750);
                    }
                }
                String title = "INSTRUCTIONS";
                textSize(15);
                fill(0);
                text(title, 30, 30);

                String operation = "Hold right button to drag a node"
                        + "\n" + "Press 'r' to reload input file"
                        + "\n" + "Press 'a' to add a tree node at mouse location"
                        + "\n" + "Press 's' to remove a tree node at mouse location"
                        + "\n" + "Press 'q' to add a node at mouse location"
                        + "\n" + "Press 'w' to remove a node at mouse location"
                        + "\n" + "Press 'z' to increase node region radius"
                        + "\n" + "Press 'x' to decrease node region radius";
                textSize(15);
                strokeWeight(1);
                text(operation, 30, 70);
            }
        }
    }

    /* ------------- mouse & key interaction ------------- */

    public void mouseDragged() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            // drag a node of traffic graph
            spacialFormGenerator.dragUpdate(mouseX, -1 * mouseY + height);
            for (Floor floor : floors) {
                floor.updateSplit(spacialFormGenerator.getMainGraph());
            }
            spacialFormGenerator.setMainGraphSwitch(false);
        }
    }

    public void mouseReleased() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            spacialFormGenerator.releaseUpdate();
        }
    }

    public void mouseClicked() {
        for (Floor f : floors) {
            if (f.activate && mouseButton == RIGHT) {
                f.selectShop(mouseX, -1 * mouseY + height);
            }
        }
    }

    public void keyPressed() {
        // display control
        if (key == '7') {
            publicSpaceDraw = !publicSpaceDraw;
        }

        // switch control
        if (key == ',') {
            publicSpaceAdjust = !publicSpaceAdjust;
        }

        for (int i = 1; i < floors.length + 1; i++) {
            char num = Character.forDigit(i, 10);
            floors[i - 1].activate = key == num;

        }

        // reload input file
        if (key == 'r' || key == 'R') {
            input.loadData(path, scale);
            spacialFormGenerator.init(input);
        }
        // interact control
        if (publicSpaceAdjust) {
            spacialFormGenerator.keyUpdate(mouseX, -1 * mouseY + height, this);
            for (Floor floor : floors) {
                floor.updateSplit(spacialFormGenerator.getMainGraph());
            }
            spacialFormGenerator.setMainGraphSwitch(false);
        }
    }
}
