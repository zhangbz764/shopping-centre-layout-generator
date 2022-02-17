package mallElementNew;

import basicGeometry.ZPoint;
import math.ZGeoMath;
import wblut.geom.WB_Point;

/**
 * the creator factory of raw atriums
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/2/17
 * @time 22:19
 */
public class AtriumRawFactory {

    /**
     * create a triangle atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return void
     */
    public static AtriumRaw createAtriumTri(WB_Point base, double area, boolean curve) {
        double ratio = Math.sqrt((area * 2) / (2 * 7.28));
        WB_Point[] pts = new WB_Point[3];
        pts[0] = new WB_Point(base.xd(), base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() + 3.64 * ratio, base.yd() + ratio);
        pts[2] = new WB_Point(base.xd() - 3.64 * ratio, base.yd() + ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }

    /**
     * create a square atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return mallElementNew.AtriumRaw
     */
    public static AtriumRaw createAtriumSq(WB_Point base, double area, boolean curve) {
        double d = Math.sqrt(area) * 0.5;
        WB_Point[] pts = new WB_Point[4];
        pts[0] = new WB_Point(base.xd() - d, base.yd() - d);
        pts[1] = new WB_Point(base.xd() + d, base.yd() - d);
        pts[2] = new WB_Point(base.xd() + d, base.yd() + d);
        pts[3] = new WB_Point(base.xd() - d, base.yd() + d);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }

    /**
     * create a trapezoid atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return mallElementNew.AtriumRaw
     */
    public static AtriumRaw createAtriumTra(WB_Point base, double area, boolean curve) {
        double ratio = Math.sqrt(area / 4.098);
        WB_Point[] pts = new WB_Point[4];
        pts[0] = new WB_Point(base.xd() - 1.366 * ratio, base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() + 1.366 * ratio, base.yd());
        pts[2] = new WB_Point(base.xd() + 1.366 * ratio, base.yd() + ratio);
        pts[3] = new WB_Point(base.xd() - 1.366 * ratio, base.yd() + ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }

    /**
     * create a pentagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return mallElementNew.AtriumRaw
     */
    public static AtriumRaw createAtriumPen(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[5];
        temp[0] = new ZPoint(base.xd(), base.yd() - 3);
        temp[1] = new ZPoint(base.xd() + 5.4, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 5.4, base.yd() + 3);
        temp[3] = new ZPoint(base.xd() - 5.4, base.yd() + 3);
        temp[4] = new ZPoint(base.xd() - 5.4, base.yd() - 1);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[5];
        pts[0] = new WB_Point(base.xd(), base.yd() - 3 * ratio);
        pts[1] = new WB_Point(base.xd() + 5.4 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 5.4 * ratio, base.yd() + 3 * ratio);
        pts[3] = new WB_Point(base.xd() - 5.4 * ratio, base.yd() + 3 * ratio);
        pts[4] = new WB_Point(base.xd() - 5.4 * ratio, base.yd() - ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }

    /**
     * create a hexagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return mallElementNew.AtriumRaw
     */
    public static AtriumRaw createAtriumHex(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[6];
        temp[0] = new ZPoint(base.xd(), base.yd() - 2);
        temp[1] = new ZPoint(base.xd() + 2.7, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 2.7, base.yd() + 1);
        temp[3] = new ZPoint(base.xd(), base.yd() + 2);
        temp[4] = new ZPoint(base.xd() - 2.7, base.yd() + 1);
        temp[5] = new ZPoint(base.xd() - 2.7, base.yd() - 1);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[6];
        pts[0] = new WB_Point(base.xd(), base.yd() - 2 * ratio);
        pts[1] = new WB_Point(base.xd() + 2.7 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 2.7 * ratio, base.yd() + ratio);
        pts[3] = new WB_Point(base.xd(), base.yd() + 2 * ratio);
        pts[4] = new WB_Point(base.xd() - 2.7 * ratio, base.yd() + ratio);
        pts[5] = new WB_Point(base.xd() - 2.7 * ratio, base.yd() - ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }

    /**
     * create a hexagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return mallElementNew.AtriumRaw
     */
    public static AtriumRaw createAtriumHex2(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[6];
        temp[0] = new ZPoint(base.xd() - 2, base.yd());
        temp[1] = new ZPoint(base.xd() - 1, base.yd() - 1.67);
        temp[2] = new ZPoint(base.xd() + 1, base.yd() - 1.67);
        temp[3] = new ZPoint(base.xd() + 2, base.yd());
        temp[4] = new ZPoint(base.xd() + 1, base.yd() + 1.67);
        temp[5] = new ZPoint(base.xd() - 1, base.yd() + 1.67);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[6];
        pts[0] = new WB_Point(base.xd() - 2 * ratio, base.yd());
        pts[1] = new WB_Point(base.xd() - ratio, base.yd() - 1.67 * ratio);
        pts[2] = new WB_Point(base.xd() + ratio, base.yd() - 1.67 * ratio);
        pts[3] = new WB_Point(base.xd() + 2 * ratio, base.yd());
        pts[4] = new WB_Point(base.xd() + ratio, base.yd() + 1.67 * ratio);
        pts[5] = new WB_Point(base.xd() - ratio, base.yd() + 1.67 * ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }

    /**
     * create a L-Shape atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return mallElementNew.AtriumRaw
     */
    public static AtriumRaw createAtriumLS(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[7];
        temp[0] = new ZPoint(base.xd(), base.yd() - 4);
        temp[1] = new ZPoint(base.xd() + 3.4, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 7.4, base.yd() - 1);
        temp[3] = new ZPoint(base.xd() + 7.4, base.yd() + 4);
        temp[4] = new ZPoint(base.xd(), base.yd() + 4);
        temp[5] = new ZPoint(base.xd() - 3.4, base.yd() + 2);
        temp[6] = new ZPoint(base.xd() - 3.4, base.yd() - 2);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[7];
        pts[0] = new WB_Point(base.xd(), base.yd() - 4 * ratio);
        pts[1] = new WB_Point(base.xd() + 3.4 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 7.4 * ratio, base.yd() - ratio);
        pts[3] = new WB_Point(base.xd() + 7.4 * ratio, base.yd() + 4 * ratio);
        pts[4] = new WB_Point(base.xd(), base.yd() + 4 * ratio);
        pts[5] = new WB_Point(base.xd() - 3.4 * ratio, base.yd() + 2 * ratio);
        pts[6] = new WB_Point(base.xd() - 3.4 * ratio, base.yd() - 2 * ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }

    /**
     * create a octagon atrium
     *
     * @param base  base (center) point
     * @param area  initial area
     * @param curve curve of polygon
     * @return mallElementNew.AtriumRaw
     */
    public static AtriumRaw createAtriumOct(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[8];
        temp[0] = new ZPoint(base.xd() - 2.732, base.yd() - 1);
        temp[1] = new ZPoint(base.xd() - 1, base.yd() - 2);
        temp[2] = new ZPoint(base.xd() + 1, base.yd() - 2);
        temp[3] = new ZPoint(base.xd() + 2.732, base.yd() - 1);
        temp[4] = new ZPoint(base.xd() + 2.732, base.yd() + 1);
        temp[5] = new ZPoint(base.xd() + 1, base.yd() + 2);
        temp[6] = new ZPoint(base.xd() - 1, base.yd() + 2);
        temp[7] = new ZPoint(base.xd() - 2.732, base.yd() + 1);
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[8];
        pts[0] = new WB_Point(base.xd() - 2.732 * ratio, base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() - ratio, base.yd() - 2 * ratio);
        pts[2] = new WB_Point(base.xd() + ratio, base.yd() - 2 * ratio);
        pts[3] = new WB_Point(base.xd() + 2.732 * ratio, base.yd() - ratio);
        pts[4] = new WB_Point(base.xd() + 2.732 * ratio, base.yd() + ratio);
        pts[5] = new WB_Point(base.xd() + ratio, base.yd() + 2 * ratio);
        pts[6] = new WB_Point(base.xd() - ratio, base.yd() + 2 * ratio);
        pts[7] = new WB_Point(base.xd() - 2.732 * ratio, base.yd() + ratio);

        AtriumRaw a;
        if (curve) {
            a = new AtriumRaw(base, pts, true);
            a.scaleShapeByArea(area);
        } else {
            a = new AtriumRaw(base, pts, false);
        }
        return a;
    }
}
