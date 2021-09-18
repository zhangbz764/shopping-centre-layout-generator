package main;

import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.DropdownList;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

/**
 * GUI manager for the shopping mall (using controlP5)
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/8/4
 * @time 13:42
 */
public class MallGUI {
    private String[][] controllerNames;
    private final ControlFont font;

    /* ------------- constructor ------------- */

    public MallGUI(PFont font) {
        this.font = new ControlFont(font, 13);
        this.controllerNames = new String[][]{
                new String[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                new String[]{},
                new String[]{}
        };
    }

    /* ------------- member function ------------- */

    /**
     * initialize 9 edit status buttons
     *
     * @return void
     */
    public void initGUI(ControlP5 cp5, int cp5H, MallParam mallParam) {
        cp5.addButton("场地&建筑轮廓")
                .setPosition(0, 0)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_SITE_BOUNDARY)
                .setFont(font)
        ;
        cp5.addButton("主路径+中庭形状")
                .setPosition(0, cp5H)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_TRAFFIC_ATRIUM)
                .setFont(font)
        ;
        cp5.addButton("空中走廊")
                .setPosition(0, cp5H * 2)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_MAIN_CORRIDOR)
                .setFont(font)
        ;
        cp5.addButton("交通空间轮廓")
                .setPosition(0, cp5H * 3)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_PUBLIC_SPACE)
                .setFont(font)
        ;
        cp5.addButton("柱网")
                .setPosition(0, cp5H * 4)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_STRUCTURE_GRID)
                .setFont(font)
        ;
        cp5.addButton("铺位划分")
                .setPosition(0, cp5H * 5)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_SHOP_EDIT)
                .setFont(font)
        ;

        cp5.addButton("自动扶梯（无效）")
                .setPosition(0, cp5H * 6)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_ESCALATOR)
                .setFont(font)
        ;
        cp5.addButton("疏散楼梯（无效）")
                .setPosition(0, cp5H * 7)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_EVACUATION)
                .setFont(font)
        ;

        addStatus0GUI(cp5, 0, mallParam);
        addStatus1GUI(cp5, cp5H, mallParam);
        addStatus2GUI(cp5, cp5H * 2);
        addStatus3GUI(cp5, cp5H * 3);
        addStatus4GUI(cp5, cp5H * 4);
        addStatus5GUI(cp5, cp5H * 5);
