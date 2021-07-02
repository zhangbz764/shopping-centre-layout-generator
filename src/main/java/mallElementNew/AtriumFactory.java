package mallElementNew;

import basicGeometry.ZPoint;
import math.ZGeoMath;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/6/16
 * @time 15:54
 */
public class AtriumFactory {

    public static AtriumNew createAtrium3(WB_Point base, double area, boolean curve) {
        double ratio = Math.sqrt((area * 2) / (2 + 7.28));
        WB_Point[] pts = new WB_Point[4];
        pts[0] = new WB_Point(base.xd(), base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() + 3.64 * ratio, base.yd() + ratio);
        pts[2] = new WB_Point(base.xd() - 3.64 * ratio, base.yd() + ratio);
        pts[3] = pts[0];
        return new AtriumNew(base, pts, curve);
    }

    public static AtriumNew createAtrium4(WB_Point base, double area, boolean curve) {
        double d = Math.sqrt(area) * 0.5;
        WB_Point[] pts = new WB_Point[5];
        pts[0] = new WB_Point(base.xd() - d, base.yd() - d);
        pts[1] = new WB_Point(base.xd() + d, base.yd() - d);
        pts[2] = new WB_Point(base.xd() + d, base.yd() + d);
        pts[3] = new WB_Point(base.xd() - d, base.yd() + d);
        pts[4] = pts[0];

        return new AtriumNew(base, pts, curve);
    }

    public static AtriumNew createAtrium4_(WB_Point base, double area, boolean curve) {
        double ratio = Math.sqrt(area / 4.098);
        WB_Point[] pts = new WB_Point[5];
        pts[0] = new WB_Point(base.xd() - 1.366 * ratio, base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() + 1.366 * ratio, base.yd());
        pts[2] = new WB_Point(base.xd() + 1.366 * ratio, base.yd() + ratio);
        pts[3] = new WB_Point(base.xd() - 1.366 * ratio, base.yd() + ratio);
        pts[4] = pts[0];

        return new AtriumNew(base, pts, curve);
    }

    public static AtriumNew createAtrium5(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[6];
        temp[0] = new ZPoint(base.xd(), base.yd() - 3);
        temp[1] = new ZPoint(base.xd() + 5.4, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 5.4, base.yd() + 3);
        temp[3] = new ZPoint(base.xd() - 5.4, base.yd() + 3);
        temp[4] = new ZPoint(base.xd() - 5.4, base.yd() - 1);
        temp[5] = temp[0];
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[6];
        pts[0] = new WB_Point(base.xd(), base.yd() - 3 * ratio);
        pts[1] = new WB_Point(base.xd() + 5.4 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 5.4 * ratio, base.yd() + 3 * ratio);
        pts[3] = new WB_Point(base.xd() - 5.4 * ratio, base.yd() + 3 * ratio);
        pts[4] = new WB_Point(base.xd() - 5.4 * ratio, base.yd() - ratio);
        pts[5] = pts[0];
        return new AtriumNew(base, pts, curve);
    }

    public static AtriumNew createAtrium6(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[7];
        temp[0] = new ZPoint(base.xd(), base.yd() - 2);
        temp[1] = new ZPoint(base.xd() + 2.7, base.yd() - 1);
        temp[2] = new ZPoint(base.xd() + 2.7, base.yd() + 1);
        temp[3] = new ZPoint(base.xd(), base.yd() + 2);
        temp[4] = new ZPoint(base.xd() - 2.7, base.yd() + 1);
        temp[5] = new ZPoint(base.xd() - 2.7, base.yd() - 1);
        temp[6] = temp[0];
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[7];
        pts[0] = new WB_Point(base.xd(), base.yd() - 2 * ratio);
        pts[1] = new WB_Point(base.xd() + 2.7 * ratio, base.yd() - ratio);
        pts[2] = new WB_Point(base.xd() + 2.7 * ratio, base.yd() + ratio);
        pts[3] = new WB_Point(base.xd(), base.yd() + 2 * ratio);
        pts[4] = new WB_Point(base.xd() - 2.7 * ratio, base.yd() + ratio);
        pts[5] = new WB_Point(base.xd() - 2.7 * ratio, base.yd() - ratio);
        pts[6] = pts[0];
        return new AtriumNew(base, pts, curve);
    }

