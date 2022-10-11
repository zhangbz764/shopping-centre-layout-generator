package mallWeb;

import archijson.ArchiJSON;
import basicGeometry.ZFactory;
import basicGeometry.ZLine;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import converter.JTS_Converter;
import converter.WB_Converter;
import geometry.Plane;
import geometry.Segments;
import geometry.Vertices;
import main.MallGenerator;
import mallElementNew.StructureGrid;
import mallIO.ImportData;
import mallParameters.MallConst;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;

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

    private static final int codeSuccess = 200;
    private static final int codeFail = 400;

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

        Segments boundary, site;

        switch (functionID) {
            case MallConst.INIT_FLAG:
                mg.initSiteBoundary(
                        input.getInputBlock(),
                        input.getInputBoundary(),
                        0,
                        MallConst.SITE_REDLINEDIST_DEFAULT,
                        MallConst.SITE_BUFFER_DEFAULT
                );

                boundary = JTSConverter.toSegments(mg.getSiteBase().getBoundary());
                site = JTSConverter.toSegments(mg.getSiteBase().getSite());
                elements.add(gson.toJsonTree(boundary));
                elements.add(gson.toJsonTree(site));
                break;
            case MallConst.BUTTON_SWITCH_BOUNDARY:
            case MallConst.SLIDER_OFFSET_DIST:
            case MallConst.SLIDER_REDLINE_DIST:
                int boundaryBase = jsonR.getProperties().get("boundaryBase").getAsInt();
                double redLineDist = jsonR.getProperties().get("redLineDist").getAsDouble();
                double siteOffsetDist = jsonR.getProperties().get("siteOffsetDist").getAsDouble();
                mg.updateSiteBaseL(boundaryBase, redLineDist, siteOffsetDist);

                boundary = JTSConverter.toSegments(mg.getSiteBase().getBoundary());
                elements.add(gson.toJsonTree(boundary));
                break;
        }

        jsonS.setGeometryElements(elements);
        jsonS.setProperties(properties);
    }

    /**
     * back-end process for edit status 1
     *
     * @param functionID ID of function
     * @param jsonR      received Archijson
     * @param jsonS      Archijson to send
     * @param properties properties to send
     * @return void
     */
    public void processStatus1(
            int functionID,
            ArchiJSON jsonR,
            ArchiJSON jsonS,
            JsonObject properties
    ) {
        List<JsonElement> elements = new ArrayList<>();

        // traffic
        Segments trafficPath, trafficBuffer;
        Vertices entryVer, innerVer;
        List<WB_Coord> entryCoords, innerCoords;
        double bufferDist;
        // atrium
        int atriumShapeID;
        double atriumPosX, atriumPosY;
        int activatedID;
        int atriumDragFlag;
        int currCtrlID;
        Segments changedAtrium;

        switch (functionID) {
            case MallConst.INIT_FLAG:
                Polygon boundary_receive = JTSConverter.toPolygon((Segments) jsonR.getGeometries().get(0));

                mg.updateBoundary(boundary_receive);
                mg.initTraffic(MallConst.TRAFFIC_BUFFER_DIST_DEFAULT);

                trafficPath = JTSConverter.toSegments(mg.getMainTraffic().getMainTrafficCurve());
                trafficBuffer = JTSConverter.toSegments(mg.getMainTraffic().getMainTrafficBuffer());
                entryCoords = new ArrayList<>(mg.getMainTraffic().getEntryNodes());
                entryVer = WB_Converter.toVertices(entryCoords, 3);
                innerCoords = new ArrayList<>(mg.getMainTraffic().getInnerNodes());
                innerVer = WB_Converter.toVertices(innerCoords, 3);

                elements.add(gson.toJsonTree(trafficPath));
                elements.add(gson.toJsonTree(trafficBuffer));
                elements.add(gson.toJsonTree(entryVer));
                elements.add(gson.toJsonTree(innerVer));
                break;
            case MallConst.DRAG_TRAFFIC_CTRL:
                List<WB_Point> entryNode_receive = WB_Converter.toWB_Point((Vertices) jsonR.getGeometries().get(0));
                List<WB_Point> innerNode_receive = WB_Converter.toWB_Point((Vertices) jsonR.getGeometries().get(1));
                bufferDist = jsonR.getProperties().get("trafficWidth").getAsDouble();

                mg.updateTrafficByRawNodes(entryNode_receive, innerNode_receive, bufferDist);

                trafficPath = JTSConverter.toSegments(mg.getMainTraffic().getMainTrafficCurve());
                trafficBuffer = JTSConverter.toSegments(mg.getMainTraffic().getMainTrafficBuffer());
                entryCoords = new ArrayList<>(mg.getMainTraffic().getEntryNodes());
                entryVer = WB_Converter.toVertices(entryCoords, 3);
                innerCoords = new ArrayList<>(mg.getMainTraffic().getInnerNodes());
                innerVer = WB_Converter.toVertices(innerCoords, 3);

                elements.add(gson.toJsonTree(trafficPath));
                elements.add(gson.toJsonTree(trafficBuffer));
                elements.add(gson.toJsonTree(entryVer));
                elements.add(gson.toJsonTree(innerVer));
                break;
            case MallConst.SLIDER_TRAFFIC_WIDTH:
                bufferDist = jsonR.getProperties().get("trafficWidth").getAsDouble();

                mg.updateTrafficWidth(bufferDist);

                trafficBuffer = JTSConverter.toSegments(mg.getMainTraffic().getMainTrafficBuffer());

                elements.add(gson.toJsonTree(trafficBuffer));
                break;
            case MallConst.DBCLICK_ADD_ATRIUM:
                atriumShapeID = jsonR.getProperties().get("atriumShapeID").getAsInt();
                atriumPosX = jsonR.getProperties().get("atriumPosX").getAsDouble();
                atriumPosY = jsonR.getProperties().get("atriumPosY").getAsDouble();

                mg.addAtriumRaw(atriumPosX, atriumPosY, atriumShapeID);
                List<Polygon> atriumRawShapes = mg.getAtriumRawShapes();
                for (Polygon a : atriumRawShapes) {
                    Segments atriumSeg = JTSConverter.toSegments(a);
                    elements.add(gson.toJsonTree(atriumSeg));
                }
                break;
            case MallConst.DBCLICK_SEL_ATRIUM:
                atriumPosX = jsonR.getProperties().get("atriumPosX").getAsDouble();
                atriumPosY = jsonR.getProperties().get("atriumPosY").getAsDouble();
                activatedID = jsonR.getProperties().get("atriumActivatedID").getAsInt();

                int[] result = this.getSelectID(activatedID, atriumPosX, atriumPosY);
                properties.addProperty("statusCode", result[0]);
                properties.addProperty("activatedID", result[1]);

                break;
            case MallConst.DRAG_ATRIUM_CTRL:
                activatedID = jsonR.getProperties().get("activatedID").getAsInt();
                atriumDragFlag = jsonR.getProperties().get("dragFlag").getAsInt();
                currCtrlID = jsonR.getProperties().get("currCtrlID").getAsInt();
                atriumPosX = jsonR.getProperties().get("posX").getAsDouble();
                atriumPosY = jsonR.getProperties().get("posY").getAsDouble();

                mg.updateAtriumRawByCtrls(
                        activatedID,
                        atriumDragFlag,
                        currCtrlID,
                        new WB_Point(atriumPosX, atriumPosY)
                );

                changedAtrium = JTSConverter.toSegments(mg.getChangedAtriumRawShape(activatedID));
                elements.add(gson.toJsonTree(changedAtrium));

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

    /* ------------- private functions ------------- */

    /**
     * get select/unselect results for atrium
     *
     * @param actID activated atrium ID
     * @param x     mouse x
     * @param y     mouse y
     * @return int[]
     */
    private int[] getSelectID(int actID, double x, double y) {
        if (actID == -1) {
            // select
            int id = -1;
            List<Polygon> atriumShapes = mg.getAtriumRawShapes();
            for (int i = 0; i < atriumShapes.size(); i++) {
                Polygon shp = atriumShapes.get(i);
                if (shp.contains(ZFactory.jtsgf.createPoint(new Coordinate(x, y)))) {
                    id = i;
                    break;
                }
            }
            if (id > -1) {
                return new int[]{codeSuccess, id}; // select success
            } else {
                return new int[]{codeFail, -1}; // not select any atrium
            }
        } else if (actID > -1) {
            // unselect
            List<Polygon> atriumShapes = mg.getAtriumRawShapes();
            Polygon shp = atriumShapes.get(actID);
            if (!shp.contains(ZFactory.jtsgf.createPoint(new Coordinate(x, y)))) {
                return new int[]{codeSuccess, -1};
            } else {
                return new int[]{codeFail, -1};
            }
        } else {
            return new int[]{codeFail, -1};
        }
    }
}
