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
    public static int FLOOR_TOTAL = 5;              // 总层数

    public static double BOUNDARY_INTERACT_R = 5;   // 轮廓控制点判定范围

    public static double TRAFFIC_NODE_R = 5;
    public static double CONTROL_NODE_D = 2;
    public static double ATRIUM_R = 10;


    public static int CURVE_POINTS_1 = 16;
    public static int CURVE_POINTS_2 = 32;
    public static double SHOP_SPAN = 8.4;
    public static double[] SHOP_SPAN_THRESHOLD = {7, 9};

    public static double EVACUATION_WIDTH = 2.4;
    public static double EVACUATION_DIST = 70;
}
