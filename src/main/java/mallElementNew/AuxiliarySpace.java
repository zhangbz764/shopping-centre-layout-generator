package mallElementNew;

import basicGeometry.*;
import mallParameters.MallConst;
import math.ZGeoMath;
import math.ZGraphMath;
import math.ZMath;
import math.ZPermuCombi;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.util.AffineTransformation;
import transform.ZTransform;
import wblut.geom.*;
import wblut.hemesh.HEC_FromPolygons;
import wblut.hemesh.HE_Face;
import wblut.hemesh.HE_Halfedge;
import wblut.hemesh.HE_Mesh;

import java.util.*;

/**
 * generator of auxiliary space: evacuation stairways and washrooms
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/4/4
 * @time 10:11
 */
public class AuxiliarySpace {
    // generator evacuation stairways
    private ZGraph trafficGraph;    // graph of public traffic (for evacuation distance calculation)
    private List<EvacGenerator> allGenerator;   // all possible evacuation generation pts (from shop divide lines)
    private List<EvacGenerator> selGenerator;   // selected evacuation generators

    private List<ZLine> coveredPath;    // covered evacuation paths

    private List<EvacShape> evacShapes;     // shapes of stairway and corridor

    private List<Polygon> washroomShapes;   // shapes of washrooms

    /* ------------- constructor ------------- */

    public AuxiliarySpace() {

    }

    /* ------------- public member function: evacuation stairway ------------- */

    /**
     * initialize evacuation generators
     *
     * @param mainTrafficInnerLine inner traffic line
     * @param shopPolys            shop polygons
     * @param floorArea            area of current floor
     * @param floorBoundary        boundary of current floor
     * @return void
     */
    public void initEvacuationGenerator(
            WB_PolyLine mainTrafficInnerLine,
            List<WB_Polygon> shopPolys,
            double floorArea,
            WB_Polygon floorBoundary
    ) {
        // find all divide lines from existing shop polygons
        List<WB_PolyLine> originalDivLines = findDivLines(mainTrafficInnerLine, shopPolys);

        // create graph for the main traffic and closest points from divide lines
        createTopoGraph(mainTrafficInnerLine, originalDivLines, floorBoundary);

        // pre-select divide lines by split boundary
        this.selGenerator = evacPosPreset(floorArea, floorBoundary, allGenerator);

        // get covered graph line
        this.coveredPath = calCoveredGraph(selGenerator);
    }

    /**
     * update evacuation generators by giving new IDs
     *
     * @param newEvacGenIDs new generator IDs
     * @return void
     */
    public void updateEvacuationGenerator(List<Integer> newEvacGenIDs) {
        this.selGenerator = new ArrayList<>();
        for (Integer i : newEvacGenIDs) {
            selGenerator.add(allGenerator.get(i));
        }

        // sort by distForPos to get the order of selected generators
        selGenerator.sort(new EvacGenComparator());

        // get covered graph line
        this.coveredPath = calCoveredGraph(selGenerator);
    }

    /**
     * generate the shape of stairway and corridor
     *
     * @param floorBoundary boundary of the floor
     * @param publicSpace   public space shape
     * @return void
     */
    public void generateStairwayShape(Polygon floorBoundary, Polygon publicSpace) {
        this.evacShapes = new ArrayList<>();
        for (EvacGenerator evacGenerator : selGenerator) {
            EvacShape es = new EvacShape(evacGenerator);
            es.generateStairwayShape(floorBoundary, publicSpace);
            evacShapes.add(es);
        }
    }

    /**
     * switch the direction of stairway shape
     *
     * @param id            selected ID of the stairway
     * @param floorBoundary boundary of current storey
     * @param publicSpace   public space shape
     * @return void
     */
    public void switchEvacDir(int id, Polygon floorBoundary, Polygon publicSpace) {
        EvacShape selES = evacShapes.get(id);
        selES.dirLeft = !selES.dirLeft;
        selES.generateStairwayShape(floorBoundary, publicSpace);
    }

    /* ------------- public member function: evacuation stairway ------------- */

    /**
     * generate the shape of washroom
     *
     * @param floorBoundary boundary of current storey
     * @param trafficLS     main traffic LineString
     * @return void
     */
    public void initWashroom(Polygon floorBoundary, LineString trafficLS) {
        this.washroomShapes = new ArrayList<>();
        List<EvacGenerator> maxCoveredGen = enumWashroomPos(MallConst.WASHROOM_NUM, floorBoundary, trafficLS);
        for (EvacGenerator g : maxCoveredGen) {
            washroomShapes.add(generateWashroomShape(g, floorBoundary));
        }
    }

