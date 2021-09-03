package mallElementNew;

import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import math.ZGeoMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

/**
 * site and base boundary
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/9/1
 * @time 15:11
 */
public class SiteBase_L {
    private Polygon site;                            // 场地轮廓
    private Polygon redLine;                         // 场地红线
    private Polygon boundary;                        // 建筑轮廓

    private int base_L = 0;
    private double boundaryArea = 0;

    /* ------------- constructor ------------- */

    public SiteBase_L(Polygon _site, Polygon _boundary, int base, double redLineDist, double siteBufferDist) {
        this.site = _site;
        this.boundary = _boundary;
        this.base_L = base;
        if (boundary != null) {
            // given any site and boundary (red line will be null)
        } else {
            // given quad site, generate L-shape boundary
            Geometry redLineSite = site.buffer(-1 * redLineDist);
            this.redLine = (Polygon) redLineSite.reverse();
            this.boundary = generateBoundary_L(redLine, base_L, siteBufferDist);
        }
        this.boundaryArea = boundary.getArea();
    }

    /* ------------- member function ------------- */

    public void update_L(int base, double redLineDist, double siteBufferDist) {
        this.base_L = base;

        // given quad site, generate L-shape boundary
        Geometry redLineSite = site.buffer(-1 * redLineDist);
        this.redLine = (Polygon) redLineSite.reverse();
        this.boundary = generateBoundary_L(redLine, base_L, siteBufferDist);

        this.boundaryArea = boundary.getArea();
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
        boundaryPts[2] = ZGeoMath.simpleLineElementsIntersect2D(
                seg01_move, "segment", seg12, "segment"
        ).toJtsCoordinate();
        boundaryPts[4] = ZGeoMath.simpleLineElementsIntersect2D(
                seg30_move, "segment", seg23, "segment"
        ).toJtsCoordinate();
        return ZFactory.jtsgf.createPolygon(boundaryPts);
    }

    /* ------------- setter & getter ------------- */

    public Polygon getSite() {
        return site;
    }

    public Polygon getRedLine() {
        return redLine;
    }

    public Polygon getBoundary() {
        return boundary;
    }

    public void setBoundary(Polygon boundary) {
        this.boundary = boundary;
    }

    public double getBoundaryArea() {
        return boundaryArea;
    }

    /* ------------- draw ------------- */
}
