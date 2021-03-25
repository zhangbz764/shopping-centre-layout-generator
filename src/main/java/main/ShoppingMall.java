package main;

import Guo_Cam.CameraController;
import floors.Floor;
import formInteractive.SpacialFormGenerator;
import processing.core.PApplet;
import processing.core.PFont;
import render.JtsRender;
import render.ZRender;
import wblut.processing.WB_Render;

public class ShoppingMall extends PApplet {
    public void setStats() {
        MallConstant.SCALE = 2;

        MallConstant.MAIN_TRAFFIC_WIDTH = 18; // 主动线初始宽度
        MallConstant.SUB_TRAFFIC_WIDTH = 7; // 次动线初始宽度
        MallConstant.SIMPLE_SHOP_WIDTH = 8.5; // 小店铺宽度

        MallConstant.ATRIUM_WIDTH = 12; // 中庭宽度
        MallConstant.ATRIUM_CORRIDOR_WIDTH = 3.6; //中庭两侧走道宽度
    }

    // model scale from input file
    private final static double scale = 2;

    // input file
    public String inputPath;

//    public final String outputPath =
//            Objects.requireNonNull(
//                    this.getClass().getClassLoader().getResource("")
//            ).getPath() + "output.3dm";

    public final ImportData input = new ImportData();

    // generate elements
    public SpacialFormGenerator publicSpaceGenerator;
    public Floor[] floors;

    // switch toggle
    public boolean publicSpaceAdjust = false;
    public boolean publicSpaceDraw = true;
    public boolean floorAdjust = false;
    public boolean floorDraw = true;

    // utils
    public WB_Render render;
    public JtsRender jtsRender;
    public CameraController gcam;
    public PFont font;

    /* ------------- settings ------------- */

    public void settings() {
        size(1800, 1000, P3D);
    }

    /* ------------- setup ------------- */

    public void setup() {
        this.inputPath = "./src/main/resources/mall_test.3dm";

        setStats();


        render = new WB_Render(this);
        jtsRender = new JtsRender(this);
        gcam = new CameraController(this);
        font = createFont("E:\\0_myjars\\fonts\\simhei.ttf", 15);

        input.loadData(inputPath, scale);
        publicSpaceGenerator = new SpacialFormGenerator(input);

        floors = new Floor[4];
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

    public void draw2D(JtsRender jtsRender, WB_Render render, PApplet app) {
        // "background"
        pushStyle();
        noStroke();
        fill(180);
        rect(0, 0, 700, height);
        popStyle();

        ZRender.drawAxis2D(this, 50);
//        if (publicSpaceDraw) {
//            publicSpaceGenerator.display(jtsRender, render, this);
//        }
        if (floorDraw) {
            for (Floor f : floors) {
                if (f.activate) {
                    f.display(render, jtsRender, app);
                }
            }
        }
    }

    public void draw3D(JtsRender jtsRender, WB_Render render, PApplet app) {
        gcam.drawSystem(500);
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
        textFont(font);
        fill(0);
        showInstructions1();
        showInstructions2();
        showFloorStatistics();
        popStyle();
    }

    public void showInstructions1() {
        if (publicSpaceAdjust) {
            textSize(15);
            String title = "操作说明";
            text(title, 30, 30);
            StringBuilder operation = new StringBuilder("'.' 开启店铺编辑模式，并锁定现有修改"
                    + "\n" + "当前为交通动线编辑模式"
                    + "\n"
                    + "\n" + "按住鼠标右键拖拽控制点"
                    + "\n" + "'r' 重新载入模型"
                    + "\n" + "'a' 增加内部控制点"
                    + "\n" + "'s' 移除内部控制点"
                    + "\n" + "'q' 增加边界出入口点"
                    + "\n" + "'w' 移除边界出入口点"
                    + "\n" + "'e' 增加或删除中庭"
                    + "\n" + "'f' 增加或删除扶梯"
                    + "\n");
            for (int i = 0; i < floors.length; i++) {
                String s = "\n" + (i + 1) + "' 切换到 " + (i + 1) + "层视图";
                operation.append(s);
            }
            text(operation.toString(), 30, 70);
        }
    }

    public void showInstructions2() {
        if (floorDraw) {
            if (floorAdjust) {
                textSize(15);
                String title = "操作说明";
                text(title, 30, 30);
                StringBuilder operation = new StringBuilder("',' 开启交通动线编辑模式，并移除现有修改"
                        + "\n" + "当前为店铺编辑模式"
                        + "\n"
                        + "\n" + "单击右键选中店铺"
                        + "\n" + "'u' 合并所选区域"
                        + "\n");
                for (int i = 0; i < floors.length; i++) {
                    String s = "\n" + (i + 1) + "' 切换到 " + (i + 1) + "层视图";
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
                    textSize(30);
                    text(f.getFloorNum() + " 层", 600, 70);
                    textSize(15);
                    text(f.getTextInfo(), 30, 750);
                    text(f.getTextInfo2(), 300, 750);
                    f.displayStats(this, 400, 737);
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
            input.loadData(inputPath, scale);
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