    public static AtriumNew createAtrium6_(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[7];
        temp[0] = new ZPoint(base.xd() - 2, base.yd());
        temp[1] = new ZPoint(base.xd() - 1, base.yd() - 1.67);
        temp[2] = new ZPoint(base.xd() + 1, base.yd() - 1.67);
        temp[3] = new ZPoint(base.xd() + 2, base.yd());
        temp[4] = new ZPoint(base.xd() + 1, base.yd() + 1.67);
        temp[5] = new ZPoint(base.xd() - 1, base.yd() + 1.67);
        temp[6] = temp[0];
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[7];
        pts[0] = new WB_Point(base.xd() - 2 * ratio, base.yd());
        pts[1] = new WB_Point(base.xd() - ratio, base.yd() - 1.67 * ratio);
        pts[2] = new WB_Point(base.xd() + ratio, base.yd() - 1.67 * ratio);
        pts[3] = new WB_Point(base.xd() + 2 * ratio, base.yd());
        pts[4] = new WB_Point(base.xd() + ratio, base.yd() + 1.67 * ratio);
        pts[5] = new WB_Point(base.xd() - ratio, base.yd() + 1.67 * ratio);
        pts[6] = pts[0];
        return new AtriumNew(base, pts, curve);
    }

    public static AtriumNew createAtrium7(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[8];
        temp[0] = new ZPoint(base.xd() - 2, base.yd());
        temp[1] = new ZPoint(base.xd() - 1, base.yd() - 1.67);
        temp[2] = new ZPoint(base.xd() + 1, base.yd() - 1.67);
        temp[3] = new ZPoint(base.xd() + 2, base.yd());
        temp[4] = new ZPoint(base.xd() + 1, base.yd() + 1.67);
        temp[5] = new ZPoint(base.xd() - 1, base.yd() + 1.67);
        temp[6] = new ZPoint(base.xd() - 1, base.yd() + 1.67);
        temp[7] = temp[0];
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[8];
        pts[0] = new WB_Point(base.xd() - 2 * ratio, base.yd());
        pts[1] = new WB_Point(base.xd() - ratio, base.yd() - 1.67 * ratio);
        pts[2] = new WB_Point(base.xd() + ratio, base.yd() - 1.67 * ratio);
        pts[3] = new WB_Point(base.xd() + 2 * ratio, base.yd());
        pts[4] = new WB_Point(base.xd() + ratio, base.yd() + 1.67 * ratio);
        pts[5] = new WB_Point(base.xd() - ratio, base.yd() + 1.67 * ratio);
        pts[6] = new WB_Point(base.xd() - ratio, base.yd() + 1.67 * ratio);
        pts[7] = pts[0];
        return new AtriumNew(base, pts, curve);
    }

    public static AtriumNew createAtrium8(WB_Point base, double area, boolean curve) {
        ZPoint[] temp = new ZPoint[9];
        temp[0] = new ZPoint(base.xd() - 2.732, base.yd() - 1);
        temp[1] = new ZPoint(base.xd() - 1, base.yd() - 2);
        temp[2] = new ZPoint(base.xd() + 1, base.yd() - 2);
        temp[3] = new ZPoint(base.xd() + 2.732, base.yd() - 1);
        temp[4] = new ZPoint(base.xd() + 2.732, base.yd() + 1);
        temp[5] = new ZPoint(base.xd() + 1, base.yd() + 2);
        temp[6] = new ZPoint(base.xd() - 1, base.yd() + 2);
        temp[7] = new ZPoint(base.xd() - 2.732, base.yd() + 1);
        temp[8] = temp[0];
        double areaTemp = ZGeoMath.areaFromPoints(temp);
        double ratio = Math.sqrt(area / areaTemp);

        WB_Point[] pts = new WB_Point[9];
        pts[0] = new WB_Point(base.xd() - 2.732 * ratio, base.yd() - ratio);
        pts[1] = new WB_Point(base.xd() - ratio, base.yd() - 2 * ratio);
        pts[2] = new WB_Point(base.xd() + ratio, base.yd() - 2 * ratio);
        pts[3] = new WB_Point(base.xd() + 2.732 * ratio, base.yd() - ratio);
        pts[4] = new WB_Point(base.xd() + 2.732 * ratio, base.yd() + ratio);
        pts[5] = new WB_Point(base.xd() + ratio, base.yd() + 2 * ratio);
        pts[6] = new WB_Point(base.xd() - ratio, base.yd() + 2 * ratio);
        pts[7] = new WB_Point(base.xd() - 2.732 * ratio, base.yd() + ratio);
        pts[8] = pts[0];
        return new AtriumNew(base, pts, curve);
    }
}
