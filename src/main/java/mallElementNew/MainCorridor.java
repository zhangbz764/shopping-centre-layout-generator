package mallElementNew;

import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.List;

/**
 * description
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/8/9
 * @time 17:03
 */
public class MainCorridor {
    private List<ZLine> corridors;
    private List<Polygon> atriums;
    private LineString centralSkel;

    /* ------------- constructor ------------- */

    public MainCorridor(Polygon publicSpace, LineString centralSkel) {
        this.centralSkel = centralSkel;

    }

    /* ------------- member function ------------- */


    /* ------------- setter & getter ------------- */



    /* ------------- draw ------------- */
}