    /* ------------- private member function: evacuation stairway ------------- */

    /**
     * find divide line from current shops
     *
     * @param mainTrafficInnerLine inner traffic route
     * @param shopPolys            all polygons of current shops
     * @return java.util.List<wblut.geom.WB_PolyLine>
     */
    private List<WB_PolyLine> findDivLines(WB_PolyLine mainTrafficInnerLine, List<WB_Polygon> shopPolys) {
        List<WB_PolyLine> result = new ArrayList<>();

        // create mesh
        HE_Mesh shopMesh = new HEC_FromPolygons(shopPolys).create();

        // get common edges between each pair of shop polygon
        List<List<HE_Halfedge>> coEdges = new ArrayList<>();
        List<Integer> visited = new ArrayList<>();
        for (int i = 0; i < shopMesh.getFaces().size(); i++) {
            HE_Face sel = shopMesh.getFaceWithIndex(i);
            List<HE_Face> nei = sel.getNeighborFaces();

            for (HE_Face f : nei) {
                if (!visited.contains(f.getInternalLabel())) {
                    HE_Halfedge e0 = sel.getHalfedge(f);
                    HE_Halfedge curr = e0.getNextInFace();

                    List<HE_Halfedge> co = new ArrayList<>();
                    co.add(e0);

                    while (curr != e0) {
                        if (f.getFaceEdges().contains(curr.getPair()) || f.getFaceEdges().contains(curr)) {
                            co.add(curr);
                        }
                        curr = curr.getNextInFace();
                    }
                    coEdges.add(co);
                }
            }
            visited.add(i);
        }

        // create polyline and reverse order
        for (List<HE_Halfedge> edgeList : coEdges) {
            List<ZLine> list = new ArrayList<>();
            for (HE_Halfedge he : edgeList) {
                list.add(new ZLine(he.getStartPosition(), he.getEndPosition()));
            }
            WB_PolyLine pl = ZFactory.createWB_PolyLineFromSegs(list);

            // test distance to judge reverse
            assert pl != null;
            WB_Point ps = pl.getPoint(0);
            WB_Point pe = pl.getPoint(pl.getNumberOfPoints() - 1);

            double dists = WB_GeometryOp.getDistance2D(ps, mainTrafficInnerLine);
            double diste = WB_GeometryOp.getDistance2D(pe, mainTrafficInnerLine);
            if (dists < diste) {
                result.add(ZGeoMath.reversePolyLine(pl));
            } else {
                result.add(pl);
            }

        }

        return result;
    }

