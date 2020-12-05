package main;

import processing.core.PApplet;

public class Main {
    public static void main(String[] args) {
        /* 主程序 */
        PApplet.main("main.Test");

        /* 测试极角排序、角平分线等向量运算 */
//        PApplet.main("main.demoTests.TestPolarAngle");

        /* 测试jts几何图形布尔关系 */
//        PApplet.main("main.demoTests.TestGeoRelation");

        /* 测试多边形等分、沿多边形边找点、多边形边线offset */
//        PApplet.main("main.demoTests.TestPolySplit");

        /* 测试hemesh里的最近点计算、线段trim和extend计算、op里的检测线段二维相交 */
//        PApplet.main("main.demoTests.TestDistCloset");

        /* 测试ZSkeleton */
//        PApplet.main("main.demoTests.TestSkeleton");

        /* 测试hemesh的mesh以及union */
//        PApplet.main("main.demoTests.TestHe_Mesh");

        /* 测试jts的convexhull、找凹点、直线多边形交点及排序 */
//        PApplet.main("main.demoTests.TestConvexHull");

        /* 测试campskeleton带洞 */
//        PApplet.main("main.demoTests.TestCampSkeleton");
    }
}
