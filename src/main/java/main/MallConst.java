package main;

/**
 * constants for the local version of the shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/4/17
 * @time 23:34
 */
public class MallConst {
    // shopping mall constants
    public static final int FLOOR_TOTAL = 5;              // 总层数

    public static final float SITE_REDLINE_DIST = 10;     // 场地红线距离
    public static final float SITE_BUFFER_MIN = 62;       // 场地退界最小值
    public static final float SITE_BUFFER_DIST = 67.2f;   // 场地退界预设距离
    public static final float SITE_BUFFER_MAX = 70;       // 场地退界最大值
    public static final double BOUNDARY_NODE_R = 5;       // 轮廓控制点判定范围

    public static final float TRAFFIC_BUFFER_DIST = 8;   // 主路两侧偏移距离
    public static final double TRAFFIC_NODE_R = 5;        // 主路径控制点判定范围

    public static final float ATRIUM_AREA_MAX = 600;      // 最大中庭面积
    public static final float ATRIUM_AREA_MIN = 280;      // 最小中庭面积
    public static final float ATRIUM_AREA_INIT = 500;     // 中庭预设面积
    public static final float ATRIUM_POS_R = 5;           // 中庭中心点判定范围
    public static final float ATRIUM_CTRL_R = 3;          // 中庭控制点判定范围

    public static final float STRUCTURE_DIST = 8.4f;      // 柱距预设值
    public static final int STRUCTURE_GRID_NUM = 2;       // 柱网体系预设数量
    public static final int STRUCTURE_GRID_MAX = 4;       // 柱网体系最大数量
    public static final float STRUCTURE_CTRL_R = 5;       // 柱网控制点判定范围


    public static double ATRIUM_R = 10;

    public static double[] SHOP_SPAN_THRESHOLD = {7, 9};

    public static double EVACUATION_WIDTH = 2.4;
    public static double EVACUATION_DIST = 70;

    // cp5 constants
    public static final int STATUS_W = 120;
    public static final int CONTROLLER_W = 180;         // controller宽度
    public static final int CONTROLLER_H = 30;          // controller高度

    // cp5 button id
    // edit status
    public static final int E_SITE_BOUNDARY = 0;
    public static final int E_MAIN_TRAFFIC = 1;
    public static final int E_RAW_ATRIUM = 2;
    public static final int E_PUBLIC_SPACE = 3;
    public static final int E_STRUCTURE_GRID = 4;
    public static final int E_SHOP_EDIT = 5;
    public static final int E_MAIN_CORRIDOR = 6;
    public static final int E_ESCALATOR = 7;
    public static final int E_EVACUATION = 8;
    // 0
    public static final int BUTTON_SWITCH_BOUNDARY = 10;
    public static final int SLIDER_REDLINE_DIST = 28;
    public static final int SLIDER_SITE_BUFFER = 29;
    // 1
    public static final int BUTTON_DELETE_INNERNODE = 11;
    public static final int BUTTON_DELETE_ENTRYNODE = 12;
    public static final int SLIDER_TRAFFIC_WIDTH = 13;
    // 2
    public static final int BUTTON_CURVE_ATRIUM = 14;
    public static final int BUTTON_DELETE_ATRIUM = 26;
    public static final int SLIDER_ATRIUM_ANGLE = 15;
    public static final int SLIDER_ATRIUM_AREA = 16;
    public static final int LIST_ATRIUM_FACTORY = 17;
    public static final int ITEM_A_TRIANGLE = 18;
    public static final int ITEM_A_SQUARE = 19;
    public static final int ITEM_A_TRAPEZOID = 20;
    public static final int ITEM_A_PENTAGON = 21;
    public static final int ITEM_A_HEXAGON1 = 22;
    public static final int ITEM_A_HEXAGON2 = 23;
    public static final int ITEM_A_L_SHAPE = 24;
    public static final int ITEM_A_OCTAGON = 25;
    // 3
    public static final int SLIDER_BUFFER_DIST = 27;
    // 4
    public static final int SLIDER_COLUMN_DIST = 31;
    public static final int SLIDER_GRID_ANGLE = 32;
    public static final int LIST_GRID_NUM = 30;
    // 5
    public static final int BUTTON_UNION_CELLS = 33;
}