//        addStatus6GUI(cp5, cp5H * 6);
//        addStatus7GUI(cp5, cp5H * 7);
    }


    /**
     * update GUI by given status number
     *
     * @param status edit status
     * @param cp5    controlP5
     * @return void
     */
    public void updateGUI(int status, ControlP5 cp5) {
        switch (status) {
            case (MallConst.E_SITE_BOUNDARY):
                setVisible(cp5, MallConst.E_SITE_BOUNDARY);
                break;
            case (MallConst.E_TRAFFIC_ATRIUM):
                setVisible(cp5, MallConst.E_TRAFFIC_ATRIUM);
                break;
            case (MallConst.E_MAIN_CORRIDOR):
                setVisible(cp5, MallConst.E_MAIN_CORRIDOR);
                break;
            case (MallConst.E_PUBLIC_SPACE):
                setVisible(cp5, MallConst.E_PUBLIC_SPACE);
                break;
            case (MallConst.E_STRUCTURE_GRID):
                setVisible(cp5, MallConst.E_STRUCTURE_GRID);
                break;
            case (MallConst.E_SHOP_EDIT):
                setVisible(cp5, MallConst.E_SHOP_EDIT);
                break;

            case (MallConst.E_ESCALATOR):
                break;
            case (MallConst.E_EVACUATION):
                break;
        }
    }

    /**
     * set controller's visibility
     *
     * @param cp5    controlP5
     * @param status status ID
     * @return void
     */
    private void setVisible(ControlP5 cp5, int status) {
        for (int i = 0; i < controllerNames.length; i++) {
            if (i == status) {
                for (String s : controllerNames[i]) {
                    cp5.getController(s).setVisible(true);
                }
            } else {
                for (String s : controllerNames[i]) {
                    cp5.getController(s).setVisible(false);
                }
            }
        }
    }

    /**
     * update status 0 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus0GUI(ControlP5 cp5, int startH, MallParam mallParam) {
        cp5.addButton("switchDirection")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_SWITCH_BOUNDARY)
                .setFont(font)
                .setLabel("切换方向")
                .setVisible(false)
        ;
        cp5.addSlider("siteRedLineDist")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_REDLINE_DIST)
                .setRange(0, MallConst.SITE_REDLINEDIST_MAX)
                .setValue(mallParam.siteRedLineDist)
                .setFont(font)
                .setLabel("红线距离")
                .setVisible(false)
                .plugTo(mallParam)
        ;
        cp5.addSlider("siteBufferDist")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_SITE_BUFFER)
                .setRange(MallConst.SITE_BUFFER_MIN, MallConst.SITE_BUFFER_MAX)
                .setValue(mallParam.siteBufferDist)
                .setFont(font)
                .setLabel("退界距离")
                .setVisible(false)
                .plugTo(mallParam)
        ;

        this.controllerNames[0] = new String[]{
                "switchDirection",
                "siteRedLineDist",
                "siteBufferDist"
        };
    }

    /**
     * update status 1 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus1GUI(ControlP5 cp5, int startH, MallParam mallParam) {
//        cp5.addButton("删除内部控制点")
//                .setPosition(MallConst.STATUS_W, startH)
//                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
//                .setId(MallConst.BUTTON_DELETE_INNERNODE)
//                .setFont(font)
//        ;
//        cp5.addButton("删除轮廓控制点")
//                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
//                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
//                .setId(MallConst.BUTTON_DELETE_ENTRYNODE)
//                .setFont(font)
//        ;

        // traffic controllers' visibility
        cp5.addButton("trafficControllers")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_TRAFFIC_CONTROLLERS)
                .setFont(font)
                .setLabel("显示/隐藏路径控制点")
                .setVisible(false)
        ;
        // traffic buffer distance
        cp5.addSlider("trafficBufferDist")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_TRAFFIC_WIDTH)
                .setRange(MallConst.TRAFFIC_BUFFER_DIST_MIN, MallConst.TRAFFIC_BUFFER_DIST_MAX)
                .setValue(mallParam.trafficBufferDist)
                .setFont(font)
                .setLabel("路径偏移距离")
                .setVisible(false)
                .plugTo(mallParam)
        ;
        // curve button
        cp5.addButton("curveOrPoly")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_CURVE_ATRIUM)
                .setFont(font)
                .setLabel("中庭曲线/折线")
                .setVisible(false)
        ;
        // delete button
        cp5.addButton("deleteAtrium")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 3)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_ATRIUM)
                .setFont(font)
                .setLabel("删除中庭")
                .setVisible(false)
        ;
        // angle slider
        cp5.addSlider("atriumAngle")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 4)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ATRIUM_ANGLE)
                .setRange(-180, 180)
                .setValue(mallParam.atriumAngle)
                .setFont(font)
                .setLabel("中庭旋转角度")
                .setVisible(false)
                .plugTo(mallParam)
        ;
        // area slider
        cp5.addSlider("atriumArea")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 5)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ATRIUM_AREA)
                .setRange(MallConst.ATRIUM_AREA_MIN, MallConst.ATRIUM_AREA_MAX)
                .setValue(mallParam.atriumArea)
                .setFont(font)
                .setLabel("中庭面积")
                .setVisible(false)
                .plugTo(mallParam)
        ;
        // atrium type DropdownList
        DropdownList ddl = cp5.addDropdownList("atriumShape")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 6)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H * 9)
                .setId(MallConst.LIST_ATRIUM_FACTORY)
                .setFont(font)
                .setLabel("中庭形状列表")
                .setVisible(false);
        ddl.setItemHeight(MallConst.CONTROLLER_H);
        ddl.setBarHeight(MallConst.CONTROLLER_H);
        ddl.setCaptionLabel("中庭形状列表");
        ddl.getCaptionLabel().getStyle().marginTop = 3;
        ddl.getCaptionLabel().getStyle().marginLeft = 3;
        ddl.getValueLabel().getStyle().marginTop = 3;
        ddl.addItem("三角形", MallConst.ITEM_A_TRIANGLE);
        ddl.addItem("正方形", MallConst.ITEM_A_SQUARE);
        ddl.addItem("梯形", MallConst.ITEM_A_TRAPEZOID);
        ddl.addItem("五边形", MallConst.ITEM_A_PENTAGON);
        ddl.addItem("六边形1", MallConst.ITEM_A_HEXAGON1);
        ddl.addItem("六边形2", MallConst.ITEM_A_HEXAGON2);
        ddl.addItem("L形", MallConst.ITEM_A_L_SHAPE);
        ddl.addItem("八边形", MallConst.ITEM_A_OCTAGON);

        this.controllerNames[1] = new String[]{
                "trafficControllers",
                "trafficBufferDist",
                "curveOrPoly",
                "deleteAtrium",
                "atriumAngle",
                "atriumArea",
                "atriumShape"
        };
    }

    /**
     * update status 2 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus2GUI(ControlP5 cp5, int startH) {
        // update public corridor
        cp5.addButton("updateMainCorridor")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_UPDATE_CORRIDOR)
                .setFont(font)
                .setLabel("更新中庭&走廊划分")
                .setVisible(false)
        ;
        // delete
        cp5.addButton("deleteCorridor")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_CORRIDOR)
                .setFont(font)
                .setLabel("删除")
                .setVisible(false)
        ;
        // public corridor width
        cp5.addSlider("corridorWidth")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_CORRIDOR_WIDTH)
                .setRange(MallConst.CORRIDOR_WIDTH_MIN, MallConst.CORRIDOR_WIDTH_MAX)
                .setValue(MallConst.CORRIDOR_WIDTH_INIT)
                .setFont(font)
                .setLabel("公区走道宽度")
                .setVisible(false)
        ;

        this.controllerNames[2] = new String[]{
                "updateMainCorridor",
                "deleteCorridor",
                "corridorWidth"
        };
    }

    /**
     * update status 3 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus3GUI(ControlP5 cp5, int startH) {
//        // buffer distance slider
//        cp5.addSlider("偏移距离")
//                .setPosition(MallConst.STATUS_W, startH)
//                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
//                .setId(MallConst.SLIDER_BUFFER_DIST)
//                .setRange(0, 3)
//                .setValue(0)
//                .setFont(font)
//        ;
//        this.activeControllers = new String[]{
//                "偏移距离"
//        };

        this.controllerNames[3] = new String[]{

        };
    }

    /**
     * update status 4 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus4GUI(ControlP5 cp5, int startH) {
//        // angle of the grid
//        cp5.addSlider("旋转角度")
//                .setPosition(MallConst.STATUS_W, startH)
//                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
//                .setId(MallConst.SLIDER_GRID_ANGLE)
//                .setRange(0, 360)
//                .setValue(0)
//                .setFont(font)
//        ;
        // grid model
        cp5.addButton("gridModel")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_GRID_MODEL)
                .setFont(font)
                .setLabel("8.4m/9m")
                .setVisible(false)
        ;
        // grid number list
        cp5.addDropdownList("gridNum")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H * 4)
                .setId(MallConst.LIST_GRID_NUM)
                .setItemHeight(MallConst.CONTROLLER_H)
                .setBarHeight(MallConst.CONTROLLER_H)
                .setCaptionLabel("柱网数目")
                .addItem("1", 1)
                .addItem("2", 2)
                .addItem("3", 3)
                .setFont(font)
                .setLabel("柱网数目")
                .setVisible(false)
        ;

        this.controllerNames[4] = new String[]{
                "gridModel",
                "gridNum"
        };
    }

    /**
     * update status 5 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus5GUI(ControlP5 cp5, int startH) {
        // union cells
        cp5.addButton("unionShopCell")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_UNION_CELLS)
                .setFont(font)
                .setLabel("合并选中商铺")
                .setVisible(false)
        ;

        this.controllerNames[5] = new String[]{
                "unionShopCell"
        };
    }

    /**
     * update status 6 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus6GUI(ControlP5 cp5, int startH) {

    }

    /**
     * update status 7 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStatus7GUI(ControlP5 cp5, int startH) {

    }

    /* ------------- info display ------------- */

    public void displayInfo(PApplet app, int status) {
        app.pushStyle();
        app.textAlign(PConstants.RIGHT);
        String base = "鼠标右键：平移" + "\n" + "鼠标滚轮：缩放" + "\n";
        String info = "";
        switch (status) {
            case 0:
            case 1:
                info = base + "左键拖拽控制点";
                break;
            case 2:
                info = base + "在列表中选择形状后单击空白位置放置"
                        + "\n" + "左键单击以选择形状"
                        + "\n" + "拖拽以改变形状"
                        + "\n" + "再次单击空白处以取消选择"
                ;
                break;
            case 3:
                info = base;
                break;
            case 4:
                info = base + "左键单击以选择柱网"
                        + "\n" + "拖拽以两轴编辑柱网"
                ;
                break;
            case 5:
                info = base + "左键单击以选择多个商铺";
                break;
            case 6:
                info = base + "左键单击选中后拖拽控制点"
                        + "\n" + "单击更新按钮以更新中庭划分"
                ;
                break;
        }

        app.text(info, app.width - 20, 30);
        app.popStyle();
    }
}
