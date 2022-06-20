package main;

import controlP5.ControlFont;
import controlP5.ControlP5;
import controlP5.DropdownList;
import mallParameters.MallConst;
import mallParameters.MallParam;
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
    public void initGUI(ControlP5 cp5, int cp5H, MallParam mallParam, PApplet app) {
//        int startW = (int) (app.width * 0.02);
//        int startH = (int) (0.5 * (app.height - cp5H * 9));
        int startW = 20;
        int startH = 20;

        cp5.addButton("场地与轮廓导入")
                .setPosition(startW, startH)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_SITE_BOUNDARY)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addButton("动线&中庭形状")
                .setPosition(startW, startH + cp5H)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_TRAFFIC_ATRIUM)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addButton("空中连廊")
                .setPosition(startW, startH + cp5H * 2)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_MAIN_CORRIDOR)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addButton("公共空间形态")
                .setPosition(startW, startH + cp5H * 3)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_PUBLIC_SPACE)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addButton("自动扶梯")
                .setPosition(startW, startH + cp5H * 4)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_ESCALATOR)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        cp5.addButton("格网参考线")
                .setPosition(startW, startH + cp5H * 5)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_STRUCTURE_GRID)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addButton("铺位划分")
                .setPosition(startW, startH + cp5H * 6)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_SHOP_EDIT)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addButton("疏散楼梯")
                .setPosition(startW, startH + cp5H * 7)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_EVAC_STAIRWAY)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addButton("卫生间")
                .setPosition(startW, startH + cp5H * 8)
                .setSize(MallConst.STATUS_W, cp5H)
                .setId(MallConst.E_EVAC_STAIRWAY)
                .setFont(font)
                .setColorBackground(app.color(63, 63, 63))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        addBoundaryGUI(cp5, startW, startH, mallParam, app);
        addTrafficGUI(cp5, startW, startH + cp5H, mallParam, app);
        addCorridorGUI(cp5, startW, startH + cp5H * 2, mallParam, app);
        addPublicGUI(cp5, startW, startH + cp5H * 3, mallParam, app);
        addEscalatorGUI(cp5, startW, startH + cp5H * 4, app);
        addGridGUI(cp5, startW, startH + cp5H * 5, app);
        addShopGUI(cp5, startW, startH + cp5H * 6, app);
        addStairwayGUI(cp5, startW, startH + cp5H * 7, app);
        addWashroomGUI(cp5, startH + cp5H * 8);
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
            case (MallConst.E_ESCALATOR):
                setVisible(cp5, MallConst.E_ESCALATOR);
                break;
            case (MallConst.E_STRUCTURE_GRID):
                setVisible(cp5, MallConst.E_STRUCTURE_GRID);
                break;
            case (MallConst.E_SHOP_EDIT):
                setVisible(cp5, MallConst.E_SHOP_EDIT);
                break;
            case (MallConst.E_EVAC_STAIRWAY):
                setVisible(cp5, MallConst.E_EVAC_STAIRWAY);
                break;
            case (MallConst.E_WASHROOM):
                setVisible(cp5, MallConst.E_WASHROOM);
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
    public void addBoundaryGUI(ControlP5 cp5, int startW, int startH, MallParam mallParam, PApplet app) {
        cp5.addButton("switchDirection")
                .setPosition(startW + MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_SWITCH_BOUNDARY)
                .setFont(font)
                .setLabel("切换方向")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addSlider("siteRedLineDist")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_REDLINE_DIST)
                .setRange(0, MallConst.SITE_REDLINEDIST_MAX)
                .setValue(mallParam.siteRedLineDist)
                .setFont(font)
                .setLabel("红线距离")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        cp5.addSlider("siteBufferDist")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_OFFSET_DIST)
                .setRange(MallConst.SITE_BUFFER_MIN, MallConst.SITE_BUFFER_MAX)
                .setValue(mallParam.siteBufferDist)
                .setFont(font)
                .setLabel("退界距离")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
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
    public void addTrafficGUI(ControlP5 cp5, int startW, int startH, MallParam mallParam, PApplet app) {
        // traffic controllers' visibility
        cp5.addButton("trafficControllers")
                .setPosition(startW + MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_TRAFFIC_CONTROLLERS)
                .setFont(font)
                .setLabel("显示/隐藏路径控制点")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // traffic buffer distance
        cp5.addSlider("trafficBufferDist")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_TRAFFIC_WIDTH)
                .setRange(MallConst.TRAFFIC_BUFFER_DIST_MIN, MallConst.TRAFFIC_BUFFER_DIST_MAX)
                .setValue(mallParam.trafficBufferDist)
                .setFont(font)
                .setLabel("路径偏移距离")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // curve button
        cp5.addButton("curveOrPoly")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_CURVE_ATRIUM)
                .setFont(font)
                .setLabel("中庭曲线/折线")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // delete button
        cp5.addButton("deleteAtrium")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 3)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_ATRIUM)
                .setFont(font)
                .setLabel("删除中庭")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // angle slider
        cp5.addSlider("atriumAngle")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 4)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ATRIUM_ANGLE)
                .setRange(-180, 180)
                .setValue(mallParam.atriumAngle)
                .setFont(font)
                .setLabel("中庭旋转角度")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // area slider
        cp5.addSlider("atriumArea")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 5)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ATRIUM_AREA)
                .setRange(MallConst.ATRIUM_AREA_MIN, MallConst.ATRIUM_AREA_MAX)
                .setValue(mallParam.atriumArea)
                .setFont(font)
                .setLabel("中庭面积")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // atrium type DropdownList
        DropdownList ddl = cp5.addDropdownList("atriumShape")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 6)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H * 9)
                .setId(MallConst.LIST_ATRIUM_FACTORY)
                .setFont(font)
                .setLabel("中庭形状列表")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83));
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
        ddl.addItem("L形", MallConst.ITEM_A_LSHAPE);
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
    public void addCorridorGUI(ControlP5 cp5, int startW, int startH, MallParam mallParam, PApplet app) {
        // public corridor width
        cp5.addSlider("corridorWidth")
                .setPosition(startW + MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_CORRIDOR_WIDTH)
                .setRange(MallConst.CORRIDOR_WIDTH_MIN, MallConst.CORRIDOR_WIDTH_MAX)
                .setValue(mallParam.corridorWidth)
                .setFont(font)
                .setLabel("公区走道宽度")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        this.controllerNames[2] = new String[]{
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
    public void addPublicGUI(ControlP5 cp5, int startW, int startH, MallParam mallParam, PApplet app) {
        // delete public space node
        cp5.addButton("deletePublicSpaceNode")
                .setPosition(startW + MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_DELETE_PUBLIC_NODE)
                .setFont(font)
                .setLabel("删除控制点")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // public space buffer distance slider
        cp5.addSlider("publicSpaceBufferDist")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_BUFFER_DIST)
                .setRange(MallConst.PUBLIC_BUFFER_DIST_MIN, MallConst.PUBLIC_BUFFER_DIST_MAX)
                .setValue(mallParam.publicSpaceBufferDist)
                .setFont(font)
                .setLabel("交通空间偏移距离")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // switch round or smooth
        cp5.addButton("atriumRoundType")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_ATRIUM_ROUND)
                .setFont(font)
                .setLabel("中庭圆角/倒角")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // the radius of rounding atrium
        cp5.addSlider("atriumRoundRadius")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 3)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_ROUND_RADIUS)
                .setRange(MallConst.ATRIUM_ROUND_RADIUS_MIN, MallConst.ATRIUM_ROUND_RADIUS_MAX)
                .setValue(mallParam.atriumRoundRadius)
                .setFont(font)
                .setLabel("中庭圆角半径")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // smooth times of atrium
        cp5.addSlider("atriumSmoothTimes")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 4)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.SLIDER_SMOOTH_TIMES)
                .setRange(0, 5)
                .setValue(mallParam.atriumSmoothTimes)
                .setFont(font)
                .setLabel("中庭倒角次数")
                .setVisible(false)
                .plugTo(mallParam)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        this.controllerNames[3] = new String[]{
                "deletePublicSpaceNode",
                "publicSpaceBufferDist",
                "atriumRoundType",
                "atriumRoundRadius",
                "atriumSmoothTimes"
        };
    }

    /**
     * update status 6 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addEscalatorGUI(ControlP5 cp5, int startW, int startH, PApplet app) {
        // change escalator position
        cp5.addButton("changeEscalatorPosition")
                .setPosition(startW + MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_UPDATE_ESCALATOR)
                .setFont(font)
                .setLabel("更改扶梯位置")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        this.controllerNames[4] = new String[]{
                "changeEscalatorPosition"
        };
    }

    /**
     * update status 4 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addGridGUI(ControlP5 cp5, int startW, int startH, PApplet app) {
        // grid model
        cp5.addButton("gridModel")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_GRID_MODEL)
                .setFont(font)
                .setLabel("8.4m/9m")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // grid number list
        cp5.addDropdownList("gridNum")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H * 2)
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
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        this.controllerNames[5] = new String[]{
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
    public void addShopGUI(ControlP5 cp5, int startW, int startH, PApplet app) {
        // union cells
        cp5.addButton("unionShopCell")
                .setPosition(startW + MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_UNION_CELLS)
                .setFont(font)
                .setLabel("合并选中商铺")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // split cells
        cp5.addButton("splitShopCell")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_SPLIT_CELLS)
                .setFont(font)
                .setLabel("阴/阳角二次剖分")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        this.controllerNames[6] = new String[]{
                "unionShopCell",
                "splitShopCell"
        };
    }

    /**
     * update status 7 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addStairwayGUI(ControlP5 cp5, int startW, int startH, PApplet app) {
        // generate stairway module
        cp5.addButton("generateModule")
                .setPosition(startW + MallConst.STATUS_W, startH)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_EVAC_MODEL)
                .setFont(font)
                .setLabel("生成模块")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;
        // change stairway module direction
        cp5.addButton("moduleDirection")
                .setPosition(startW + MallConst.STATUS_W, startH + MallConst.CONTROLLER_H)
                .setSize(MallConst.CONTROLLER_W, MallConst.CONTROLLER_H)
                .setId(MallConst.BUTTON_EVAC_DIR)
                .setFont(font)
                .setLabel("模块方向")
                .setVisible(false)
                .setColorBackground(app.color(80, 80, 80))
                .setColorForeground(app.color(200, 120, 60))
                .setColorActive(app.color(220, 151, 83))
        ;

        this.controllerNames[7] = new String[]{
                "generateModule",
                "moduleDirection"
        };
    }

    /**
     * update status 8 GUI
     *
     * @param cp5    controlP5
     * @param startH start height of the controllers
     * @return void
     */
    public void addWashroomGUI(ControlP5 cp5, int startH) {

        this.controllerNames[8] = new String[]{

        };
    }

    /* ------------- info display ------------- */

    public void displayInfo(PApplet app, int status) {
        app.pushStyle();
        app.textAlign(PConstants.RIGHT);
        String base = "鼠标右键：平移" + "\n" + "鼠标滚轮：缩放" + "\n";
        String info = "";
        switch (status) {
            case 0:
                info = base + "左键拖拽控制点";
                break;
            case 1:
                info = base + "左键拖拽控制点"
                        + "\n" + "在列表中选择形状后单击空白位置放置"
                        + "\n" + "左键单击以选择形状"
                        + "\n" + "拖拽以改变形状"
                        + "\n" + "再次单击空白处以取消选择";
                break;
            case 2:
                info = base + "左键单击以选择走廊"
                        + "\n" + "拖拽以改变走廊位置"
                ;
                break;
            case 3:
                info = base + "左键单击以选择中庭或轮廓点";
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
                info = base + "左键单击以选择扶梯";
                break;
        }

        app.text(info, app.width - 20, 30);
        app.popStyle();
    }
}
