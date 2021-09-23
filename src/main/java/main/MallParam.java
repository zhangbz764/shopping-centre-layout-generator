package main;

/**
 * shopping mall parameters
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/8/10
 * @time 15:15
 */
public class MallParam {
    public float siteRedLineDist;           // 场地红线距离
    public float siteBufferDist;            // 场地退界距离

    public float trafficBufferDist;         // 主路径偏移距离
    public float atriumAngle;               // 中庭旋转角度
    public float atriumArea;                // 中庭面积

    public float corridorWidth;             // 空中走廊宽度

    public float publicSpaceBufferDist;
    public float atriumRoundRadius;         // 中庭倒圆角半径
    public int atriumSmoothTimes;           // 中庭倒角次数

    /* ------------- constructor ------------- */

    public MallParam() {
        this.siteRedLineDist = 10;
        this.siteBufferDist = 67.2f;

        this.trafficBufferDist = 8;
        this.atriumAngle = 0;
        this.atriumArea = 500;

        this.corridorWidth = 6f;

        this.publicSpaceBufferDist = 3.6f;
        this.atriumRoundRadius = 1.5f;
        this.atriumSmoothTimes = 2;
    }
}