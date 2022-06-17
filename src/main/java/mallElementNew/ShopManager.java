package mallElementNew;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import mallParameters.MallConst;
import math.ZGeoMath;
import math.ZMath;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.linemerge.LineMerger;
import org.locationtech.jts.operation.polygonize.Polygonizer;
import transform.ZTransform;
import wblut.geom.WB_GeometryOp;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.util.*;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/16
 * @time 10:54
 */
public class ShopManager {
    private Polygon publicBlock;
    private List<Polygon> shopBlocks;

    private List<Shop> allShops;
    private List<List<LineString>> allSubLines; // 初次剖分线

    private List<ZPoint> turning; // 记录需要拐角的剖分线所属的轴网交点

    /* ------------- constructor ------------- */

    public ShopManager() {

    }

    /* ------------- member function ------------- */

    public void updateShopPartition(
            Polygon boundary,
            List<LineString> publicSpaceCurves,
            Point verify,
            StructureGrid[] grids
    ) {
        this.allShops = new ArrayList<>();
        this.allSubLines = new ArrayList<>();

        updatePolygonizer(boundary, publicSpaceCurves, verify);
        Map<ZPoint, ZLine> intersectGrid = selectIntersection(publicSpaceCurves, grids);
        updateSubdivision(boundary, intersectGrid, grids, verify);
    }