    /**
     * create topology graph for main traffic
     * with divide lines' nodes
     *
     * @param mainTrafficInnerLine inner traffic route
     * @return void
     */
    private void createTopoGraph(WB_PolyLine mainTrafficInnerLine, List<WB_PolyLine> divLines, WB_PolyLine boundary) {
        // inner class: NodeTemp
        class NodeTemp {
            private WB_Point pt;
            private double distToStart;
            private WB_PolyLine div;

            private NodeTemp(WB_Point pt, double d, WB_PolyLine divLine) {
                this.pt = pt;
                this.distToStart = d;
                this.div = divLine;
            }
        }

        // inner class: NodeTempComparator
        class NodeTempComparator implements Comparator<NodeTemp> {
            @Override
            public int compare(NodeTemp n1, NodeTemp n2) {
                return Double.compare(n1.distToStart, n2.distToStart);
            }
        }

        // create graph for traffic
        NodeTemp[] trafficCoords = new NodeTemp[mainTrafficInnerLine.getNumberOfPoints()];
        for (int i = 0; i < mainTrafficInnerLine.getNumberOfPoints(); i++) {
            WB_Point coord = mainTrafficInnerLine.getPoint(i);
            double distToStart = 0;
            for (int j = 0; j < i; j++) {
                distToStart += mainTrafficInnerLine.getSegment(j).getLength();
            }

            trafficCoords[i] = new NodeTemp(coord, distToStart, null);
        }
        NodeTemp[] closestCoords = new NodeTemp[divLines.size()];
        for (int i = 0; i < divLines.size(); i++) {
            WB_Point closest = WB_GeometryOp.getClosestPoint2D(
                    divLines.get(i).getPoint(divLines.get(i).getNumberOfPoints() - 1),
                    mainTrafficInnerLine
            );
            double distToStart = ZGeoMath.distFromStart(mainTrafficInnerLine, closest);
            closestCoords[i] = new NodeTemp(closest, distToStart, divLines.get(i));
        }

        // sort and get nodes
        // create Side objects
        List<NodeTemp> pointFiltered = new ArrayList<>();
        pointFiltered.addAll(Arrays.asList(trafficCoords));
        pointFiltered.addAll(Arrays.asList(closestCoords));
        pointFiltered.sort(new NodeTempComparator());

        this.allGenerator = new ArrayList<>();
        List<ZNode> graphNodes = new ArrayList<>();
        double currD = -1;
        for (NodeTemp temp : pointFiltered) {
            if (temp.div != null) { // if NodeTemp has side, create one object
                EvacGenerator generator = new EvacGenerator(temp.div);
                // exclude coincident points
                double dist = temp.distToStart;
                if (dist - currD > ZGeoMath.epsilon) {
                    ZNode n = new ZNode(temp.pt);
                    graphNodes.add(n);

                    // if NodeTemp has side, record ZNode
                    generator.setClosestNode(n);
                    currD = dist;
                } else {
                    // if NodeTemp has side, record the last one in the list
                    generator.setClosestNode(graphNodes.get(graphNodes.size() - 1));
                }
                // set the distance from boundary LineString start
                generator.distForPos = ZGeoMath.distFromStart((WB_PolyLine) boundary, generator.start.toWB_Point());
                allGenerator.add(generator);
            } else { // if doesn't, just create ZNode
                // exclude coincident points
                double dist = temp.distToStart;
                if (dist - currD > ZGeoMath.epsilon) {
                    ZNode n = new ZNode(temp.pt);
                    graphNodes.add(n);
                    currD = dist;
                }
            }
        }
        int[][] matrix = new int[graphNodes.size() - 1][];
        for (int i = 0; i < graphNodes.size() - 1; i++) {
            matrix[i] = new int[]{i, i + 1};
        }
        this.trafficGraph = new ZGraph(graphNodes, matrix);
    }

    /**
     * pre-select divide lines by split boundary
     * given evacuation width
     *
     * @param floorArea area of current floor
     * @param boundary  boundary of current floor
     * @param allGen    all possible generators
     * @return java.util.List<wblut.geom.WB_PolyLine>
     */
    private List<EvacGenerator> evacPosPreset(double floorArea, WB_Polygon boundary, List<EvacGenerator> allGen) {
        List<EvacGenerator> preSelGens = new ArrayList<>();

        double evacWidth = (floorArea * MallConst.POPULATION_RATE * MallConst.EVAC_WIDTH_HUNDRED) / 100;
        System.out.println("evacuation width : " + evacWidth);
        int stairwayNum = (int) Math.ceil((evacWidth / MallConst.EVACUATION_WIDTH) * 0.5);
        System.out.println("stairway group number : " + stairwayNum);

        List<ZPoint> dividePts = ZGeoMath.splitPolyLineEdge(boundary, stairwayNum);
        for (ZPoint dp : dividePts) {
            double[] dist = new double[allGen.size()];
            for (int i = 0; i < allGen.size(); i++) {
                ZPoint start = allGen.get(i).start;
                dist[i] = dp.distanceSq(start);
            }
            int min = ZMath.getMinIndex(dist);
            EvacGenerator g = allGen.get(min);
            preSelGens.add(g);
        }

        // sort by distForPos to get the order of selected generators
        preSelGens.sort(new EvacGenComparator());
        return preSelGens;
    }

    /**
     * record covered path
     *
     * @param selGen selected generator
     * @return java.util.List<basicGeometry.ZLine>
     */
    private List<ZLine> calCoveredGraph(List<EvacGenerator> selGen) {
        List<ZLine> covered = new ArrayList<>();

        for (EvacGenerator s : selGen) {
            // how long remain
            double evacDistRemain = MallConst.EVACUATION_DIST - s.sideLineL - s.distToClosest;
            if (evacDistRemain > 0) {
                ZNode closest = s.closestNode;
                // passed edge
                covered.addAll(ZGraphMath.segmentsOnGraphByDist(closest, null, evacDistRemain));
            }
        }

        return covered;
    }

    /* ------------- private member function: washroom ------------- */

