package mallElementNew;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import main.MallConst;
import math.ZGeoMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2021/11/25
 * @time 13:52
 */
public class Stairway {
    private int type = 0;  // 0 -> series   1 -> parallel
    private ZPoint base;
    private Polygon bound;
    private List<ZLine> shapes;

    /* ------------- constructor ------------- */

    public Stairway(int _type, ZPoint _base, StructureGrid grid) {
        this.type = _type;
        this.base = _base;
        if (type == 1) {
//            generateSeries(grid);
        } else {
            generateParallel(grid);
        }
    }

    public Stairway(int _type, ZPoint _base, WB_Polygon boundary) {
        this.type = _type;
        this.base = _base;
        this.shapes = new ArrayList<>();

        int edgeIndex = ZGeoMath.pointOnWhichEdgeIndices(base, boundary)[0];
        ZLine edge = new ZLine(boundary.getSegment(edgeIndex));
        ZPoint edgeVec = edge.getDirectionNor();
        ZPoint perpVec = edgeVec.rotate2D(Math.PI * 0.5);
        Coordinate[] boundCoords = new Coordinate[5];
        if (type == 1) {
            ZPoint p0 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY1_LENGTH * -0.5));
            ZPoint p1 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY1_LENGTH * 0.5));
            ZPoint p2 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY1_LENGTH * 0.5)).add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH));
            ZPoint p3 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY1_LENGTH * -0.5)).add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH));
            boundCoords[0] = p0.toJtsCoordinate();
            boundCoords[1] = p1.toJtsCoordinate();
            boundCoords[2] = p2.toJtsCoordinate();
            boundCoords[3] = p3.toJtsCoordinate();
            boundCoords[4] = boundCoords[0];
            this.bound = ZFactory.jtsgf.createPolygon(boundCoords);

            // shape: div lines
            ZPoint div1Base = p0.add(edgeVec.scaleTo(MallConst.STAIRWAY1_LENGTH * 0.25));
            ZLine div1 = new ZLine(
                    div1Base,
                    div1Base.add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH))
            );
            ZPoint div2Base = p0.add(edgeVec.scaleTo(MallConst.STAIRWAY1_LENGTH * 0.5));
            ZLine div2 = new ZLine(
                    div2Base,
                    div2Base.add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH))
            );
            ZPoint div3Base = p0.add(edgeVec.scaleTo(MallConst.STAIRWAY1_LENGTH * 0.75));
            ZLine div3 = new ZLine(
                    div3Base,
                    div3Base.add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH))
            );
            shapes.add(div1);
            shapes.add(div2);
            shapes.add(div3);
            // shape: stairs
            ZLine cut1 = new ZLine(
                    div1Base.add(edgeVec.scaleTo(MallConst.STAIRWAY_RADIUS)),
                    div2Base.add(edgeVec.scaleTo(-MallConst.STAIRWAY_RADIUS))
            );
            List<ZPoint> stairBases1 = cut1.divide(20);
            for (ZPoint base : stairBases1) {
                shapes.add(new ZLine(
                        base,
                        base.add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH))
                ));
            }
            ZLine centerLine1 = new ZLine(
                    stairBases1.get(0).add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH * 0.5)),
                    stairBases1.get(stairBases1.size() - 1).add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH * 0.5))
            );
            shapes.add(centerLine1);
            ZLine cut2 = new ZLine(
                    div3Base.add(edgeVec.scaleTo(MallConst.STAIRWAY_RADIUS)),
                    p1.add(edgeVec.scaleTo(-MallConst.STAIRWAY_RADIUS))
            );
            List<ZPoint> stairBases2 = cut2.divide(20);
            for (ZPoint base : stairBases2) {
                shapes.add(new ZLine(
                        base,
                        base.add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH))
                ));
            }
            ZLine centerLine2 = new ZLine(
                    stairBases2.get(0).add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH * 0.5)),
                    stairBases2.get(stairBases2.size() - 1).add(perpVec.scaleTo(MallConst.STAIRWAY1_WIDTH * 0.5))
            );
            shapes.add(centerLine2);
        } else {
            ZPoint p0 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY2_LENGTH * -0.5));
            ZPoint p1 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY2_LENGTH * 0.5));
            ZPoint p2 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY2_LENGTH * 0.5)).add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH));
            ZPoint p3 = base.add(edgeVec.scaleTo(MallConst.STAIRWAY2_LENGTH * -0.5)).add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH));
            boundCoords[0] = p0.toJtsCoordinate();
            boundCoords[1] = p1.toJtsCoordinate();
            boundCoords[2] = p2.toJtsCoordinate();
            boundCoords[3] = p3.toJtsCoordinate();
            boundCoords[4] = boundCoords[0];
            this.bound = ZFactory.jtsgf.createPolygon(boundCoords);

            // shape: div lines
            ZPoint div1Base = p0.add(edgeVec.scaleTo(MallConst.STAIRWAY2_LENGTH * 0.5));
            ZLine div1 = new ZLine(
                    div1Base,
                    div1Base.add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH))
            );
            ZPoint div2Base = p1.add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH * 0.5));
            ZLine div2 = new ZLine(
                    div2Base,
                    div2Base.add(edgeVec.scaleTo(MallConst.STAIRWAY2_LENGTH * -0.5))
            );
            shapes.add(div1);
            shapes.add(div2);
            // shape: stairs
            ZLine cut1 = new ZLine(
                    div1Base.add(edgeVec.scaleTo(MallConst.STAIRWAY_RADIUS)),
                    p1.add(edgeVec.scaleTo(-MallConst.STAIRWAY_RADIUS))
            );
            List<ZPoint> stairBases1 = cut1.divide(20);
            for (ZPoint base : stairBases1) {
                shapes.add(new ZLine(
                        base,
                        base.add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH * 0.5))
                ));
            }
            ZLine centerLine1 = new ZLine(
                    stairBases1.get(0).add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH * 0.25)),
                    stairBases1.get(stairBases1.size() - 1).add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH * 0.25))
            );
            shapes.add(centerLine1);
            ZLine cut2 = new ZLine(
                    p2.add(edgeVec.scaleTo(-MallConst.STAIRWAY_RADIUS)),
                    p3.add(edgeVec.scaleTo(MallConst.STAIRWAY2_LENGTH * 0.5 + MallConst.STAIRWAY_RADIUS))
            );
            List<ZPoint> stairBases2 = cut2.divide(20);
            for (ZPoint base : stairBases2) {
                shapes.add(new ZLine(
                        base,
                        base.add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH * -0.5))
                ));
            }
            ZLine centerLine2 = new ZLine(
                    stairBases2.get(0).add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH * -0.25)),
                    stairBases2.get(stairBases2.size() - 1).add(perpVec.scaleTo(MallConst.STAIRWAY2_WIDTH * -0.25))
            );
            shapes.add(centerLine2);
        }

    }

    /* ------------- member function ------------- */

