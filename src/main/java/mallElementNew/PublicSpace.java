package mallElementNew;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import main.MallConst;
import math.ZGeoMath;
import math.ZMath;
import org.locationtech.jts.geom.*;
import transform.ZTransform;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/8/9
 * @time 17:03
 */
public class PublicSpace {
    private List<Atrium> atriums;
    private List<Corridor> corridors;

    /* ------------- constructor ------------- */

    public PublicSpace(Polygon[] atriumShapes, LineString mainTraffic) {

        initCorridorLines(atriumShapes, mainTraffic);
    }

    /* ------------- member function ------------- */

    /**
     * initialize main traffic corridors
     *
     * @param atriumShapes atrium shapes
     * @param mainTraffic  main traffic curve
     * @return void
     */
    public void initCorridorLines(Polygon[] atriumShapes, LineString mainTraffic) {
        // atriums are already sorted
        this.atriums = new ArrayList<>();
        for (Polygon atriumShape : atriumShapes) {
            atriums.add(new Atrium(atriumShape));
        }
        this.corridors = new ArrayList<>();
        for (int i = 0; i < atriumShapes.length - 1; i++) {
            // neighbor atrium polygons
            Polygon shape1 = atriumShapes[i];
            Polygon shape2 = atriumShapes[i + 1];
            LineString a1_ls = ZTransform.PolygonToLineString(shape1).get(0);
            LineString a2_ls = ZTransform.PolygonToLineString(shape2).get(0);
            Geometry intersection1 = a1_ls.intersection(mainTraffic);
            Geometry intersection2 = a2_ls.intersection(mainTraffic);

            // to ensure each atrium has 2 intersection points with main traffic
            if (intersection1.getGeometryType().equals("MultiPoint")
                    && intersection2.getGeometryType().equals("MultiPoint")
                    && intersection1.getNumGeometries() == 2
                    && intersection2.getNumGeometries() == 2
            ) {
                ZPoint[] pts = new ZPoint[]{
                        new ZPoint((Point) intersection1.getGeometryN(0)),
                        new ZPoint((Point) intersection1.getGeometryN(1)),
                        new ZPoint((Point) intersection2.getGeometryN(0)),
                        new ZPoint((Point) intersection2.getGeometryN(1))
                };
                double[] distAlongEdge = new double[]{
                        ZGeoMath.distAlongEdge(pts[0], pts[2], mainTraffic),
                        ZGeoMath.distAlongEdge(pts[0], pts[3], mainTraffic),
                        ZGeoMath.distAlongEdge(pts[1], pts[2], mainTraffic),
                        ZGeoMath.distAlongEdge(pts[1], pts[3], mainTraffic),
                };

                // find the closest 2 points
                int minIndex = ZMath.getMinIndex(distAlongEdge);
                ZPoint p1 = pts[(int) (minIndex * 0.5)];
                ZPoint p2 = pts[minIndex % 2 == 0 ? 2 : 3];

                // find middle points between them, and get divide line
                ZPoint mid = ZGeoMath.pointOnEdgeByDist(p1, mainTraffic, distAlongEdge[minIndex] * 0.5)[0];
                int[] edgeID = ZGeoMath.pointOnWhichEdgeIndices(mid, mainTraffic);
                ZPoint edgeVec = new ZPoint(mainTraffic.getCoordinateN(edgeID[1])).sub(new ZPoint(mainTraffic.getCoordinateN(edgeID[0])));
                ZPoint divDir = edgeVec.perpVec().normalize();
                ZLine divLine = new ZLine(mid.add(divDir.scaleTo(10)), mid.add(divDir.scaleTo(-10)));

                // create Atrium and Corridor
                Corridor c = new Corridor(divLine, MallConst.CORRIDOR_WIDTH_INIT);
                c.setNeighborAtriums(new Atrium[]{atriums.get(i), atriums.get(i + 1)});
                atriums.get(i).addNeighborCorridor(1, c);
                atriums.get(i + 1).addNeighborCorridor(0, c);
                corridors.add(c);
            }
        }

        // trim all atriums by the offset lines of corridors
        for (Atrium a : atriums) {
            Corridor[] corridors = a.neighbor;
            Corridor c0 = corridors[0];
            Corridor c1 = corridors[1];
            Coordinate[] coords = a.initShape.getCoordinates();

            if (c0 != null) {
                trimAtrium(coords, c0, 1);
            }
            if (c1 != null) {
                trimAtrium(coords, c1, 0);
            }

            a.setCurrShape(ZFactory.jtsgf.createPolygon(coords));
        }
    }

