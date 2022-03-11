package mallElementNew;

import org.locationtech.jts.geom.Polygon;

/**
 * define a site & boundary by input (no red line)
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/3/10
 * @time 16:59
 */
public class SiteBase_Input extends SiteBase {


    /* ------------- constructor ------------- */

    public SiteBase_Input(Polygon _site, Polygon _boundary) {
        super(_site, _boundary);
    }


    /* ------------- member function ------------- */

    @Override
    public void updateByParams(int param1, double param2, double param3) {

    }

    /* ------------- setter & getter ------------- */



    /* ------------- draw ------------- */
}