//    private void generateSeries(StructureGrid g) {
//        double distTo10 = WB_GeometryOp.getDistance2D(base.toWB_Point(), g.getLat12().get(0).toWB_Segment());
//        double distTo12 = WB_GeometryOp.getDistance2D(base.toWB_Point(), g.getLon10().get(0).toWB_Segment());
//
//        if (distTo10 < g.getLengthUnit12()) {
//            // 距离小于一个单元，靠近01边
//            int n = (int) (distTo12 / g.getLengthUnit10());
//            Coordinate[] coords = new Coordinate[5];
//            if (n < 1) {
//                // 012角
//                coords[0] = g.getGridNodes()[0][0].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[0][1].centerWith(g.getGridNodes()[0][0]).toJtsCoordinate();
//                coords[2] = g.getGridNodes()[4][1].centerWith(g.getGridNodes()[4][0]).toJtsCoordinate();
//                coords[3] = g.getGridNodes()[4][0].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else if (n > g.getLon10().size() - 4) {
//                // 103角
//                coords[0] = g.getGridNodes()[n][0].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[n][1].centerWith(g.getGridNodes()[n][0]).toJtsCoordinate();
//                coords[2] = g.getGridNodes()[n + 4][1].centerWith(g.getGridNodes()[n + 4][0]).toJtsCoordinate();
//                coords[3] = g.getGridNodes()[n + 4][0].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else {
//                // 中间
//                coords[0] = g.getGridNodes()[n - 2][0].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[n - 2][1].centerWith(g.getGridNodes()[n - 2][0]).toJtsCoordinate();
//                coords[2] = g.getGridNodes()[n + 2][1].centerWith(g.getGridNodes()[n + 2][0]).toJtsCoordinate();
//                coords[3] = g.getGridNodes()[n + 2][0].toJtsCoordinate();
//                coords[4] = coords[0];
//            }
//            evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//        } else if (distTo10 <= g.getLength12() && distTo10 > g.getLength12() - g.getLengthUnit12()) {
//            // 靠近23边
//            int size12 = g.getLat12().size();
//            int n = (int) (distTo12 / g.getLengthUnit10());
//            Coordinate[] coords = new Coordinate[5];
//            if (n < 1) {
//                coords[0] = g.getGridNodes()[0][size12 - 2].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[0][size12 - 1].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[2][size12 - 1].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[2][size12 - 2].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else if (n > g.getLon10().size() - 2) {
//                coords[0] = g.getGridNodes()[n][size12 - 2].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[n][size12 - 1].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[n + 2][size12 - 1].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[n + 2][size12 - 2].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else {
//                coords[0] = g.getGridNodes()[n - 1][size12 - 2].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[n - 1][size12 - 1].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[n + 1][size12 - 1].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[n + 1][size12 - 2].toJtsCoordinate();
//                coords[4] = coords[0];
//            }
//            evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//        } else if (distTo12 < g.getLengthUnit10()) {
//            // 靠近12边
//            int n = (int) (distTo10 / g.getLengthUnit12());
//            Coordinate[] coords = new Coordinate[5];
//            if (n < 1) {
//                coords[0] = g.getGridNodes()[1][0].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[0][0].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[0][2].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[1][2].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else if (n > g.getLat12().size() - 2) {
//                coords[0] = g.getGridNodes()[1][n - 1].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[0][n - 1].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[0][n + 1].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[1][n + 1].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else {
//                coords[0] = g.getGridNodes()[1][n].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[0][n].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[0][n + 2].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[1][n + 2].toJtsCoordinate();
//                coords[4] = coords[0];
//            }
//            evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//        } else if (distTo12 <= g.getLength10() && distTo12 > g.getLength10() - g.getLengthUnit10()) {
//            // 靠近30边
//            int size10 = g.getLon10().size();
//            int n = (int) (distTo10 / g.getLengthUnit12());
//            Coordinate[] coords = new Coordinate[5];
//            if (n < 1) {
//                coords[0] = g.getGridNodes()[size10 - 1][0].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[size10 - 2][0].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[size10 - 2][2].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[size10 - 1][2].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else if (n > g.getLat12().size() - 2) {
//                coords[0] = g.getGridNodes()[size10 - 1][n - 1].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[size10 - 2][n - 1].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[size10 - 2][n + 1].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[size10 - 1][n + 1].toJtsCoordinate();
//                coords[4] = coords[0];
//            } else {
//                coords[0] = g.getGridNodes()[size10 - 1][n].toJtsCoordinate();
//                coords[1] = g.getGridNodes()[size10 - 2][n].toJtsCoordinate();
//                coords[2] = g.getGridNodes()[size10 - 2][n + 2].toJtsCoordinate();
//                coords[3] = g.getGridNodes()[size10 - 1][n + 2].toJtsCoordinate();
//                coords[4] = coords[0];
//            }
//            evacRectTemp.add(ZFactory.jtsgf.createPolygon(coords));
//        }
//    }

    private void generateParallel(StructureGrid grid) {

    }

    /* ------------- setter & getter ------------- */

    public Polygon getBound() {
        return bound;
    }

    public ZPoint getBase() {
        return base;
    }

    public List<ZLine> getShapes() {
        return shapes;
    }

    /* ------------- draw ------------- */
}
