package main;

import Guo_Cam.CameraController;
import floors.Floor;
import formInteractive.InputData;
import formInteractive.SpacialFormGenerator;
import processing.core.PApplet;
import render.ZDisplay;
import render.JtsRender;
import wblut.processing.WB_Render3D;

public class Test extends PApplet {
    // model scale from input file
    private final static double scale = 2;

    // input file
    public final String path = "E:\\AAA_Project\\202009_Shuishi\\codefiles\\1029.3dm";
    public final InputData input = new InputData();

    // generate elements
    public SpacialFormGenerator publicSpaceGenerator;
    public Floor[] floors;

    // switch toggle
    public boolean publicSpaceAdjust = false;
    public boolean publicSpaceDraw = true;
    public boolean floorAdjust = false;
    public boolean floorDraw = true;

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
        publicSpaceGenerator = new SpacialFormGenerator(input);

        floors = new Floor[2];
        for (int i = 0; i < floors.length; i++) {
            println("generating floor " + (i + 1));
            if (i == 0) {
                floors[i] = new Floor(i + 1, publicSpaceGenerator.getMainGraph(), input.getInputBoundary(), scale);
            } else {
                floors[i] = new Floor(i + 1, publicSpaceGenerator.getFloorGraph(), input.getInputBoundary(), scale);
            }
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

        showInstruction();

        gcam.begin3d();
        draw3D(jtsRender, render, this);
    }

    public void draw2D(JtsRender jtsRender, WB_Render3D render, PApplet app) {
        // "background"
        pushStyle();
        noStroke();
        fill(180);
        rect(0, 0, 700, height);
        popStyle();

        ZDisplay.drawAxis2D(this, 50);
        if (publicSpaceDraw) {
            publicSpaceGenerator.display(jtsRender, render, this);
        }
        if (floorDraw) {
            for (Floor f : floors) {
                if (f.activate) {
                    f.display(render, jtsRender, app);
                }
            }
        }
    }

    public void draw3D(JtsRender jtsRender, WB_Render3D render, PApplet app) {
        ZDisplay.drawAxis3D(this, 50);
        if (floorDraw) {
            pushMatrix();
            for (Floor floor : floors) {
                floor.display(render, jtsRender, app);
                translate(0, 0, 500);
            }
            popMatrix();
        }
    }

    /* ------------- print & text ------------- */

    public void showInstruction() {
        pushStyle();
        textSize(15);
        fill(0);
        showInstructions1();
        showInstructions2();
        showFloorStatistics();
        popStyle();
    }

    public void showInstructions1() {
        if (publicSpaceAdjust) {
            String title = "INSTRUCTIONS";
            text(title, 30, 30);
            StringBuilder operation = new StringBuilder("Press '.' to enable FLOOR adjustment"
                    + "\n" + "This will also lock the PUBLIC SPACE adjustment"
                    + "\n"
                    + "\n" + "Hold right button to drag a node"
                    + "\n" + "Press 'r' to reload input file"
                    + "\n" + "Press 'a' to add a tree node at mouse location"
                    + "\n" + "Press 's' to remove a tree node at mouse location"
                    + "\n" + "Press 'q' to add a node at mouse location"
                    + "\n" + "Press 'w' to remove a node at mouse location"
                    + "\n" + "Press 'z' to increase node region radius"
                    + "\n" + "Press 'x' to decrease node region radius"
                    + "\n");
            for (int i = 0; i < floors.length; i++) {
                String s = "\n" + "Press '" + (i + 1) + "' to switch to " + (i + 1) + "F";
                operation.append(s);
            }
            text(operation.toString(), 30, 70);
        }
    }

    public void showInstructions2() {
        if (floorDraw) {
            if (floorAdjust) {
                String title = "INSTRUCTIONS";
                text(title, 30, 30);
                StringBuilder operation = new StringBuilder("Press ',' to enable PUBLIC SPACE adjustment"
                        + "\n" + "This will also dismiss and lock the FLOOR adjustment"
                        + "\n"
                        + "\n" + "Right click a shop to pick it"
                        + "\n" + "Press 'u' to union all the picked shops"
                        + "\n");
                for (int i = 0; i < floors.length; i++) {
                    String s = "\n" + "Press '" + (i + 1) + "' to switch to " + (i + 1) + "F";
                    operation.append(s);
                }
                text(operation.toString(), 30, 70);
            }
        }
    }