    private List<EvacGenerator> enumWashroomPos(int washroomNum, Polygon boundary, LineString trafficLS) {
        // check each generator if a washroom shape can fit in
        List<EvacGenerator> possibleForWashroom = new ArrayList<>();
        for (int i = 0; i < selGenerator.size(); i++) {
            EvacGenerator g = selGenerator.get(i);
            if (!g.shape.dirLeft) {
                // stairway shape direction: along boundary direction
                // washroom shape direction: against boundary direction
                // need to check the previous generator
                EvacGenerator g_prev = selGenerator.get((i + selGenerator.size() - 1) % selGenerator.size());
                if ((g_prev.shape.dirLeft && ZGeoMath.distAlongEdge(g_prev.start, g.start, boundary) > 0.75 * MallConst.WASHROOM_LENGTH)
                        ||
                        (!g_prev.shape.dirLeft && ZGeoMath.distAlongEdge(g_prev.start, g.start, boundary) > (MallConst.WASHROOM_LENGTH + 0.75 * MallConst.STAIRWAY_LENGTH))
                ) {
                    possibleForWashroom.add(g);
                }
            } else {
                // stairway shape direction: against boundary direction
                // washroom shape direction: along boundary direction
                // need to check the next generator
                EvacGenerator g_next = selGenerator.get((i + 1) % selGenerator.size());
                if ((!g_next.shape.dirLeft && ZGeoMath.distAlongEdge(g.start, g_next.start, boundary) > 0.75 * MallConst.WASHROOM_LENGTH)
                        ||
                        (g_next.shape.dirLeft && ZGeoMath.distAlongEdge(g.start, g_next.start, boundary) > (MallConst.WASHROOM_LENGTH + 0.75 * MallConst.STAIRWAY_LENGTH))
                ) {
                    possibleForWashroom.add(g);
                }
            }
        }

        // enumerate the position of possible generators
        int[] selGenIndex = ZMath.createIntegerSeries(0, possibleForWashroom.size());
        ZPermuCombi zpc = new ZPermuCombi();
        zpc.combination(selGenIndex, washroomNum, 0, 0);
        List<List<Integer>> combiResult = zpc.getCombinationResults();

        // find the most-traffic-covered washroom group
        double[] coverRatios = new double[combiResult.size()];
        for (int i = 0; i < combiResult.size(); i++) {
            List<Integer> list = combiResult.get(i);
            Geometry unionCircle = ZFactory.jtsgf.createPolygon();
            for (Integer index : list) {
                EvacGenerator g = possibleForWashroom.get(index);
                ZPoint[] cirPts = ZFactory.createCircle(g.start, MallConst.WASHROOM_SERV_R, 32);
                Coordinate[] cirCoords = new Coordinate[cirPts.length];
                for (int j = 0; j < cirPts.length; j++) {
                    cirCoords[j] = cirPts[j].toJtsCoordinate();
                }
                Polygon cir = ZFactory.jtsgf.createPolygon(cirCoords);
                unionCircle = unionCircle.union(cir);
            }
            Geometry intersection = unionCircle.intersection(trafficLS);
            coverRatios[i] = intersection.getLength();
        }
        int max = ZMath.getMaxIndex(coverRatios);
        List<EvacGenerator> washroomGenerator = new ArrayList<>();
        for (Integer index : combiResult.get(max)) {
            washroomGenerator.add(possibleForWashroom.get(index));
        }
        return washroomGenerator;
    }

    private Polygon generateWashroomShape(EvacGenerator g, Polygon floorBoundary) {
        WB_PolyLine sl = g.sideLine;
        WB_Segment firstSeg = sl.getSegment(0);
        ZPoint firstSegDir = new ZPoint(firstSeg.getDirection()).normalize();

        ZPoint generateDir;
        if (!g.shape.dirLeft) {
            // stairway shape direction: along boundary direction
            // washroom shape direction: against boundary direction
            generateDir = firstSegDir.rotate2D(Math.PI * 0.5);
        } else {
            // stairway shape direction: against boundary direction
            // washroom shape direction: along boundary direction
            generateDir = firstSegDir.rotate2D(Math.PI * -0.5);
        }

        return generateRectShape(g, floorBoundary, firstSegDir, generateDir);
    }