    /**
     * update 1 position of the corridors
     *
     * @param corridorID index of the updated corridors
     * @param newPos     new central line of the corridor
     * @return void
     */
    public void updateCorridorPos(int corridorID, ZLine newPos) {
        Corridor c = corridors.get(corridorID);
        Atrium[] atriums = c.neighbor;
        c.setCentralLine(newPos);

        Atrium a0 = atriums[0];
        Atrium a1 = atriums[1];

        Coordinate[] coords0 = a0.currShape.getCoordinates();
        trimAtrium(coords0, c, 1);
        a0.setCurrShape(ZFactory.jtsgf.createPolygon(coords0));
        Coordinate[] coords1 = a1.currShape.getCoordinates();
        trimAtrium(coords1, c, 0);
        a1.setCurrShape(ZFactory.jtsgf.createPolygon(coords1));
    }

    /**
     * core function trim the atrium shape to neighbor corridor
     *
     * @param coords   coordinates of the shape
     * @param c        corridor
     * @param offsetID which offset line of corridor should perform the trim
     * @return void
     */
    private void trimAtrium(Coordinate[] coords, Corridor c, int offsetID) {
        // find the closest segment
        double[] distToCentralLine1 = new double[coords.length - 1];
        for (int j = 0; j < coords.length - 1; j++) {
            WB_Point center = new WB_Point(
                    0.5 * (coords[j].getX() + coords[j + 1].getX()),
                    0.5 * (coords[j].getY() + coords[j + 1].getY())
            );
            distToCentralLine1[j] = WB_GeometryOp.getDistance2D(center, c.centralLine.toWB_Line());
        }
        int min = ZMath.getMinIndex(distToCentralLine1);
        // set the closest segment to intersection points
        ZLine former = new ZLine(
                coords[min == 0 ? coords.length - 2 : min - 1],
                coords[min]
        );
        ZPoint newPt1 = ZGeoMath.simpleLineElementsIntersect2D(
                former, "line", c.offset[offsetID], "line"
        );
        coords[min] = newPt1.toJtsCoordinate();
        if (min == 0) {
            coords[coords.length - 1] = coords[min];
        }
        ZLine next = new ZLine(
                coords[min + 1],
                coords[(min + 2) % (coords.length - 1)]
        );
        ZPoint newPt2 = ZGeoMath.simpleLineElementsIntersect2D(
                next, "line", c.offset[offsetID], "line"
        );
        coords[min + 1] = newPt2.toJtsCoordinate();
        if (min + 1 == coords.length - 1) {
            coords[0] = coords[min + 1];
        }
    }

    /* ------------- setter & getter ------------- */

    public List<ZLine> getCorridorsLines() {
        List<ZLine> lines = new ArrayList<>();
        for (Corridor c : corridors) {
            lines.add(c.centralLine);
        }
        return lines;
    }

    public List<List<ZLine>> getOffsets() {
        List<List<ZLine>> offsets = new ArrayList<>();
        for (Corridor c : corridors) {
            offsets.add(Arrays.asList(c.offset));
        }
        return offsets;
    }

    public Polygon[] getAtriumShapes() {
        Polygon[] shapes = new Polygon[atriums.size()];
        for (int i = 0; i < atriums.size(); i++) {
            shapes[i] = atriums.get(i).currShape;
        }
        return shapes;
    }

    public double[] getAtriumAreas() {
        double[] areas = new double[atriums.size()];
        for (int i = 0; i < atriums.size(); i++) {
            areas[i] = atriums.get(i).area;
        }
        return areas;
    }

    /* ------------- inner class: Corridor ------------- */

    private static class Corridor {
        private ZLine centralLine;
        private ZLine[] offset;
        private double width;
        private Atrium[] neighbor;

        private Corridor(ZLine centralLine, double width) {
            this.centralLine = centralLine;
            this.width = width;
            this.offset = centralLine.offset2D(width);
        }

        private void setCentralLine(ZLine centralLine) {
            this.centralLine = centralLine;
            this.offset = centralLine.offset2D(width);
        }

        private void setNeighborAtriums(Atrium[] as) {
            neighbor = as;

            // switch the order of the offset lines
            ZLine l1 = offset[0];
            ZLine l2 = offset[1];
            ZPoint[] test = new ZLine(
                    new ZPoint(neighbor[0].currShape.getCentroid()),
                    l1.getCenter()
            ).toLinePD();
            if (ZGeoMath.checkLineSegmentIntersection(l2.toLinePD(), test)) {
                offset = new ZLine[]{l2, l1};
            }
        }
    }

    /* ------------- inner class: Atrium ------------- */

    private static class Atrium {
        private Polygon initShape;
        private Polygon currShape;
        private double area;
        private Corridor[] neighbor;

        private Atrium(Polygon _initShape) {
            this.initShape = _initShape;
            this.currShape = _initShape;
            this.area = _initShape.getArea();
            this.neighbor = new Corridor[]{null, null};
        }

        private void addNeighborCorridor(int index, Corridor c) {
            this.neighbor[index] = c;
        }

        public void setCurrShape(Polygon currShape) {
            this.currShape = currShape;
            this.area = currShape.getArea();
        }
    }
}