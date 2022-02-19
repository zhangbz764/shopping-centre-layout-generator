package mallWeb;

import basicGeometry.ZFactory;
import geometry.Plane;
import geometry.Segments;
import geometry.Vertices;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.math.WB_Epsilon;
import wblut.math.WB_M44;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * description
 *
 * @author MO Yichen, ZHANG Baizhou
 * @project shopping_mall
 * @date 2022/2/16
 * @time 16:12
 */
public class JTSConverter {
    public JTSConverter() {

    }


    /**
     * description
     *
     * @param vs
     * @return java.util.List<org.locationtech.jts.geom.Coordinate>
     */
    public static List<Coordinate> toCoordinate(Vertices vs) {
        List<Coordinate> pts = new ArrayList<>();
        int size = vs.getSize();
        int count = vs.getCoordinates().size() / size;

        for (int i = 0; i < count; ++i) {
            double[] pt = new double[size];

            Coordinate c = new Coordinate();
            for (int j = 0; j < size; ++j) {
                pt[j] = (Double) vs.getCoordinates().get(i * size + j);
                c.setOrdinate(j, pt[j]);
            }

            pts.add(c);
        }

        return pts;
    }

    /**
     * description
     *
     * @param pts
     * @param size
     * @return geometry.Vertices
     */
    public static Vertices toVertices(List<Coordinate> pts, int size) {
        Vertices vs = new Vertices();
        vs.setSize(size);
        List<Double> coords = new ArrayList<>();

        Iterator<Coordinate> iterator = pts.iterator();
        while (iterator.hasNext()) {
            Coordinate pt = iterator.next();

            for (int i = 0; i < size; ++i) {
                coords.add(pt.getOrdinate(i));
            }
        }

        vs.setCoordinates(coords);
        return vs;
    }

    /**
     * description
     *
     * @param p
     * @return org.locationtech.jts.geom.Polygon
     */
    public static Polygon toPolygon(Plane p) {
        Coordinate p1 = new Coordinate(-0.5D, -0.5D, 0.0D);
        Coordinate p2 = new Coordinate(0.5D, -0.5D, 0.0D);
        Coordinate p3 = new Coordinate(0.5D, 0.5D, 0.0D);
        Coordinate p4 = new Coordinate(-0.5D, 0.5D, 0.0D);
        WB_M44 T = getMatrix(p.getMatrix());
        p1 = applyAsCoordinate(p1, T);
        p2 = applyAsCoordinate(p2, T);
        p3 = applyAsCoordinate(p3, T);
        p4 = applyAsCoordinate(p4, T);
        return ZFactory.jtsgf.createPolygon(new Coordinate[]{p1, p2, p3, p4});
    }

    /**
     * description
     *
     * @param s
     * @return org.locationtech.jts.geom.Polygon
     */
    public static Polygon toPolygon(Segments s) {
        int size = s.getSize();
        int count = s.getCoordinates().size() / size;
        Coordinate[] points = new Coordinate[count];

        for (int i = 0; i < count; ++i) {
            double[] pt = new double[size];

            Coordinate c = new Coordinate();
            for (int j = 0; j < size; ++j) {
                pt[j] = (Double) s.getCoordinates().get(i * size + j);
                c.setOrdinate(j, pt[j]);
            }

            points[i] = c;
        }

        return ZFactory.jtsgf.createPolygon(points);
    }

    /**
     * description
     *
     * @param s
     * @return org.locationtech.jts.geom.LineString
     */
    public static LineString toLineString(Segments s) {
        int size = s.getSize();
        int count = s.getCoordinates().size() / size;
        Coordinate[] points = new Coordinate[count];

        for (int i = 0; i < count; ++i) {
            double[] pt = new double[size];

            Coordinate c = new Coordinate();
            for (int j = 0; j < size; ++j) {
                pt[j] = (Double) s.getCoordinates().get(i * size + j);
                c.setOrdinate(j, pt[j]);
            }

            points[i] = c;
        }

        return ZFactory.jtsgf.createLineString(points);
    }

    /**
     * description
     *
     * @param ply
     * @return geometry.Segments
     */
    public static Segments toSegments(Polygon ply) {
        Segments segments = new Segments();
        List<Double> positions = new ArrayList<>();
        int size = 3;
        Coordinate[] coords = ply.getCoordinates();

        for (int i = 0; i < coords.length; ++i) {
            Coordinate c = coords[i];

            for (int j = 0; j < size; ++j) {
                double ordinate = c.getOrdinate(j);
                if (Double.isNaN(ordinate)) {
                    positions.add(0D);
                } else {
                    positions.add(c.getOrdinate(j));
                }
            }
        }

        segments.setCoordinates(positions);
        segments.setSize(size);
        segments.setClosed(true);
        return segments;
    }

    /**
     * description
     *
     * @param ls
     * @return geometry.Segments
     */
    public static Segments toSegments(LineString ls) {
        Segments segments = new Segments();
        List<Double> positions = new ArrayList<>();
        int size = 3;
        Coordinate[] coords = ls.getCoordinates();

        for (int i = 0; i < coords.length; ++i) {
            Coordinate c = coords[i];

            for (int j = 0; j < size; ++j) {
                double ordinate = c.getOrdinate(j);
                if (Double.isNaN(ordinate)) {
                    positions.add(0D);
                } else {
                    positions.add(c.getOrdinate(j));
                }
            }
        }

        segments.setCoordinates(positions);
        segments.setSize(size);
        segments.setClosed(true);
        return segments;
    }

    /* ------------- utils ------------- */

    /**
     * description
     *
     * @param matrix
     * @return wblut.math.WB_M44
     */
    private static WB_M44 getMatrix(List<Double> matrix) {
        return new WB_M44((Double) matrix.get(0), (Double) matrix.get(4), (Double) matrix.get(8), (Double) matrix.get(12), (Double) matrix.get(1), (Double) matrix.get(5), (Double) matrix.get(9), (Double) matrix.get(13), (Double) matrix.get(2), (Double) matrix.get(6), (Double) matrix.get(10), (Double) matrix.get(14), (Double) matrix.get(3), (Double) matrix.get(7), (Double) matrix.get(11), (Double) matrix.get(15));
    }

    /**
     * description
     *
     * @param p
     * @param T
     * @return org.locationtech.jts.geom.Coordinate
     */
    private static Coordinate applyAsCoordinate(Coordinate p, WB_M44 T) {
        double x = T.m11 * p.getX() + T.m12 * p.getY() + T.m13 * p.getZ() + T.m14;
        double y = T.m21 * p.getX() + T.m22 * p.getY() + T.m23 * p.getZ() + T.m24;
        double z = T.m31 * p.getX() + T.m32 * p.getY() + T.m33 * p.getZ() + T.m34;
        double wp = T.m41 * p.getX() + T.m42 * p.getY() + T.m43 * p.getZ() + T.m44;
        if (WB_Epsilon.isZero(wp)) {
            return new Coordinate(x, y, z);
        } else {
            wp = 1.0D / wp;
            return new Coordinate(x * wp, y * wp, z * wp);
        }
    }
}