    /**
     * generate rectangle module shape (stairway or washroom)
     *
     * @param g             given generator
     * @param floorBoundary boundary of current storey
     * @param firstSegDir   direction vector of divide line
     * @param generateDir   direction vector of generation
     * @return org.locationtech.jts.geom.Polygon
     */
    private Polygon generateRectShape(EvacGenerator g, Polygon floorBoundary, ZPoint firstSegDir, ZPoint generateDir) {
        ZPoint genStart = g.start;

        // cut boundary
        LineString boundaryLS = ZTransform.PolygonToLineString(floorBoundary).get(0);
        Coordinate[] cutPolyCoords = new Coordinate[5];
        ZPoint p0 = genStart.add(firstSegDir.scaleTo(20));
        ZPoint p1 = genStart.add(firstSegDir.scaleTo(-20));
        cutPolyCoords[0] = p0.toJtsCoordinate();
        cutPolyCoords[1] = p1.toJtsCoordinate();
        cutPolyCoords[2] = p1.add(generateDir.scaleTo(MallConst.STAIRWAY_LENGTH)).toJtsCoordinate();
        cutPolyCoords[3] = p0.add(generateDir.scaleTo(MallConst.STAIRWAY_LENGTH)).toJtsCoordinate();
        cutPolyCoords[4] = cutPolyCoords[0];
        Polygon cutPoly = ZFactory.jtsgf.createPolygon(cutPolyCoords);
        Geometry cut = boundaryLS.intersection(cutPoly);
        LineString cutLS = null;
        if (Objects.equals(cut.getGeometryType(), "LineString")) {
            cutLS = (LineString) cut;
        } else if (cut.getGeometryType().equals("MultiLineString")) {
            cutLS = (LineString) cut.getGeometryN(0);
        }

        if (cutLS != null) {
            // get the farthest coord under new xy
            WB_Transform2D transform2D = new WB_Transform2D();
            ZPoint xAxis = new ZPoint(1, 0);
            double theta = xAxis.angleWith(firstSegDir);
            transform2D.addRotateAboutOrigin((theta / 180) * Math.PI);

            Coordinate maxDistCoord = cutLS.getCoordinateN(0);
            double maxDist = Double.MAX_VALUE * -1;
            for (int j = 0; j < cutLS.getNumPoints(); j++) {
                Coordinate c = cutLS.getCoordinateN(j);
                WB_Point p = transform2D.applyAsPoint2D(c.getX(), c.getY());
                if (p.xd() > maxDist) {
                    maxDistCoord = c;
                    maxDist = p.xd();
                }
            }

            // reverse cutLS ?
            if (
                    cutLS.getCoordinateN(0).distance(genStart.toJtsCoordinate())
                            >
                            cutLS.getCoordinateN(cutLS.getNumPoints() - 1).distance(genStart.toJtsCoordinate())
            ) {
                cutLS = ZGeoMath.reverseLineString(cutLS);
            }

            // get 2 offset intersect coords
            ZPoint offset = new ZPoint(maxDistCoord).add(firstSegDir.scaleTo(MallConst.STAIRWAY_WIDTH));
            ZPoint[] offsetLinePD = new ZPoint[]{offset, generateDir};
            ZPoint[] genBaseLinePD1 = new ZPoint[]{genStart, firstSegDir};
            ZPoint[] genBaseLinePD2 = new ZPoint[]{genStart.add(generateDir.scaleTo(MallConst.STAIRWAY_LENGTH)), firstSegDir};
            ZPoint intersect1 = ZGeoMath.lineIntersection2D(offsetLinePD, genBaseLinePD1);
            ZPoint intersect2 = ZGeoMath.lineIntersection2D(offsetLinePD, genBaseLinePD2);

            // build stairway shape
            Coordinate[] shapeCoords = new Coordinate[cutLS.getNumPoints() + 3];
            for (int j = 0; j < cutLS.getNumPoints(); j++) {
                shapeCoords[j] = cutLS.getCoordinateN(j);
            }
            assert intersect2 != null;
            shapeCoords[shapeCoords.length - 3] = intersect2.toJtsCoordinate();
            assert intersect1 != null;
            shapeCoords[shapeCoords.length - 2] = intersect1.toJtsCoordinate();
            shapeCoords[shapeCoords.length - 1] = shapeCoords[0];
            return ZFactory.jtsgf.createPolygon(shapeCoords);
        } else {
            return ZFactory.jtsgf.createPolygon();
        }
    }

    /* ------------- inner class ------------- */

    /**
     * class of shop divide lines as evacuation generator
     */
    private static class EvacGenerator {
        private WB_PolyLine sideLine;
        private ZPoint start;
        private double sideLineL;

        private ZNode closestNode;
        private double distToClosest;

