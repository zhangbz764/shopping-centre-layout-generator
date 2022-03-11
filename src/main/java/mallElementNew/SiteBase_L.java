package mallElementNew;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import math.ZGeoMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

/**
 * define an L-shape site & boundary by generating
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/9/1
 * @time 15:11
 */
public class SiteBase_L extends SiteBase {
    private Polygon redLine;                         // 场地红线
    private int base_L = 0;

    /* ------------- constructor ------------- */

    public SiteBase_L(Polygon _site, int base, double redLineDist, double siteBufferDist) {
        super(_site);
        updateByParams(base, redLineDist, siteBufferDist);
    }

    /* ------------- member function ------------- */

    /**
     * update L-shape boundary by base index, red line distance and site buffer distance
     *
     * @param base           base index
     * @param redLineDist    red line distance
     * @param siteBufferDist site buffer distance
     * @return void
     */
    @Override
    public void updateByParams(int base, double redLineDist, double siteBufferDist) {
        this.base_L = base;

        // given quad site, generate L-shape boundary
        Geometry redLineSite = super.getSite().buffer(-1 * redLineDist);
        this.setRedLine((Polygon) redLineSite.reverse());
        Polygon boundary = generateBoundary_L(getRedLine(), base_L, siteBufferDist);
        super.setBoundary(boundary);
    }

    /**
     * generate an L-shape building boundary from a quad site
     *
     * @param validRedLine quad site red line
     * @param base         base index of point of validRedLine
     * @return wblut.geom.WB_Polygon
     */
    private Polygon generateBoundary_L(Polygon validRedLine, int base, double siteBufferDist) {
        assert validRedLine.getNumPoints() == 5;
        Coordinate[] boundaryPts = new Coordinate[7];
        boundaryPts[0] = validRedLine.getCoordinates()[base];
        boundaryPts[1] = validRedLine.getCoordinates()[(base + 1) % 4];
        boundaryPts[5] = validRedLine.getCoordinates()[(base + 3) % 4];
        boundaryPts[6] = boundaryPts[0];
        ZPoint vec01 = new ZPoint(
                boundaryPts[1].getX() - boundaryPts[0].getX(),
                boundaryPts[1].getY() - boundaryPts[0].getY()
        ).normalize();
        ZPoint vec05 = new ZPoint(
                boundaryPts[5].getX() - boundaryPts[0].getX(),
                boundaryPts[5].getY() - boundaryPts[0].getY()
        ).normalize();
        ZPoint bisector = vec01.add(vec05);
        double sin = Math.abs(vec01.cross2D(bisector));
        ZPoint move = bisector.scaleTo(siteBufferDist / sin);

        ZLine seg01_move = new ZLine(boundaryPts[0], boundaryPts[1]).translate2D(move);
        ZLine seg30_move = new ZLine(boundaryPts[5], boundaryPts[6]).translate2D(move);
        ZLine seg12 = new ZLine(validRedLine.getCoordinates()[(base + 1) % 4], validRedLine.getCoordinates()[(base + 2) % 4]);
        ZLine seg23 = new ZLine(validRedLine.getCoordinates()[(base + 2) % 4], validRedLine.getCoordinates()[(base + 3) % 4]);
        boundaryPts[3] = seg01_move.getPt0().toJtsCoordinate();
        boundaryPts[2] = ZGeoMath.simpleLineElementsIntersection2D(
                seg01_move, "segment", seg12, "segment"
        ).toJtsCoordinate();
        boundaryPts[4] = ZGeoMath.simpleLineElementsIntersection2D(
                seg30_move, "segment", seg23, "segment"
        ).toJtsCoordinate();
        return ZFactory.jtsgf.createPolygon(boundaryPts);
    }

    /* ------------- setter & getter ------------- */

    public int getBase_L() {
        return base_L;
    }

    public Polygon getRedLine() {
        return redLine;
    }

    public void setRedLine(Polygon redLine) {
        this.redLine = redLine;
    }


    /* ------------- draw ------------- */
}
