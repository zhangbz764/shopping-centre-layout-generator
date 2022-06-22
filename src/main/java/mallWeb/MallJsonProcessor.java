package mallWeb;

import archijson.ArchiJSON;
import basicGeometry.ZLine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import geometry.Segments;
import geometry.Vertices;
import main.MallGenerator;
import mallElementNew.StructureGrid;
import mallIO.ImportData;
import mallParameters.MallConst;
import org.locationtech.jts.geom.LineString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Baizhou zhangbz
 * @project shopping_mall
 * @date 2022/2/18
 * @time 13:21
 */
public class MallJsonProcessor {
    private final ImportData input = new ImportData(); // temp
    private final MallGenerator mg;
    private final Gson gson = new Gson();

    /* ------------- constructor ------------- */

    public MallJsonProcessor() {
        // local import
        String importPath = "./src/main/resources/0310.3dm";
        this.input.loadData(importPath);

        this.mg = new MallGenerator();
    }

    /* ------------- member function ------------- */

    /**
     * back-end process for edit status 0
     *
     * @param functionID ID of function
     * @param jsonR      received Archijson
     * @param jsonS      Archijson to send
     * @param properties properties to send
     * @return void
     */
    public void processStatus0(
            int functionID,
            ArchiJSON jsonR,
            ArchiJSON jsonS,
            JsonObject properties
    ) {
        List<JsonElement> elements = new ArrayList<>();

        switch (functionID) {
            case MallConst.INIT_FLAG:
                mg.initSiteBoundary(
                        input.getInputBlock(),
                        input.getInputBoundary(),
                        0,
                        MallConst.SITE_REDLINEDIST_DEFAULT,
                        MallConst.SITE_BUFFER_DEFAULT
                );

                Segments b1 = JTSConverter.toSegments(mg.getSiteBase().getBoundary());
                Segments s1 = JTSConverter.toSegments(mg.getSiteBase().getSite());
                elements.add(gson.toJsonTree(b1));
                elements.add(gson.toJsonTree(s1));
                break;
            case MallConst.BUTTON_SWITCH_BOUNDARY:
            case MallConst.SLIDER_OFFSET_DIST:
            case MallConst.SLIDER_REDLINE_DIST:
                int boundaryBase = jsonR.getProperties().get("boundaryBase").getAsInt();
                double redLineDist = jsonR.getProperties().get("redLineDist").getAsDouble();
                double siteOffsetDist = jsonR.getProperties().get("siteOffsetDist").getAsDouble();
                mg.updateSiteBaseL(boundaryBase, redLineDist, siteOffsetDist);

                Segments b2 = JTSConverter.toSegments(mg.getSiteBase().getBoundary());
                elements.add(gson.toJsonTree(b2));
                break;
        }

        jsonS.setGeometryElements(elements);
        jsonS.setProperties(properties);
    }

    /**
     * back-end process for edit status 0
     *
     * @param functionID ID of function
     * @param jsonR      received Archijson
     * @param jsonS      Archijson to send
     * @param properties properties to send
     * @return void
     */
    public void processStatus4(
            int functionID,
            ArchiJSON jsonR,
            ArchiJSON jsonS,
            JsonObject properties
    ) {
        List<JsonElement> elements = new ArrayList<>();

        switch (functionID) {
            case MallConst.INIT_FLAG:
                mg.initGrid(MallConst.STRUCTURE_GRID_NUM, MallConst.STRUCTURE_MODEL);

                break;
            case MallConst.BUTTON_GRID_8:
            case MallConst.BUTTON_GRID_9:
                double gridModulus = jsonR.getProperties().get("gridModulus").getAsDouble();
                mg.updateGridModulus(gridModulus);

                break;
            case MallConst.BUTTON_GRIDNUM_1:
            case MallConst.BUTTON_GRIDNUM_2:
            case MallConst.BUTTON_GRIDNUM_3:
                int gridNum = jsonR.getProperties().get("gridNum").getAsInt();
                mg.initGrid(gridNum, MallConst.STRUCTURE_MODEL);

                break;
        }

        List<Segments> gridLines = new ArrayList<>();
        for (int i = 0; i < mg.getGrids().length; i++) {
            StructureGrid g = mg.getGrids()[i];
            for (ZLine l : g.getAllLines()) {
                LineString ls = l.toJtsLineString();
                Segments seg = JTSConverter.toSegments(ls);
                elements.add(gson.toJsonTree(seg));
            }
        }

        jsonS.setGeometryElements(elements);
        jsonS.setProperties(properties);
    }

    /* ------------- setter & getter ------------- */


}
