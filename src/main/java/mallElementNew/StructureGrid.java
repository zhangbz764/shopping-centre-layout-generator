package mallElementNew;

import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * a construction grid
 * should generate based on ZRectCover
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/5/14
 * @time 12:46
 */
public class StructureGrid {
    private Polygon rect;
    private double model = -1;
    private double[] threshold;

    private double length10;
    private double length12;
    private double lengthUnit10;
    private double lengthUnit12;

    private List<ZLine> lon10; // lines start from 10 edge
    private List<ZLine> lat12; // lines start from 12 edge
    private int size10;
    private int size12;

    private ZPoint[][] gridNodes; // all grid nodes
    private ZPoint unit10; // unit along 10 direction
    private ZPoint unit12; // unit along 12 direction

    /* ------------- constructor ------------- */

    public StructureGrid(WB_Polygon rectangle, double[] threshold) {

    }

    public StructureGrid(Polygon rect, double model) {
        this.rect = rect;
        this.model = model;
        initModel();
    }

    public StructureGrid(Polygon rect, double[] threshold) {
        this.threshold = threshold;
        this.rect = rect;
        initThreshold();
    }

    /* ------------- member function ------------- */

    /**
     * initialize grid by a given model
     *
     * @return void
     */
    public void initModel() {
        Coordinate c0 = rect.getCoordinates()[0];
        Coordinate c1 = rect.getCoordinates()[1];
        Coordinate c2 = rect.getCoordinates()[2];

        this.length10 = c0.distance(c1);
        this.length12 = c1.distance(c2);

        ZLine line10 = new ZLine(new ZPoint(c1), new ZPoint(c0));
        ZLine line12 = new ZLine(new ZPoint(c1), new ZPoint(c2));

        ZPoint dir10 = new ZPoint(c0.x - c1.x, c0.y - c1.y);
        ZPoint dir12 = new ZPoint(c2.x - c1.x, c2.y - c1.y);

        // lines
        List<ZPoint> dividePoint10 = line10.divideByStep(model);
        List<ZPoint> dividePoint12 = line12.divideByStep(model);

        this.lon10 = new ArrayList<>();
        this.lat12 = new ArrayList<>();

        for (ZPoint pt : dividePoint10) {
            lon10.add(new ZLine(pt, pt.add(dir12)));
        }
        this.size10 = lon10.size();;
        for (ZPoint pt : dividePoint12) {
            lat12.add(new ZLine(pt, pt.add(dir10)));
        }
        this.size12 = lat12.size();

        // nodes
        ZPoint start = dividePoint10.get(0);
        this.unit10 = dividePoint10.get(1).sub(dividePoint10.get(0));
        this.lengthUnit10 = unit10.getLength();
        this.unit12 = dividePoint12.get(1).sub(dividePoint12.get(0));
        this.lengthUnit12 = unit12.getLength();
        this.gridNodes = new ZPoint[dividePoint10.size()][dividePoint12.size()];
        for (int i = 0; i < dividePoint10.size(); i++) {
            for (int j = 0; j < dividePoint12.size(); j++) {
                gridNodes[i][j] = start.add(unit10.scaleTo(i)).add(unit12.scaleTo(j));
            }
        }
    }

    /**
     * initialize grid by a model threshold
     *
     * @return void
     */
    public void initThreshold() {
        Coordinate c0 = rect.getCoordinates()[0];
        Coordinate c1 = rect.getCoordinates()[1];
        Coordinate c2 = rect.getCoordinates()[2];

        this.length10 = c0.distance(c1);
        this.length12 = c1.distance(c2);

        ZLine line10 = new ZLine(new ZPoint(c1), new ZPoint(c0));
        ZLine line12 = new ZLine(new ZPoint(c1), new ZPoint(c2));

        ZPoint dir10 = new ZPoint(c0.x - c1.x, c0.y - c1.y);
        ZPoint dir12 = new ZPoint(c2.x - c1.x, c2.y - c1.y);

        // lines
        List<ZPoint> dividePoint10 = line10.divideByThreshold(threshold[0], threshold[1]);
        List<ZPoint> dividePoint12 = line12.divideByThreshold(threshold[0], threshold[1]);

        this.lon10 = new ArrayList<>();
        this.lat12 = new ArrayList<>();

        for (ZPoint pt : dividePoint10) {
            lon10.add(new ZLine(pt, pt.add(dir12)));
        }
        for (ZPoint pt : dividePoint12) {
            lat12.add(new ZLine(pt, pt.add(dir10)));
        }

        // nodes
        ZPoint start = dividePoint10.get(0);
        this.unit10 = dividePoint10.get(1).sub(dividePoint10.get(0));
        this.lengthUnit10 = unit10.getLength();
        this.unit12 = dividePoint12.get(1).sub(dividePoint12.get(0));
        this.lengthUnit12 = unit12.getLength();
        this.gridNodes = new ZPoint[dividePoint10.size()][dividePoint12.size()];
        for (int i = 0; i < dividePoint10.size(); i++) {
            for (int j = 0; j < dividePoint12.size(); j++) {
                gridNodes[i][j] = start.add(unit10.scaleTo(i)).add(unit12.scaleTo(j));
            }
        }
    }

    /**
     * update grid by new rectangle
     *
     * @param rect new rectangle
     * @return void
     */
    public void updateRect(Polygon rect) {
        if (model > 0) {
            this.rect = rect;
            initModel();
        } else if (threshold != null) {
            this.rect = rect;
            initThreshold();
        }
    }

    /**
     * update grid by new model
     *
     * @param model new model
     * @return void
     */
    public void updateModel(double model) {
        this.model = model;
        initModel();
    }

    /**
     * update grid by new threshold
     *
     * @param threshold new model
     * @return void
     */
    public void updateThreshold(double[] threshold) {
        this.threshold = threshold;
        initThreshold();
    }

    /* ------------- setter & getter ------------- */

    public Polygon getRect() {
        return rect;
    }

    public double getLength10() {
        return length10;
    }

    public double getLength12() {
        return length12;
    }

    public double getLengthUnit10() {
        return lengthUnit10;
    }

    public double getLengthUnit12() {
        return lengthUnit12;
    }

    public List<ZLine> getLon10() {
        return lon10;
    }

    public List<ZLine> getLat12() {
        return lat12;
    }

    public int getSize10() {
        return size10;
    }

    public int getSize12() {
        return size12;
    }

    public ZPoint[][] getGridNodes() {
        return gridNodes;
    }

    public ZPoint getUnit10() {
        return unit10;
    }

    public ZPoint getUnit12() {
        return unit12;
    }

    public List<ZLine> getAllLines() {
        List<ZLine> result = new ArrayList<>();
        result.addAll(lon10);
        result.addAll(lat12);
        return result;
    }

    public int[] getLineIndex(ZLine line) {
        if (lon10.contains(line)) {
            return new int[]{0, lon10.indexOf(line)};
        } else if (lat12.contains(line)) {
            return new int[]{1, lat12.indexOf(line)};
        } else {
            return new int[]{-1, -1};
        }
    }

//    public int[] getFormerNode(ZPoint ptOnLine, ZLine line) {
//        if (lon01.contains(line)) {
//            int i = lon01.indexOf(line);
//
//        }
//    }

    /* ------------- draw ------------- */


    /* ------------- inner class ------------- */
}
