package mallParameters;

/**
 * constants for the local version of the shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/4/17
 * @time 23:34
 */
public class MallConst {

    /* shopping mall constants */

    public static final int FLOOR_TOTAL = 5;              // 总层数
    // 0
    public static final float SITE_REDLINEDIST_MAX = 20;  // 场地红线最大值
    public static final float SITE_BUFFER_MIN = 62;       // 场地退界最小值
    public static final float SITE_BUFFER_MAX = 70;       // 场地退界最大值
    public static final double BOUNDARY_NODE_R = 5;       // 轮廓控制点判定范围
    // 1
    public static final float TRAFFIC_BUFFER_DIST_MIN = 6; // 主路径预设偏移距离
    public static final float TRAFFIC_BUFFER_DIST_MAX = 10; // 主路径预设偏移距离
    public static final double TRAFFIC_NODE_R = 5;        // 主路径控制点判定范围
    public static final float ATRIUM_AREA_INIT = 500;     // 中庭预设面积
    public static final float ATRIUM_AREA_MAX = 1500;     // 最大中庭面积
    public static final float ATRIUM_AREA_MIN = 200;      // 最小中庭面积
    public static final float ATRIUM_POS_R = 5;           // 中庭中心点判定范围
    public static final float ATRIUM_CTRL_R = 3;          // 中庭控制点判定范围
    // 2
    public static final float CORRIDOR_WIDTH_INIT = 6f;      // 空中走廊预设宽度
    public static final float CORRIDOR_WIDTH_MAX = 10f;  // 空中走廊最大宽度
    public static final float CORRIDOR_WIDTH_MIN = 2.4f;  // 空中走廊最小宽度
    public static final double CORRIDOR_NODE_R = 5;        // 走廊控制点判定范围
    // 3
    public static final float PUBLIC_BUFFER_DIST_INIT = 3.6f; // 中央交通空间偏移预设距离
    public static final float PUBLIC_BUFFER_DIST_MAX = 6f;  // 中央交通空间偏移最大距离
    public static final float PUBLIC_BUFFER_DIST_MIN = 1.2f;    // 中央交通空间偏移最小距离
    public static final double PUBLIC_SPACE_NODE_R = 5;       // 中央交通空间控制点范围
    public static final float ATRIUM_ROUND_RADIUS_INIT = 1.5f;
    public static final float ATRIUM_ROUND_RADIUS_MAX = 5f;
    public static final float ATRIUM_ROUND_RADIUS_MIN = 0.5f;
    // 4
    public static final double STRUCTURE_MODEL = 8.4;     // 柱距预设值8.4m
    public static final double STRUCTURE_MODEL_2 = 9;     // 柱距预设值9m
    public static final int STRUCTURE_GRID_NUM = 2;       // 柱网体系预设数量
    public static final int STRUCTURE_GRID_MAX = 4;       // 柱网体系最大数量
    public static final float STRUCTURE_CTRL_R = 5;       // 柱网控制点判定范围// 中庭倒角半径
    // 5
    public static final double ESCALATOR_LENGTH = 13;     // 扶梯长度
    public static final double ESCALATOR_WIDTH = 3.2;     // 扶梯宽度
    public static final float ESCALATOR_DIST_MAX = 50;    // 扶梯服务半径上限
    public static final float ESCALATOR_DIST_MIN = 30;    // 扶梯服务半径上限

    public static double ATRIUM_R = 10;

    public static double[] SHOP_SPAN_THRESHOLD = {7, 9};

    public static double EVACUATION_WIDTH = 2.4;
    public static double EVACUATION_DIST = 60;
    public static double STAIRWAY1_LENGTH = 33.6;
    public static double STAIRWAY1_WIDTH = 4;
    public static double STAIRWAY2_LENGTH = 16.8;
    public static double STAIRWAY2_WIDTH = 8.4;
    public static double STAIRWAY_RADIUS = 2;

    /* cp5 constants */

    public static final int STATUS_W = 120;
    public static final int CONTROLLER_W = 180;           // controller宽度
    public static final int CONTROLLER_H = 30;            // controller高度

    /* number of status */

    public static final int STATUS_NUM = 8;

    /* cp5 button id */

    // edit status
    public static final int E_SITE_BOUNDARY = 0;
    public static final int E_TRAFFIC_ATRIUM = 1;
    public static final int E_MAIN_CORRIDOR = 2;
    public static final int E_PUBLIC_SPACE = 3;
    public static final int E_STRUCTURE_GRID = 4;
    public static final int E_SHOP_EDIT = 5;
    public static final int E_ESCALATOR = 6;
    public static final int E_EVACUATION = 7;

    public static final int INIT_FLAG = 0;

    // 0
    public static final int BUTTON_SWITCH_BOUNDARY = 100;
    public static final int SLIDER_OFFSET_DIST = 101;
    public static final int SLIDER_REDLINE_DIST = 102;
    // 1
//    public static final int BUTTON_DELETE_INNERNODE = 11;
//    public static final int BUTTON_DELETE_ENTRYNODE = 12;
    public static final int BUTTON_TRAFFIC_CONTROLLERS = 12;
    public static final int SLIDER_TRAFFIC_WIDTH = 103;
    public static final int BUTTON_CURVE_ATRIUM = 104;
    public static final int BUTTON_DELETE_ATRIUM = 26;
    public static final int SLIDER_ATRIUM_ANGLE = 15;
    public static final int SLIDER_ATRIUM_AREA = 16;
    public static final int LIST_ATRIUM_FACTORY = 17;
    public static final int ITEM_A_TRIANGLE = 108;
    public static final int ITEM_A_SQUARE = 109;
    public static final int ITEM_A_TRAPEZOID = 110;
    public static final int ITEM_A_PENTAGON = 111;
    public static final int ITEM_A_HEXAGON1 = 112;
    public static final int ITEM_A_HEXAGON2 = 113;
    public static final int ITEM_A_LSHAPE = 114;
    public static final int ITEM_A_OCTAGON = 115;
    // 2
    public static final int SLIDER_CORRIDOR_WIDTH = 116;
    // 3
    public static final int BUTTON_DELETE_PUBLIC_NODE = 96;
    public static final int SLIDER_BUFFER_DIST = 117;
    public static final int BUTTON_ATRIUM_ROUND = 99;
    public static final int SLIDER_ROUND_RADIUS = 98;
    public static final int SLIDER_SMOOTH_TIMES = 97;

    // 4
    public static final int BUTTON_GRID_8 = 118;
    public static final int BUTTON_GRID_9 = 119;
    public static final int BUTTON_GRIDNUM_1 = 120;
    public static final int BUTTON_GRIDNUM_2 = 121;
    public static final int BUTTON_GRIDNUM_3 = 122;

    public static final int BUTTON_GRID_MODEL = 31;
    public static final int SLIDER_GRID_ANGLE = 32;
    public static final int LIST_GRID_NUM = 30;
    // 5
    public static final int BUTTON_UNION_CELLS = 123;
    // 6
    public static final int BUTTON_UPDATE_ESCALATOR = 124;
}
