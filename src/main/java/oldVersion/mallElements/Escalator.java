package oldVersion.mallElements;

import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import oldVersion.MallConstant;
import processing.core.PApplet;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.List;

/**
 * the public escalator in a shopping mall
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2020/10/12
 * @time 10:07
 */
public class Escalator {
    private ZPoint location; // base point
    private ZPoint dir; // escalator direction
    private double width = 3.6 * MallConstant.SCALE;
    private double length = 14 * MallConstant.SCALE;
    private ZLine atriumEdge = null; // base edge of atrium
    private WB_Polygon shape; // final shape rectangle

    private static double serviceRadius = MallConstant.ESCALATOR_RADIUS * MallConstant.SCALE;

    /* ------------- constructor ------------- */

    public Escalator(AtriumOld atrium) {
        initShape(atrium);
    }

    public Escalator(AtriumOld atrium, double scale) {
        setScale(scale);
        initShape(atrium);
    }

    /* ------------- member function ------------- */

    /**
     * initialize the +shape of escalator
     *
     * @param atrium input atrium to generate
     * @return void
     */
    private void initShape(AtriumOld atrium) {
        // find a valid atrium edge to generate a escalator
        List<ZLine> possibleAtriumSide = atrium.getMainSegmentsOfAtrium();
        for (ZLine possible : possibleAtriumSide) {
            if (possible.getLength() > length) { // scale ?
                atriumEdge = possible;
                break;
            }
        }
        if (atriumEdge != null) {
            this.dir = atriumEdge.getDirectionNor();
            this.location = atriumEdge.getCenter();

            // atrium polygon face up or down
            if (atrium.getPolygon().getNormal().zd() < 0) {
                ZPoint p0 = location.add(dir.scaleTo(length * 0.5));
                ZPoint p1 = location.add(dir.scaleTo(length * -0.5));
                ZPoint p2 = p1.add(dir.rotate2D(Math.PI * -0.5).scaleTo(width));
                ZPoint p3 = p0.add(dir.rotate2D(Math.PI * -0.5).scaleTo(width));

                WB_Point[] shapePoints = new WB_Point[]{
                        p0.toWB_Point(),
                        p1.toWB_Point(),
                        p2.toWB_Point(),
                        p3.toWB_Point(),
                        p0.toWB_Point()
                };
                this.shape = new WB_Polygon(shapePoints);
            } else {
                ZPoint p0 = location.add(dir.scaleTo(length * -0.5));
                ZPoint p1 = location.add(dir.scaleTo(length * 0.5));
                ZPoint p2 = p1.add(dir.rotate2D(Math.PI * 0.5).scaleTo(width));
                ZPoint p3 = p0.add(dir.rotate2D(Math.PI * 0.5).scaleTo(width));

                WB_Point[] shapePoints = new WB_Point[]{
                        p0.toWB_Point(),
                        p1.toWB_Point(),
                        p2.toWB_Point(),
                        p3.toWB_Point(),
                        p0.toWB_Point()
                };
                this.shape = new WB_Polygon(shapePoints);
            }
        } else {
            System.out.println("cannot generate escalator here");
        }
    }

    /* ------------- setter & getter ------------- */

    public static void setServiceRadius(double serviceRadius) {
        Escalator.serviceRadius = serviceRadius;
    }

    public void setScale(double scale) {
        this.width *= scale;
        this.length *= scale;
    }

    public ZPoint getLocation() {
        return location;
    }

    /* ------------- draw ------------- */

    public void displayShape(WB_Render render, PApplet app) {
        if (shape != null) {
            app.noFill();
            app.stroke(200);
            app.strokeWeight(1);
            render.drawPolygonEdges2D(shape);
        }
    }

    public void displayCoverRegion(PApplet app) {
        if (shape != null) {
            app.noFill();
            app.stroke(255, 0, 0);
            app.strokeWeight(1.5f);
            app.ellipse(location.xf(), location.yf(), (float) (serviceRadius * 2), (float) (serviceRadius * 2));
        }
    }
}
