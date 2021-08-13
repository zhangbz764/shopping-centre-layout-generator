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
    private String[] activeControllers = new String[]{};
    private final ControlFont font;

    /* ------------- constructor ------------- */

    public MallGUI(PFont font) {
        this.font = new ControlFont(font, 13);
    }

    /* ------------- member function ------------- */

    /**
     * initialize 9 edit status buttons
     *
     * @return void
     */
    public void initstatusButton(ControlP5 cp5, int cp5H) {
        cp5.addButton("场地&建筑轮廓")
                .setPosition(0, 0)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_SITE_BOUNDARY)
                .setFont(font)
        ;
        cp5.addButton("主路径")
                .setPosition(0, cp5H)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_MAIN_TRAFFIC)
                .setFont(font)
        ;
        cp5.addButton("原始中庭形状")
                .setPosition(0, cp5H * 2)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_RAW_ATRIUM)
                .setFont(font)
        ;
        cp5.addButton("中央交通空间轮廓")
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
        cp5.addButton("中庭&空中走廊")
                .setPosition(0, cp5H * 6)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_MAIN_CORRIDOR)
                .setFont(font)
        ;
        cp5.addButton("自动扶梯（无效）")
                .setPosition(0, cp5H * 7)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_ESCALATOR)
                .setFont(font)
        ;
        cp5.addButton("疏散楼梯（无效）")
                .setPosition(0, cp5H * 8)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_EVACUATION)
                .setFont(font)
        ;
    }

    /**
     * update GUI by given status number
     *
     * @param status edit status
     * @param cp5    controlP5
     * @param cp5H   height of the controller unit
     * @return void
     */
    public void updateGUI(int status, ControlP5 cp5, int cp5H) {
        int startH = cp5H * status;
        switch (status) {
            case (MallConst.E_SITE_BOUNDARY):
                updateStatus0GUI(cp5, startH);
                break;
            case (MallConst.E_MAIN_TRAFFIC):
                updateStatus1GUI(cp5, startH);
                break;
            case (MallConst.E_RAW_ATRIUM):
                updateStatus2GUI(cp5, startH);
                break;
            case (MallConst.E_PUBLIC_SPACE):
                updateStatus3GUI(cp5, startH);
                break;
            case (MallConst.E_STRUCTURE_GRID):
                updateStatus4GUI(cp5, startH);
                break;
            case (MallConst.E_SHOP_EDIT):
                updateStatus5GUI(cp5, startH);
                break;
            case (MallConst.E_MAIN_CORRIDOR):
                updateStatus6GUI(cp5, startH);
                break;
            case (MallConst.E_ESCALATOR):
                break;
            case (MallConst.E_EVACUATION):
                break;
        }
    }

    /**
     * remove current GUIs
     *
     * @param cp5 controlP5
     * @return void
     */
    private void removeGUI(ControlP5 cp5) {
        for (String s : activeControllers) {
            cp5.remove(s);
        }
    }

    /**
     * update status 0 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus0GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        cp5.addButton("切换方向")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_SWITCH_BOUNDARY)
                .setFont(font)
        ;
        cp5.addSlider("siteRedLineDist")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_REDLINE_DIST)
                .setRange(0, MallConst.SITE_REDLINE_DIST * 2)
                .setValue(MallConst.SITE_REDLINE_DIST)
                .setFont(font)
                .setLabel("红线距离")
        ;
        cp5.addSlider("siteBufferDist")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_SITE_BUFFER)
                .setRange(MallConst.SITE_BUFFER_MIN, MallConst.SITE_BUFFER_MAX)
                .setValue(MallConst.SITE_BUFFER_DIST)
                .setFont(font)
                .setLabel("退界距离")
        ;
        this.activeControllers = new String[]{
                "切换方向",
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
    public void updateStatus1GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
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
        cp5.addSlider("偏移距离")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_TRAFFIC_WIDTH)
                .setRange(MallConst.TRAFFIC_BUFFER_DIST - 2, MallConst.TRAFFIC_BUFFER_DIST + 2)
                .setValue(MallConst.TRAFFIC_BUFFER_DIST)
                .setFont(font)
        ;
        this.activeControllers = new String[]{
//                "删除内部控制点",
//                "删除轮廓控制点",
                "偏移距离"
        };
    }

    /**
     * update status 2 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus2GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        // curve button
        cp5.addButton("曲线/折线")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_CURVE_ATRIUM)
                .setFont(font)
        ;
        // delete button
        cp5.addButton("删除")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_ATRIUM)
                .setFont(font)
        ;
        // angle slider
        cp5.addSlider("旋转角度")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ATRIUM_ANGLE)
                .setRange(0, 360)
                .setValue(0)
                .setFont(font)
        ;
        // area slider
        cp5.addSlider("面积")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 3)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ATRIUM_AREA)
                .setRange(MallConst.ATRIUM_AREA_MIN, MallConst.ATRIUM_AREA_MAX)
                .setValue(MallConst.ATRIUM_AREA_INIT)
                .setFont(font)
        ;
        // atrium type DropdownList
        DropdownList ddl = cp5.addDropdownList("中庭形状列表")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 4)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H * 9)
                .setId(MallConst.LIST_ATRIUM_FACTORY)
                .setFont(font);
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

        this.activeControllers = new String[]{
                "曲线/折线",
                "删除",
                "旋转角度",
                "面积",
                "中庭形状列表"
        };
    }

    /**
     * update status 3 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus3GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
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
    }

    /**
     * update status 4 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus4GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
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
        cp5.addButton("8.4m/9m")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_GRID_MODEL)
                .setFont(font)
        ;
        // grid number list
        cp5.addDropdownList("柱网数目")
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
        ;
        this.activeControllers = new String[]{
                "8.4m/9m",
                "柱网数目"
        };
    }

    /**
     * update status 5 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus5GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        // union cells
        cp5.addButton("合并选中商铺")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_UNION_CELLS)
                .setFont(font)
        ;
        this.activeControllers = new String[]{
                "合并选中商铺"
        };
    }

    /**
     * update status 6 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void updateStatus6GUI(ControlP5 cp5, int startH) {
        removeGUI(cp5);
        // update public corridor
        cp5.addButton("更新中庭&走廊划分")
                .setPosition(MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_UPDATE_CORRIDOR)
                .setFont(font)
        ;
        // delete
        cp5.addButton("删除")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_CORRIDOR)
                .setFont(font)
        ;
        // public corridor width
        cp5.addSlider("公区走道宽度")
                .setPosition(MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_CORRIDOR_WIDTH)
                .setRange(MallConst.CORRIDOR_WIDTH_MIN, MallConst.CORRIDOR_WIDTH_MAX)
                .setValue(MallConst.CORRIDOR_WIDTH)
                .setFont(font)
        ;
        this.activeControllers = new String[]{
                "更新中庭&走廊划分",
                "公区走道宽度",
                "删除"
        };
    }

    /* ------------- info display ------------- */

    public void infoDisplay(PApplet app, int status) {
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