    /**
     * perform first-level shop subdivision
     *
     * @param boundary      boundary of current floor
     * @param intersectGrid intersection points of the grid and buffer curves
     * @param grids         construction grid
     * @param verify        verift point
     * @return void
     */
    private void updateSubdivision(Polygon boundary, Map<ZPoint, ZLine> intersectGrid, StructureGrid[] grids, Point verify) {
        if (this.shopBlocks.size() > 1) {
            // 1st floor
            List<List<LineString>> subLines = new ArrayList<>();
            int shopBlockNum = this.shopBlocks.size();
            for (int i = 0; i < shopBlockNum; i++) {
                subLines.add(new ArrayList<>());
            }

            for (ZPoint p : intersectGrid.keySet()) {
                int index = -1;
                // 根据是否朝向判断方向
                ZLine lineValue = intersectGrid.get(p);
                ZPoint[] ray = new ZPoint[]{p, lineValue.getDirection()};
                Point testP = p.add(lineValue.getDirectionNor().scaleTo(1)).toJtsPoint();
                boolean flag = true;
                if (publicBlock.contains(testP)) {
                    // reverse
                    ray = new ZPoint[]{p, lineValue.getDirection().scaleTo(-1)};
                    testP = p.add(lineValue.getDirectionNor().scaleTo(-1)).toJtsPoint();

                    for (int i = 0; i < shopBlockNum; i++) {
                        if (shopBlocks.get(i).contains(testP)) {
                            index = i;
                            break;
                        }
                    }
                } else {
                    flag = false;
                    for (int i = 0; i < shopBlockNum; i++) {
                        if (shopBlocks.get(i).contains(testP)) {
                            index = i;
                            break;
                        }
                    }
                }

                // 端头是否需要转弯相交
                if (turning.contains(p)) {
                    int a = -1; // 所在轴网标号
                    int[] interPos = {-1, -1};
                    for (int i = 0; i < grids.length; i++) {
                        interPos = grids[i].getLineIndex(lineValue);
                        if (interPos[0] != -1) {
                            a = i;
                            break;
                        }
                    }
                    if (a != -1) {
                        StructureGrid g = grids[a];

                        ZPoint nextNode = null;
                        if (interPos[0] == 0) {
                            // 01上
                            ZPoint start = lineValue.getPt0();

                            double unitLength = g.getUnit12().getLength();
                            double distance = start.distance(p);
                            int num = 0;
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num + 1];
                            }
                        } else {
                            // 12上
                            ZPoint start = lineValue.getPt0();

                            int num = 0;
                            double unitLength = g.getUnit10().getLength();
                            double distance = start.distance(p);
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num][interPos[1]];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num + 1][interPos[1]];
                            }
                        }
                        // 两段线：延长线和最近点连线 合成一条LineString
                        ZPoint[] newRay = new ZPoint[]{nextNode, ray[1]};
                        ZLine extend = ZGeoMath.extendSegmentToPolygon(newRay, ZTransform.PolygonToWB_Polygon(boundary));
                        if (extend != null) {
                            if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                                // 剔除过长剖分线
                                WB_Point closest = WB_GeometryOp.getClosestPoint2D(nextNode.toWB_Point(), ZTransform.PolygonToWB_PolyLine(publicBlock).get(0));
                                Coordinate[] coords = new Coordinate[3];
                                coords[0] = new Coordinate(closest.xd(), closest.yd());
                                coords[1] = new Coordinate(extend.getPt0().xd(), extend.getPt0().yd());
                                coords[2] = new Coordinate(extend.getPt1().xd(), extend.getPt1().yd());

                                LineString subLine = ZFactory.jtsgf.createLineString(coords);
                                subLines.get(index).add(subLine);
                            }
                        }
                    }
                } else {
                    ZLine extend = ZGeoMath.extendSegmentToPolygon(ray, ZTransform.PolygonToWB_Polygon(boundary));
                    if (extend != null) {
                        if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                            // 剔除过长剖分线
                            LineString subLine = extend.toJtsLineString();
                            subLines.get(index).add(subLine);
                        }
                    }
                }
            }

            // polygonizer to get cells
            for (int i = 0; i < shopBlockNum; i++) {
                Polygonizer pr = new Polygonizer();
                Geometry nodedLineStrings = ZTransform.PolygonToLineString(shopBlocks.get(i)).get(0);
                List<LineString> blockSL = subLines.get(i);
                for (int j = 0; j < blockSL.size(); j++) {
                    LineString sl = ZFactory.createExtendedLineString(blockSL.get(j), 0.1);
                    nodedLineStrings = nodedLineStrings.union(sl);
                }
                pr.add(nodedLineStrings);
                Collection<Polygon> allPolys = pr.getPolygons();
                for (Polygon poly : allPolys) {
                    if (poly.getArea() > 1.0) {
                        // 防止 polygonizer 时出现极小误差
                        allShops.add(new Shop(poly));
                    }
                }
            }
            // restore subdivide lines
            this.allSubLines = subLines;
        } else {
            List<LineString> subLines = new ArrayList<>();

            for (ZPoint p : intersectGrid.keySet()) {
                ZLine lineValue = intersectGrid.get(p);
                ZPoint[] ray = new ZPoint[]{p, lineValue.getDirection()};
                Point testP = p.add(lineValue.getDirectionNor().scaleTo(1)).toJtsPoint();
                boolean flag = false;
                if (publicBlock.contains(testP)) {
                    // reverse
                    ray = new ZPoint[]{p, lineValue.getDirection().scaleTo(-1)};
                    flag = true;
                }
                // 端头是否需要转弯相交
                if (turning.contains(p)) {
                    int a = -1; // 所在轴网标号
                    int[] interPos = {-1, -1};
                    for (int i = 0; i < grids.length; i++) {
                        interPos = grids[i].getLineIndex(lineValue);
                        if (interPos[0] != -1) {
                            a = i;
                            break;
                        }
                    }
                    if (a != -1) {
                        StructureGrid g = grids[a];

                        ZPoint nextNode = null;
                        if (interPos[0] == 0) {
                            // 01上
                            ZPoint start = lineValue.getPt0();

                            double unitLength = g.getUnit12().getLength();
                            double distance = start.distance(p);
                            int num = 0;
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[interPos[1]][num + 1];
                            }
                        } else {
                            // 12上
                            ZPoint start = lineValue.getPt0();

                            int num = 0;
                            double unitLength = g.getUnit10().getLength();
                            double distance = start.distance(p);
                            while (unitLength * (num + 1) < distance) {
                                num++;
                            }
                            // 找到邻接节点
                            if (flag) {
                                // 前一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num][interPos[1]];
                            } else {
                                // 下一个轴网节点开始延伸
                                nextNode = g.getGridNodes()[num + 1][interPos[1]];
                            }
                        }
                        // 两段线：延长线和最近点连线 合成一条LineString
                        ZPoint[] newRay = new ZPoint[]{nextNode, ray[1]};
                        ZLine extend = ZGeoMath.extendSegmentToPolygon(newRay, ZTransform.PolygonToWB_Polygon(boundary));
                        if (extend != null) {
                            if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                                // 剔除过长剖分线
//                                ZPoint closestP = ZGeoMath.closestPointToLineList(nextNode, ZFactory.breakWB_PolyLine(ZTransform.PolygonToWB_PolyLine(publicBlock).get(0)));
                                WB_Point closestP = WB_GeometryOp.getClosestPoint2D(nextNode.toWB_Point(), ZTransform.PolygonToWB_PolyLine(publicBlock).get(0));
                                if (closestP != null && !Double.isNaN(closestP.xd())) {
                                    Coordinate[] coords = new Coordinate[3];
                                    coords[0] = new Coordinate(closestP.xd(), closestP.yd());
                                    coords[1] = new Coordinate(extend.getPt0().xd(), extend.getPt0().yd());
                                    coords[2] = new Coordinate(extend.getPt1().xd(), extend.getPt1().yd());

                                    LineString subLine = ZFactory.jtsgf.createLineString(coords);
                                    subLines.add(subLine);
                                }
                            }
                        }
                    }
                } else {
                    ZLine extend = ZGeoMath.extendSegmentToPolygon(ray, ZTransform.PolygonToWB_Polygon(boundary));
                    if (extend != null) {
                        if (extend.getLength() < 4 * MallConst.SHOP_SPAN_THRESHOLD[1]) {
                            // 剔除过长剖分线
                            LineString subLine = extend.toJtsLineString();
                            subLines.add(subLine);
                        }
                    }
                }
            }

            // polygonizer to get cells
            Polygonizer pr = new Polygonizer();
            List<LineString> shopBlockLS = ZTransform.PolygonToLineString(shopBlocks.get(0));
            Geometry nodedLineStrings = shopBlockLS.get(0);
            if (shopBlockLS.size() > 1) {
                for (int i = 1; i < shopBlockLS.size(); i++) {
                    nodedLineStrings = nodedLineStrings.union(shopBlockLS.get(i));
                }
            }
            for (int j = 0; j < subLines.size(); j++) {
                LineString sl = ZFactory.createExtendedLineString(subLines.get(j), 0.1);
                nodedLineStrings = nodedLineStrings.union(sl);
            }
            pr.add(nodedLineStrings);
            Collection<Polygon> allPolys = pr.getPolygons();

            boolean doIf = true;