    public void showFloorStatistics() {
        if (floorDraw) {
            for (Floor f : floors) {
                if (f.activate) {
                    text(f.getTextInfo(), 30, 750);
                    text(f.getTextInfo2(), 350, 750);
                }
            }
        }
    }

    /* ------------- mouse & key interaction ------------- */

    public void mouseDragged() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            // drag a node of traffic graph
            publicSpaceGenerator.dragUpdate(mouseX, -1 * mouseY + height);
            for (Floor floor : floors) {
                if (floor.getFloorNum() == 1) {
                    floor.updateSplit(publicSpaceGenerator.getMainGraph());
                } else {
                    floor.updateSplit(publicSpaceGenerator.getFloorGraph());
                }
            }
            publicSpaceGenerator.setGraphSwitch(false);
        }
    }

    public void mouseReleased() {
        if (publicSpaceAdjust && mouseButton == RIGHT) {
            publicSpaceGenerator.releaseUpdate();
        }
    }

    public void mouseClicked() {
        if (!publicSpaceAdjust && floorAdjust) {
            for (Floor f : floors) {
                if (f.activate && mouseButton == RIGHT) {
                    f.selectShop(mouseX, -1 * mouseY + height);
                }
            }
        }
        if (publicSpaceAdjust && !floorAdjust && mouseButton == LEFT) {
            publicSpaceGenerator.atriumEdit(mouseX, -1 * mouseY + height);
        }
        if (publicSpaceAdjust && !floorAdjust && mouseButton == RIGHT) {
            publicSpaceGenerator.atriumEditEnd();
        }
    }

    public void keyPressed() {
        // display control
        if (key == '/') {
            publicSpaceDraw = !publicSpaceDraw;
        }
        if (key == '*') {
            floorDraw = !floorDraw;
        }

        // switch control
        if (key == ',') {
            publicSpaceAdjust = !publicSpaceAdjust;
            for (Floor floor : floors) {
                floor.clearSelect();
            }
            floorAdjust = false;
        }
        if (key == '.') {
            floorAdjust = !floorAdjust;
            publicSpaceAdjust = false;
        }

        // switch floor draw
        if (floorDraw) {
            for (int i = 1; i < floors.length + 1; i++) {
                char num = Character.forDigit(i, 10);
                if (key == num) {
                    floors[i - 1].activate = true;
                    for (int j = 1; j < floors.length + 1; j++) {
                        if (j != i) {
                            floors[j - 1].activate = false;
                        }
                    }
                }
            }
        }

        // floor polygon union
        if (!publicSpaceAdjust && floorAdjust) {
            if (key == 'u' || key == 'U') {
                for (Floor f : floors) {
                    f.updateShop();
                }
            }
        }

        // reload input file
        if (key == 'r' || key == 'R') {
            input.loadData(path, scale);
            publicSpaceGenerator.init(input);
            floors = new Floor[3];
            for (int i = 0; i < floors.length; i++) {
                println("generating floor " + (i + 1));
                if (i == 0) {
                    floors[i] = new Floor(i + 1, publicSpaceGenerator.getMainGraph(), input.getInputBoundary(), scale);
                } else {
                    floors[i] = new Floor(i + 1, publicSpaceGenerator.getFloorGraph(), input.getInputBoundary(), scale);
                }
            }
        }

        // interact control
        if (publicSpaceAdjust) {
            publicSpaceGenerator.keyUpdate(mouseX, -1 * mouseY + height, this);
            for (Floor floor : floors) {
                if (floor.getFloorNum() == 1) {
                    floor.updateSplit(publicSpaceGenerator.getMainGraph());
                } else {
                    floor.updateSplit(publicSpaceGenerator.getFloorGraph());
                }
            }
            publicSpaceGenerator.setGraphSwitch(false);
        }
    }
}