        private EvacShape shape = null;
        private double distForPos = 0;

        private EvacGenerator(WB_PolyLine sideLine) {
            this.sideLine = sideLine;
            this.start = new ZPoint(sideLine.getPoint(0));
            this.sideLineL = 0;
            for (int i = 0; i < sideLine.getNumberSegments(); i++) {
                sideLineL += sideLine.getSegment(i).getLength();
            }
        }

        private void setClosestNode(ZNode closestNode) {
            this.closestNode = closestNode;
            this.distToClosest = sideLine.getPoint(sideLine.getNumberOfPoints() - 1).getDistance2D(closestNode.toWB_Point());
        }
    }

    /**
     * EvacGenComparator
     */
    private static class EvacGenComparator implements Comparator<EvacGenerator> {
        @Override
        public int compare(EvacGenerator eg1, EvacGenerator eg2) {
            return Double.compare(eg1.distForPos, eg2.distForPos);
        }
    }

    /**
     * class of stairway and corridor shape
     */
    private class EvacShape {
        private EvacGenerator g;
        private Polygon stairwayShape = ZFactory.jtsgf.createPolygon();
        private boolean dirLeft;

        private EvacShape(EvacGenerator generator) {
            double random = Math.random();
            dirLeft = random > 0.5;
            this.g = generator;
            g.shape = this;
        }

        private void generateStairwayShape(Polygon floorBoundary, Polygon shopBlock) {
            // get the generating direction from divide line
            WB_PolyLine sl = g.sideLine;
            WB_Segment firstSeg = sl.getSegment(0);
            ZPoint firstSegDir = new ZPoint(firstSeg.getDirection()).normalize();

            double random = Math.random();
            ZPoint generateDir;
            if (dirLeft) {
                generateDir = firstSegDir.rotate2D(Math.PI * 0.5);
            } else {
                generateDir = firstSegDir.rotate2D(Math.PI * -0.5);
            }

            this.stairwayShape = generateRectShape(g, floorBoundary, firstSegDir, generateDir);

            // TODO: 2022/6/16 16:49 by zhangbz
            // corridor width
            // corridor
            LineString divLS = ZTransform.WB_PolyLineToLineString(g.sideLine);
            LineString extDivLS = ZFactory.createExtendedLineString(divLS, 3);
            ZPoint translateVec = generateDir.scaleTo(2.4);
            AffineTransformation atf = new AffineTransformation().translate(translateVec.xd(), translateVec.yd());
            LineString transExtDivLS = (LineString) atf.transform(extDivLS);

            List<Coordinate> corridorCoordList = new ArrayList<>();
            for (int i = 0; i < extDivLS.getNumPoints(); i++) {
                corridorCoordList.add(extDivLS.getCoordinateN(i));
            }
            for (int i = 0; i < transExtDivLS.getNumPoints(); i++) {
                corridorCoordList.add(transExtDivLS.getCoordinateN(transExtDivLS.getNumPoints() - i - 1));
            }
            corridorCoordList.add(corridorCoordList.get(0));

            Polygon corridorRaw = ZFactory.createPolygonFromList(corridorCoordList);
            Polygon intersectCorridor = (Polygon) corridorRaw.intersection(shopBlock);
            stairwayShape = (Polygon) stairwayShape.union(intersectCorridor);
        }
    }

    /* ------------- setter & getter ------------- */

    public List<ZPoint> getAllGeneratorPos() {
        List<ZPoint> list = new ArrayList<>();
        for (EvacGenerator e : allGenerator) {
            list.add(e.start);
        }
        return list;
    }

    public List<ZPoint> getSelGeneratorPos() {
        List<ZPoint> list = new ArrayList<>();
        for (EvacGenerator e : selGenerator) {
            list.add(new ZPoint(e.start.xd(), e.start.yd()));
        }
        return list;
    }

    public List<Integer> getSelGeneratorIDs() {
        List<Integer> ids = new ArrayList<>();
        for (EvacGenerator e : selGenerator) {
            ids.add(allGenerator.indexOf(e));
        }
        return ids;
    }

    public List<ZLine> getCoveredPath() {
        return coveredPath;
    }

    public List<EvacShape> getEvacShapes() {
        return evacShapes;
    }

    public List<Polygon> getStairwayShapePoly() {
        List<Polygon> shapes = new ArrayList<>();
        for (EvacShape es : evacShapes) {
            shapes.add(es.stairwayShape);
        }
        return shapes;
    }
}
