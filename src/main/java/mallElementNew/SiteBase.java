package mallElementNew;

import basicGeometry.ZFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;

/**
 * an abstract class to define a site and boundary
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/10
 * @time 16:54
 */
public abstract class SiteBase {
    private Polygon site;                            // 场地轮廓
    private Polygon boundary;                        // 建筑轮廓

    private double boundaryArea = 0;

    /* ------------- constructor ------------- */

    public SiteBase(Polygon _site) {
        setSite(_site);
    }

    public SiteBase(Polygon _site, Polygon _boundary) {
        setSite(_site);
        setBoundary(_boundary);
    }

    /* ------------- member function ------------- */

    public void updateByNodes(Coordinate[] boundaryNodes) {
        setBoundary(ZFactory.jtsgf.createPolygon(boundaryNodes));
    }

    public abstract void updateByParams(int param1, double param2, double param3);

    /* ------------- setter & getter ------------- */

    public Polygon getSite() {
        return site;
    }

    public Polygon getBoundary() {
        return boundary;
    }

    public double getBoundaryArea() {
        return boundaryArea;
    }

    public void setSite(Polygon site) {
        this.site = site;
    }

    public void setBoundary(Polygon boundary) {
        this.boundary = boundary;
        this.boundaryArea = boundary.getArea();
    }

    /* ------------- draw ------------- */
}