//            Point verify = graph.getTreeNodes().get(0).toJtsPoint();
            for (Polygon poly : allPolys) {
                if (poly.getArea() > 1.0) {
                    // 防止 polygonizer 时出现极小误差
                    if (doIf) {
                        if (poly.contains(verify)) {
                            doIf = false;
                            continue;
                        }
                    }
                    allShops.add(new Shop(poly));
                }
            }
            // restore subdivide lines
            this.allSubLines.add(subLines);
        }
    }

    /**
     * split boundary by Polygonizer, find public block and shop blocks
     *
     * @param boundary
     * @param publicSpaceCurves traffic curves from frontend
     * @param verify
     * @return void
     */
    private void updatePolygonizer(Polygon boundary, List<LineString> publicSpaceCurves, Point verify) {
        // spilt blocks
        Polygonizer pr = new Polygonizer();
        Geometry nodedLineStrings = ZTransform.PolygonToLineString(boundary).get(0);
        if (publicSpaceCurves.size() > 1) {
            // first floor
            for (LineString ls : publicSpaceCurves) {
                LineString newLs = ZFactory.createExtendedLineString(ls, 0.1);
                nodedLineStrings = nodedLineStrings.union(newLs);
            }
        } else {
            for (LineString ls : publicSpaceCurves) {
                nodedLineStrings = nodedLineStrings.union(ls);
            }
        }
        pr.add(nodedLineStrings);
        Collection<Polygon> allPolys = pr.getPolygons();
        // verify one tree node to find public poly, others are shop blocks
//        this.verify = graph.getTreeNodes().get(0).toJtsPoint();
        for (Polygon p : allPolys) {
            if (p.contains(verify)) {
                this.publicBlock = p;
                allPolys.remove(p);
                break;
            }
        }
        if (publicBlock != null) {
            allPolys.remove(publicBlock);
            this.shopBlocks = new ArrayList<>();
            shopBlocks.addAll(allPolys);
        } else {
            throw new NullPointerException("can't find public space block polygon");
        }
    }

    /**
     * select intersection points of the grid and buffer curves
     *
     * @param publicSpaceCurves buffer curves from front end
     * @param grid              structure grid
     * @return java.util.Map<basicGeometry.ZPoint, basicGeometry.ZLine>
     */
    private Map<ZPoint, ZLine> selectIntersection(List<LineString> publicSpaceCurves, StructureGrid[] grid) {
        Map<ZPoint, ZLine> intersectGrid = new HashMap<>();

        this.turning = new ArrayList<>();
        if (publicSpaceCurves.size() > 1) {
            // 1层 若干条LineString
            // 1层 若干条LineString
            for (LineString ls : publicSpaceCurves) {
                for (StructureGrid g : grid) {
                    for (ZLine l : g.getAllLines()) {
                        ZPoint[] linePD = l.toLinePD();
                        for (int i = 0; i < ls.getCoordinates().length - 2; i++) {
                            ZLine polySeg = new ZLine(
                                    ls.getCoordinateN(i),
                                    ls.getCoordinateN(i + 1)
                            );

                            double cos = l.angleCosWith(polySeg);
                            if (cos <= 0.5) {
                                // 大于60度，才取相交 否则舍弃
                                ZPoint[] polySegPD = polySeg.toLinePD();
                                ZPoint intersect = null;

                                ZPoint delta = polySegPD[0].sub(linePD[0]);
                                double crossBase = linePD[1].cross2D(polySegPD[1]);
                                double crossDelta0 = delta.cross2D(linePD[1]);
                                double crossDelta1 = delta.cross2D(polySegPD[1]);

                                if (Math.abs(crossBase) >= 0.00000001) {
                                    double s = crossDelta1 / crossBase; // seg
                                    double t = crossDelta0 / crossBase; // polySeg
                                    if (s >= 0 && s <= 1 && t >= 0 && t < 1) {
                                        intersect = polySegPD[0].add(polySegPD[1].scaleTo(t));
                                    }
                                }
                                if (intersect != null) {
                                    // 大于75度 直接取相交点 若60~75则另做记录
                                    if (cos > 0.26) {
                                        turning.add(intersect);
                                    }
                                    intersectGrid.put(intersect, l);
                                }
                            }
                        }
                        // final polyline segment: [--]
                        ZLine polySeg = new ZLine(
                                ls.getCoordinateN(ls.getCoordinates().length - 2),
                                ls.getCoordinateN(ls.getCoordinates().length - 1)
                        );
                        double cos = l.angleCosWith(polySeg);
                        if (cos <= 0.5) {
                            // 大于60度，才取相交 否则舍弃
                            ZPoint[] polySegPD = polySeg.toLinePD();
                            ZPoint intersect = null;

                            ZPoint delta = polySegPD[0].sub(linePD[0]);
                            double crossBase = linePD[1].cross2D(polySegPD[1]);
                            double crossDelta0 = delta.cross2D(linePD[1]);
                            double crossDelta1 = delta.cross2D(polySegPD[1]);

                            if (Math.abs(crossBase) >= 0.00000001) {
                                double s = crossDelta1 / crossBase; // seg
                                double t = crossDelta0 / crossBase; // polySeg
                                if (s >= 0 && s <= 1 && t >= 0 && t <= 1) {
                                    intersect = polySegPD[0].add(polySegPD[1].scaleTo(t));
                                }
                            }
                            if (intersect != null) {
                                // 大于75度 直接取相交点 若60~75则另做记录
                                if (cos > 0.26) {
                                    turning.add(intersect);
                                }
                                intersectGrid.put(intersect, l);
                            }
                        }
                    }
                }
            }
        } else {
            // 2层及以上 闭合LineString
            LineString ls = publicSpaceCurves.get(0);
            for (StructureGrid g : grid) {
                for (ZLine l : g.getAllLines()) {
                    ZPoint[] linePD = l.toLinePD();
                    for (int i = 0; i < ls.getCoordinates().length - 1; i++) {
                        ZLine polySeg = new ZLine(
                                ls.getCoordinateN(i),
                                ls.getCoordinateN(i + 1)
                        );
                        double cos = l.angleCosWith(polySeg);
                        if (cos <= 0.5) {
                            // 大于60度，才取相交 否则舍弃
                            ZPoint[] polySegPD = polySeg.toLinePD();
                            ZPoint intersect = null;

                            ZPoint delta = polySegPD[0].sub(linePD[0]);
                            double crossBase = linePD[1].cross2D(polySegPD[1]);
                            double crossDelta0 = delta.cross2D(linePD[1]);
                            double crossDelta1 = delta.cross2D(polySegPD[1]);

                            if (Math.abs(crossBase) >= 0.00000001) {
                                double s = crossDelta1 / crossBase; // seg
                                double t = crossDelta0 / crossBase; // polySeg
                                if (s >= 0 && s <= 1 && t >= 0 && t < 1) {
                                    intersect = polySegPD[0].add(polySegPD[1].scaleTo(t));
                                }
                            }
                            if (intersect != null) {
                                // 大于75度 直接取相交点 若60~75则另做记录
                                if (cos > 0.26) {
                                    turning.add(intersect);
                                }
                                intersectGrid.put(intersect, l);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("valid intersection points: " + intersectGrid.size());
        return intersectGrid;
    }


    /**
     * secondary-split for internal corners and external corners
     *
     * @param splitShopID      id of the shop to split
     * @param publicSpaceShape shape polygon of public space
     * @param grid             structure grids
     * @return void
     */
    public void updateSplit(List<Integer> splitShopID, Polygon publicSpaceShape, StructureGrid[] grid) {
        for (int id : splitShopID) {
            // get shop center
            Shop shop = allShops.get(id);
            Polygon shape = shop.getShape();
            String type = shop.getShopType();
            ZPoint shopCenter = new ZPoint(shop.getCenter());

            if (type.equals("ZL") || type.equals("CZL")) {
                // find contained grid nodes and 2 possible split vector
                List<ZPoint> contained = new ArrayList<>();
                ZPoint[] possibleVec = new ZPoint[2];

                for (StructureGrid g : grid) {
                    List<ZPoint> con = new ArrayList<>();
                    for (ZPoint node : g.getGridNodesFlat()) {
                        if (shape.contains(node.toJtsPoint())) {
                            con.add(node);
                        }
                    }
                    if (con.size() > 0 && contained.size() <= con.size()) {
                        contained = con;
                        possibleVec[0] = g.getUnit10().normalize();
                        possibleVec[1] = g.getUnit12().normalize();
                    }
                }

                // find the closest node to shop center
                double[] dist = new double[contained.size()];
                for (int i = 0; i < dist.length; i++) {
                    dist[i] = shopCenter.distance(contained.get(i));
                }
                int min = ZMath.getMinIndex(dist);
                ZPoint closest = contained.get(min);

                // find the overlapped linestring with public space
                Geometry intersect = shape.intersection(publicSpaceShape);
                intersect.union();
                System.out.println("intersect: " + intersect);

                // merge MultiLineString to LineString
                LineMerger lineMerger = new LineMerger();
                lineMerger.add(intersect);
                Collection merge = lineMerger.getMergedLineStrings();
                Object[] mergeResult = merge.toArray();

                System.out.println("mergeResult.length: " + mergeResult.length);

                if (mergeResult.length > 0) {
                    // get the longest
                    LineString overlap = (LineString) mergeResult[0];
                    if (mergeResult.length > 1) {
                        for (int i = 1; i < mergeResult.length; i++) {
                            LineString ls = (LineString) mergeResult[i];
                            if (overlap.getLength() < ls.getLength()) {
                                overlap = ls;
                            }
                        }
                    }
                    System.out.println("overlap: " + overlap);

                    double length = overlap.getLength();
                    ZPoint mid = ZGeoMath.pointFromStart(overlap, length * 0.5);

                    // get two new split line
                    // closest to mid-point
                    Coordinate[] splitLineCoords = new Coordinate[3];
                    splitLineCoords[0] = mid.toJtsCoordinate();
                    splitLineCoords[1] = closest.toJtsCoordinate();

                    // random a vector direction
                    ZPoint rayDir = Math.random() > 0.5 ? possibleVec[1] : possibleVec[0];

                    // should be obtuse angle between ray and closest-mid
                    if (rayDir.dot2D(mid.sub(closest)) > 0) {
                        rayDir.scaleSelf(-1);
                    }
                    ZPoint[] ray = new ZPoint[]{
                            closest,
                            rayDir
                    };

                    // extend to original shop boundary
                    WB_Polygon shapeWB = shop.getShapeWB();
                    ZLine extend = ZGeoMath.extendSegmentToPolygon(ray, shapeWB);
                    splitLineCoords[2] = extend.getPt1().toJtsCoordinate();
                    LineString sl = ZFactory.jtsgf.createLineString(splitLineCoords);
                    LineString newSplitLines = ZFactory.createExtendedLineString(sl, 0.1);

                    // perform polygonizer
                    Polygonizer pr = new Polygonizer();
                    Geometry nodedLineStrings = newSplitLines;
                    for (LineString ls : ZTransform.PolygonToLineString(shape)) {
                        nodedLineStrings = nodedLineStrings.union(ls);
                    }
                    pr.add(nodedLineStrings);
                    Collection<Polygon> allPolys = pr.getPolygons();

                    // remove original shop and create new shops
                    System.out.println("allpolys: " + allPolys.size());
                    if (allPolys.size() == 2) {
                        allShops.remove(shop);
                        for (Polygon p : allPolys) {
                            allShops.add(new Shop(p));
                        }
                    }
                }
            }

        }
    }


    /* ------------- setter & getter ------------- */

    public void setAllShops(List<Shop> allShops) {
        this.allShops = allShops;
    }

    public List<Shop> getAllShops() {
        return allShops;
    }

    public List<Polygon> getShopBlocks() {
        return shopBlocks;
    }

    /* ------------- draw ------------- */
}