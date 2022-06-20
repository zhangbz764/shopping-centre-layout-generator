package main;

import advancedGeometry.rectCover.ZRectCover;
import basicGeometry.ZLine;
import mallElementNew.*;
import mallParameters.MallConst;
import math.ZGeoMath;
import org.locationtech.jts.geom.*;
import processing.core.PApplet;
import render.JtsRender;
import render.ZRender;
import transform.ZTransform;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;
import wblut.processing.WB_Render;

import java.util.ArrayList;
import java.util.List;

/**
 * main generator
 * catch data from frontend
 * convert data to frontend
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project archijson
 * @date 2021/3/10
 * @time 17:55
 */
public class MallGenerator {
    // SiteBase: site & boundary
    private SiteBase siteBase;
    private double boundaryArea = 0;

    // MainTraffic: main traffic & raw atriums
    private MainTraffic mainTraffic;
    private LineString innerTrafficCurve;          // 内部主路径轴线（除去入口）

    // PublicSpace: main corridor & public space
    private Polygon[] rawAtrium_receive;       // 中庭多边形（共用）
    private PublicSpace publicSpace;

    // structure grid
    private StructureGrid[] grids;                  // 结构轴网（共用）
    private boolean validGrids = true;              // 轴网是否覆盖平面
    private boolean gridModelSwitch = true;         // 8.4m 或 9m 切换

    // ShopManager: shop cells
    private ShopManager shopManager;

//    // core generate result: floors
//    private MallFloor[] floors;

    // AuxiliarySpace: evacuation stairways & washrooms
    private AuxiliarySpace auxiliarySpace;


    private List<List<LineString>> bufferCurve_receive;   // 动线边界曲线（不同层）
//    private List<List<WB_Polygon>> cellPolys_receive;     // 商铺剖分多边形（不同层）

    /* ------------- constructor ------------- */

    public MallGenerator() {

    }

    /* ------------- utils ------------- */

    public String getTrafficLength() {
        String s = "";
        if (mainTraffic != null) {
            s = s + String.format("%.2f", mainTraffic.getTrafficLength()) + "m";
        }
        return s;
    }

    public String getInnerTrafficLength() {
        String s = "";
        if (innerTrafficCurve != null) {
            s = s + String.format("%.2f", innerTrafficCurve.getLength()) + "m";
        }
        return s;
    }

    /* ------------- generating site & boundary ------------- */

    /**
     * initialize site and boundary
     *
     * @param _site     input site
     * @param _boundary input boundary (null allowed)
     * @param param1    input parameter 1 (int)
     * @param param2    input parameter 2 (double)
     * @param param3    input parameter 3 (double)
     * @return void
     */
    public void initSiteBoundary(WB_Polygon _site, WB_Polygon _boundary, int param1, double param2, double param3) {
        if (_boundary == null) {
            // init SiteBase_L
            this.siteBase = new SiteBase_L(
                    ZTransform.WB_PolygonToPolygon(_site),
                    param1, param2, param3
            );
        } else {
            this.siteBase = new SiteBase_Input(
                    ZTransform.WB_PolygonToPolygon(_site),
                    ZTransform.WB_PolygonToPolygon(_boundary)
            );
        }
        this.boundaryArea = siteBase.getBoundaryArea();
    }

    /**
     * update SiteBase_L parameters
     *
     * @param base           base point (L shape)
     * @param redLineDist    red line distance
     * @param siteBufferDist L shape buffer distance
     * @return void
     */
    public void updateSiteBaseL(int base, double redLineDist, double siteBufferDist) {
        siteBase.updateByParams(base, redLineDist, siteBufferDist);
        this.boundaryArea = siteBase.getBoundaryArea();
    }

    /**
     * update boundary by nodes
     *
     * @param boundaryNodes_receive nodes received from MallInteract
     * @return void
     */
    public void updateBoundaryByNodes(Coordinate[] boundaryNodes_receive) {
        this.siteBase.updateByNodes(boundaryNodes_receive);
        this.boundaryArea = siteBase.getBoundaryArea();
    }

    /* ------------- generating main traffic ------------- */

    /**
     * initialize main traffic
     *
     * @param bufferDist distance to buffer
     * @return void
     */
    public void initTraffic(double bufferDist) {
        this.mainTraffic = new MainTraffic(siteBase.getBoundary(), bufferDist);
    }

    /**
     * update main traffic
     *
     * @param innerNode_receive received inner node
     * @param entryNode_receive received entry node
     * @param bufferDist        distance to buffer
     * @return void
     */
    public void updateTraffic(List<WB_Point> innerNode_receive, List<WB_Point> entryNode_receive, double bufferDist) {
        mainTraffic.updateTraffic(innerNode_receive, entryNode_receive, bufferDist);
    }

    /**
     * update main traffic
     *
     * @param bufferDist distance to buffer
     * @return void
     */
    public void updateTrafficWidth(double bufferDist) {
        mainTraffic.updateTrafficWidth(bufferDist);
    }

    /* ------------- generating main corridor & public space ------------- */

    /**
     * initialize the main corridor
     *
     * @param corridorWidth width of corridor
     * @return void
     */
    public void initMainCorridor(double corridorWidth) {
        this.publicSpace = new PublicSpace(rawAtrium_receive, mainTraffic.getMainTrafficCurve());
    }

    /**
     * update main corridor position
     *
     * @param corridorID id of changed corridor
     * @param newNodes   new corridor nodes
     * @return void
     */
    public void updateMainCorridorPos(int corridorID, WB_Point[] newNodes) {
        if (corridorID > -1) {
            publicSpace.updateCorridorPos(corridorID, new ZLine(newNodes[0], newNodes[1]));
        }
    }

    /**
     * update main corridor width
     *
     * @param corridorID id of changed corridor
     * @param width      new width
     * @return void
     */
    public void updateMainCorridorWidth(int corridorID, double width) {
        if (corridorID > -1) {
            publicSpace.updateCorridorWidth(corridorID, width);
        }
    }

    /*temp*/
    public void setPublicSpaceShapeTemp(Polygon publicSpaceShape) {
        this.publicSpace = new PublicSpace();
        this.publicSpace.setPublicSpaceShapeTemp(publicSpaceShape);
    }

    /**
     * initialize public traffic space
     *
     * @return void
     */
    public void initPublicSpaceShape() {
        publicSpace.initPublicShape();
    }

    /**
     * update public space by a new set of control points
     *
     * @param publicSpaceNode_interact new control points
     * @return void
     */
    public void updatePublicSpace(List<WB_Point> publicSpaceNode_interact) {
        Coordinate[] ctrls = new Coordinate[publicSpaceNode_interact.size()];
        for (int i = 0; i < ctrls.length; i++) {
            ctrls[i] = ZTransform.WB_CoordToCoordinate(publicSpaceNode_interact.get(i));
        }
        publicSpace.updatePublicShape(ctrls);
    }

    /**
     * update public space by buffer distance
     *
     * @param dist buffer distance
     * @return void
     */
    public void updatePublicSpaceBuffer(double dist) {
        publicSpace.updatePublicShapeBuffer(dist);
    }

    /**
     * switch between round mode and smooth mode
     *
     * @param atriumID index of the atrium to switch
     * @return void
     */
    public void switchAtriumRoundType(int atriumID) {
        publicSpace.switchAtriumRoundType(atriumID);
    }

    /**
     * update the radius of rounding atrium
     *
     * @param atriumID index of atrium
     * @param radius   new radius
     * @return void
     */
    public void updateAtriumRoundRadius(int atriumID, double radius) {
        publicSpace.updateAtriumRoundRadius(atriumID, radius);
    }

    /**
     * update smooth times of the atrium
     *
     * @param atriumID index of atrium
     * @param times    smooth count times
     * @return void
     */
    public void updateAtriumSmoothTimes(int atriumID, int times) {
        publicSpace.updateAtriumSmoothTimes(atriumID, times);
    }

    /* ------------- generating escalators ------------- */

    /**
     * initialize escalators in public space
     *
     * @return void
     */
    public void initEscalators() {
        publicSpace.initEscalators(mainTraffic.getMainTrafficCurve());
    }

    /**
     * update escalator position by counting ids
     *
     * @param atriumID the atrium id with selected escalator
     * @return void
     */
    public void updateEscalatorPos(int atriumID) {
        publicSpace.updateEscalatorPos(atriumID);
    }

    /* ------------- generating structure grid ------------- */

    /**
     * setup structure grids
     *
     * @param gridNum number of structure grids
     * @param dist    column distance
     * @return void
     */
    public void initGrid(int gridNum, double dist) {
        ZRectCover zrc = new ZRectCover(siteBase.getBoundary(), gridNum);
        List<Polygon> rects = zrc.getBestRects();
        this.grids = new StructureGrid[gridNum];
        for (int i = 0; i < rects.size(); i++) {
            grids[i] = new StructureGrid(rects.get(i), dist);
        }
    }

    /**
     * update structure grid given a new rectangle
     *
     * @param gridIndex index of changed grid
     * @param rect      new rectangle
     * @return void
     */
    public void updateGridByRect(int gridIndex, Polygon rect) {
        if (gridIndex > -1 && rect != null) {
            grids[gridIndex].updateRect(rect);
            Geometry union = grids[0].getRect();
            for (int i = 1; i < grids.length; i++) {
                union = union.union(grids[i].getRect());
            }
            union = union.buffer(1);
            this.validGrids = union.contains(siteBase.getBoundary());
        }
    }

    /**
     * switch grid model between 8.4m and 9m
     *
     * @return void
     */
    public void switchGridModulus() {
        this.gridModelSwitch = !gridModelSwitch;
        if (gridModelSwitch) {
            for (StructureGrid g : grids) {
                g.updateModel(MallConst.STRUCTURE_MODEL);
            }
        } else {
            for (StructureGrid g : grids) {
                g.updateModel(MallConst.STRUCTURE_MODEL_2);
            }
        }
    }

    /**
     * update structure grid modulus
     *
     * @param gridModulus modulus of the grid
     * @return void
     */
    public void updateGridModulus(double gridModulus) {
        for (StructureGrid g : grids) {
            g.updateModel(gridModulus);
        }
    }

    /* ------------- generating shop cells ------------- */

    /**
     * initialize shop cells
     *
     * @return void
     */
    public void initShopCells() {
        this.shopManager = new ShopManager();
        List<LineString> publicSpaceLS = new ArrayList<>(
                ZTransform.PolygonToLineString(publicSpace.getPublicSpaceShape())
        );

        shopManager.updateShopPartition(
                siteBase.getBoundary(),
                publicSpaceLS,
                publicSpace.getPublicSpaceShape().getInteriorPoint(),
                grids
        );
    }

    /**
     * split internal & external shops
     *
     * @param splitShopID selected id of shop to split
     * @return void
     */
    public void splitShopCell(List<Integer> splitShopID) {
        shopManager.updateSplit(splitShopID, publicSpace.getPublicSpaceShape(), grids);
    }

//    /**
//     * initialize shop cells
//     *
//     * @param floorNum number of floor
//     * @return void
//     */
//    public void initShopCells(int floorNum) {
//        if (floorNum == 1) {
//            floors[floorNum - 1] = new MallFloor(floorNum, siteBase.getBoundary());
//            floors[floorNum - 1].setStatus(0);
//            floors[floorNum - 1].updateSubdivision(publicSpace.getPublicSpaceShape(), grids);
//        } else {
//            // mainly here
//            floors[floorNum - 1] = new MallFloor(floorNum, siteBase.getBoundary());
//            floors[floorNum - 1].setStatus(0);
//            Point verify = publicSpace.getPublicSpaceShape().getInteriorPoint();
//            floors[floorNum - 1].setVerify(verify);
//            floors[floorNum - 1].updateSubdivision(publicSpace.getPublicSpaceShape(), grids);
//        }
//    }
//
//    /**
//     * split internal & external shops
//     *
//     * @param floorNum
//     * @param splitShopID
//     * @return void
//     */
//    public void splitShopCell(int floorNum, List<Integer> splitShopID) {
//        if (floorNum == 1) {
//
//        } else {
//            // mainly here
//            floors[floorNum - 1].updateSplit(splitShopID, publicSpace.getPublicSpaceShape(), grids);
//        }
//    }

    /* ------------- generating evacuations ------------- */

    /**
     * initialize evacuation stairway position
     *
     * @return void
     */
    public void initEvacStairwaysPos() {
        this.auxiliarySpace = new AuxiliarySpace();
        List<WB_Polygon> shopPolys = new ArrayList<>();
        for (Shop s : shopManager.getAllShops()) {
            WB_Polygon p = ZGeoMath.polygonFaceUp(s.getShapeWB());
            ZTransform.validateWB_Polygon(p);
            shopPolys.add(p);
        }

        double floorArea = boundaryArea;
        for (double a : publicSpace.getAtriumCurrAreas()) {
            floorArea -= a;
        }
        auxiliarySpace.initEvacuationGenerator(
                mainTraffic.getMainTrafficInnerWB(),
                shopPolys,
                floorArea,
                ZTransform.PolygonToWB_Polygon(siteBase.getBoundary())
        );
    }

    public void updateEvacPosByID(List<Integer> newEvacGenIDs) {
        auxiliarySpace.updateEvacuationGenerator(newEvacGenIDs);
    }

    public void generateEvacShape() {
        auxiliarySpace.generateStairwayShape(siteBase.getBoundary(), shopManager.getShopBlocks().get(0));
    }

    public void switchEvacDirection(int id) {
        auxiliarySpace.switchEvacDir(id, siteBase.getBoundary(), shopManager.getShopBlocks().get(0));
    }

    /* ------------- generating washrooms ------------- */

    public void initwashrooms() {
        auxiliarySpace.initWashroom(siteBase.getBoundary(), mainTraffic.getMainTrafficInnerLS());
    }

    /* ------------- setter & getter ------------- */

    public SiteBase getSiteBase() {
        return siteBase;
    }

    public Coordinate[] getBoundaryNodes() {
        return siteBase.getBoundary().getCoordinates();
    }

    public String getBoundaryAreaAsString() {
        return boundaryArea > 0 ? String.format("%.2f", boundaryArea) + "㎡" : "";
    }

    public MainTraffic getMainTraffic() {
        return mainTraffic;
    }

    public List<WB_Point> getTrafficInnerNodes() {
        return mainTraffic.getInnerNodes();
    }

    public List<WB_Point> getTrafficEntryNodes() {
        return mainTraffic.getEntryNodes();
    }

    public void setRawAtrium_receive(Polygon[] rawAtrium_receive) {
        this.rawAtrium_receive = rawAtrium_receive;
    }

    public PublicSpace getPublicSpace() {
        return publicSpace;
    }

    public List<WB_Point> getCorridorNode() {
        List<WB_Point> corridorNode = new ArrayList<>();
        for (ZLine l : publicSpace.getCorridorsLines()) {
            corridorNode.add(l.getPt0().toWB_Point());
            corridorNode.add(l.getPt1().toWB_Point());
        }
        return corridorNode;
    }

    public List<Polygon> getEscalatorBounds() {
        return publicSpace.getEscalatorBounds();
    }

    public List<Integer> getEscalatorAtriumIDs() {
        return publicSpace.getEscalatorAtriumIDs();
    }

    public Polygon getEscalatorBoundN(int escalatorAtriumID) {
        return publicSpace.getEscalatorBoundN(escalatorAtriumID);
    }

    public StructureGrid[] getGrids() {
        return grids;
    }

    public Polygon[] getGridRects() {
        Polygon[] rects = new Polygon[grids.length];
        for (int i = 0; i < grids.length; i++) {
            rects[i] = grids[i].getRect();
        }
        return rects;
    }

    public List<Shop> getShopCells() {
        return shopManager.getAllShops();
    }

    public void setShopCells(List<Polygon> shopCellPolys) {
        List<Shop> newShops = new ArrayList<>();
        for (Polygon p : shopCellPolys) {
            newShops.add(new Shop(p));
        }
        shopManager.setAllShops(newShops);
    }

//    public List<Shop> getShopCells(int floorNum) {
//        return floors[floorNum - 1].getAllShops();
//    }
//
//    public void setShopCells(int floorNum, List<Polygon> shopCellPolys) {
//        List<Shop> newShops = new ArrayList<>();
//        for (Polygon p : shopCellPolys) {
//            newShops.add(new Shop(p));
//        }
//        this.floors[floorNum - 1].setAllShops(newShops);
//    }

    public AuxiliarySpace getAuxiliarySpace() {
        return auxiliarySpace;
    }



    /* ------------- draw ------------- */

    public void displayLocal(PApplet app, WB_Render render, JtsRender jtsRender, int status) {
        app.pushStyle();
        switch (status) {
            case -1:
                break;
            case MallConst.E_SITE_BOUNDARY:
                displaySiteBoundaryLocal(app, jtsRender);
                break;
            case MallConst.E_TRAFFIC_ATRIUM:
                displaySiteBoundaryLocal(app, jtsRender);
                displayTrafficLocal(app, jtsRender);
                break;
            case MallConst.E_MAIN_CORRIDOR:
                displaySiteBoundaryLocal(app, jtsRender);
                displayTrafficLocal(app, jtsRender);
                displayMainCorridorLocal(app, jtsRender);
                break;
            case MallConst.E_PUBLIC_SPACE:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                break;
            case MallConst.E_ESCALATOR:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayEscalatorLocal(app, jtsRender);
                displayEscalatorRadiusLocal(app);
                break;
            case MallConst.E_STRUCTURE_GRID:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayEscalatorLocal(app, jtsRender);
                displayGridLocal(app);
                break;
            case MallConst.E_SHOP_EDIT:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayEscalatorLocal(app, jtsRender);
                displayGridLocal(app);
                displayShopCellsLocal(app, jtsRender);
                break;
            case MallConst.E_EVAC_STAIRWAY:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayEscalatorLocal(app, jtsRender);
                displayShopCellsLocal(app, jtsRender);
                displayGridLocal(app);
                displayEvacStairwayPosLocal(app);
                displayEvacStairwayLocal(app, jtsRender);
                break;
            case MallConst.E_WASHROOM:
                displaySiteBoundaryLocal(app, jtsRender);
                displayPublicSpaceLocal(app, jtsRender);
                displayEscalatorLocal(app, jtsRender);
                displayShopCellsLocal(app, jtsRender);
                displayGridLocal(app);
                displayEvacStairwayPosLocal(app);
                break;
        }
        app.popStyle();
    }

    public void displaySiteBoundaryLocal(PApplet app, JtsRender render) {
        // draw boundary and site
        app.noFill();
        app.stroke(255);
        app.strokeWeight(6);
        render.drawGeometry(siteBase.getBoundary());
        app.stroke(255, 0, 0);
        app.strokeWeight(3);
        render.drawGeometry(siteBase.getSite());
    }

    public void displayTrafficLocal(PApplet app, JtsRender jtsRender) {
        // draw traffic route and buffer area
        app.stroke(255);
        app.strokeWeight(1);
        jtsRender.drawGeometry(mainTraffic.getMainTrafficCurve());
        app.stroke(52, 170, 187);
        app.strokeWeight(3);
        jtsRender.drawGeometry(mainTraffic.getMainTrafficBuffer());
    }

    public void displayMainCorridorLocal(PApplet app, JtsRender jtsRender) {
        app.strokeWeight(3);
        app.stroke(55, 103, 171);
        app.noFill();
        for (Polygon a : publicSpace.getAtriumTrimShapes()) {
            jtsRender.drawGeometry(a);
        }

        app.strokeWeight(1);
        app.stroke(0, 255, 0);
        for (int i = 0; i < publicSpace.getCorridorsLines().size(); i++) {
            publicSpace.getCorridorsLines().get(i).display(app);
        }

        app.fill(255);
        app.textSize(2);
        for (int i = 0; i < publicSpace.getAtriumTrimShapes().length; i++) {
            Polygon p = publicSpace.getAtriumTrimShapes()[i];
            app.pushMatrix();
            app.scale(1, -1);
            app.translate(0, (float) (-2 * p.getCentroid().getY()));
            app.text(String.format("%.2f", publicSpace.getAtriumCurrAreas()[i]), (float) p.getCentroid().getX(), (float) p.getCentroid().getY());
            app.popMatrix();
        }
    }

    public void displayPublicSpaceLocal(PApplet app, JtsRender jtsRender) {
        app.noFill();
        app.stroke(52, 170, 187);
        app.strokeWeight(3);
        jtsRender.drawGeometry(publicSpace.getPublicSpaceShape());
        for (Polygon p : publicSpace.getAtriumCurrShapes()) {
            jtsRender.drawGeometry(p);
        }

        app.fill(255);
        app.textSize(2);
        for (int i = 0; i < publicSpace.getAtriumTrimShapes().length; i++) {
            Polygon p = publicSpace.getAtriumTrimShapes()[i];
            app.pushMatrix();
            app.scale(1, -1);
            app.translate(0, (float) (-2 * p.getCentroid().getY()));
            app.text(String.format("%.2f", publicSpace.getAtriumCurrAreas()[i]), (float) p.getCentroid().getX(), (float) p.getCentroid().getY());
            app.popMatrix();
        }
    }

    public void displayGridLocal(PApplet app) {
        app.strokeWeight(0.5f);
        if (validGrids) {
            app.stroke(128);
        } else {
            app.stroke(255, 0, 0);
        }

        for (StructureGrid grid : grids) {
            for (ZLine l : grid.getAllLines()) {
                l.display(app);
            }
        }
    }

    public void displayEscalatorLocal(PApplet app, JtsRender jtsRender) {
        app.noFill();
        app.stroke(255);
        app.strokeWeight(1);
        for (Polygon p : publicSpace.getEscalatorBounds()) {
            jtsRender.drawGeometry(p);
        }
        for (ZLine l : publicSpace.getEscalatorShapes()) {
            l.display(app);
        }
    }

    public void displayEscalatorRadiusLocal(PApplet app) {
        for (Polygon p : publicSpace.getEscalatorBounds()) {
            Point centroid = p.getCentroid();
            app.noFill();
            app.ellipse((float) centroid.getX(), (float) centroid.getY(), 2 * MallConst.ESCALATOR_DIST_MIN, 2 * MallConst.ESCALATOR_DIST_MIN);
            app.line(
                    (float) centroid.getX(),
                    (float) centroid.getY(),
                    (float) centroid.getX() + MallConst.ESCALATOR_DIST_MIN,
                    (float) centroid.getY()
            );
            app.fill(255);
            app.textSize(2);
            app.pushMatrix();
            app.scale(1, -1);
            app.translate(0, (float) (-2 * centroid.getY()));
            app.text("30m",
                    (float) centroid.getX() + 0.5f * MallConst.ESCALATOR_DIST_MIN,
                    (float) centroid.getY() + 1
            );
            app.popMatrix();
        }
    }

    public void displayShopCellsLocal(PApplet app, JtsRender jtsRender) {
        if (shopManager.getAllShops() != null) {
            app.strokeWeight(3);
            app.stroke(255);
            List<Shop> cells = shopManager.getAllShops();
            for (Shop s : cells) {
                jtsRender.drawGeometry(s.getShape());
            }
            app.noStroke();
            for (Shop s : shopManager.getAllShops()) {
                s.display(app, jtsRender);
            }

            app.fill(255);
            app.textSize(2);

            for (Shop s : shopManager.getAllShops()) {
                app.pushMatrix();
                app.scale(1, -1);
                app.translate(0, (float) (-2 * s.getCenter().getY()));
                s.displayText(app);
                app.popMatrix();
            }
        }
    }

    public void displayEvacStairwayPosLocal(PApplet app) {
        app.strokeWeight(3);
        app.stroke(255, 97, 136);
        app.noFill();
        for (ZLine path : auxiliarySpace.getCoveredPath()) {
            ZRender.drawZLine2D(app, path);
        }

//        app.noStroke();
//        app.fill(255, 97, 136);
//        for (ZPoint generator : auxiliarySpace.getSelGeneratorPos()) {
//            ZRender.drawZPoint(app, generator, 5);
//        }
    }

    public void displayEvacStairwayLocal(PApplet app, JtsRender jtsRender) {
        if (auxiliarySpace.getEvacShapes() != null) {
            app.pushMatrix();
            app.translate(0, 0, 0.5f);
            app.strokeWeight(3);
            app.stroke(255);
            app.fill(82);
            for (Polygon shape : auxiliarySpace.getStairwayShapePoly()) {
                jtsRender.drawGeometry(shape);
            }
            app.popMatrix();
        }
    }

//    public void displayEvacuationLocal(PApplet app, JtsRender jtsRender) {
//        app.pushStyle();
//        if (stairways != null) {
//            app.stroke(255);
//            for (Stairway s : stairways) {
//                app.fill(80);
//                jtsRender.drawGeometry(s.getBound());
//                app.fill(255, 0, 0);
//                s.getBase().displayAsPoint(app);
//                for (ZLine l : s.getShapes()) {
//                    l.display(app);
//                }
//            }
//        }
//        if (evacPoly != null) {
//            app.stroke(255);
//            app.fill(80);
//            for (Polygon p : evacPoly) {
//                jtsRender.drawGeometry(p);
//                // display shape
//                ZPoint v01 = new ZPoint(
//                        p.getCoordinates()[1].getX() - p.getCoordinates()[0].getX(),
//                        p.getCoordinates()[1].getY() - p.getCoordinates()[0].getY()
//                );
//                ZPoint v03 = new ZPoint(
//                        p.getCoordinates()[3].getX() - p.getCoordinates()[0].getX(),
//                        p.getCoordinates()[3].getY() - p.getCoordinates()[0].getY()
//                );
//                ZPoint v01Nor = v01.normalize();
//                ZPoint v03Nor = v03.normalize();
//
//                app.line(
//                        new ZPoint(p.getCoordinates()[0]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[0]).add(v01Nor.scaleTo(2.4)).yf(),
//                        new ZPoint(p.getCoordinates()[3]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[3]).add(v01Nor.scaleTo(2.4)).yf()
//                );
//                app.line(
//                        new ZPoint(p.getCoordinates()[1]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[1]).add(v01Nor.scaleTo(2.4)).yf(),
//                        new ZPoint(p.getCoordinates()[2]).add(v01Nor.scaleTo(2.4)).xf(),
//                        new ZPoint(p.getCoordinates()[2]).add(v01Nor.scaleTo(2.4)).yf()
//                );
//
//            }
//        }

//        if (newBound != null) {
//            app.stroke(255);
//            app.pushMatrix();
//            app.translate(500, 0);
//            jtsRender.drawGeometry(newBound);
//            app.popMatrix();
//        }
//
//        app.popStyle();
//    }

//    public void displayEvacuationRadiusLocal(PApplet app) {
//        app.pushStyle();
//        for (Stairway s : stairways) {
//            ZPoint base = s.getBase();
//            app.noFill();
//            app.stroke(255, 0, 0);
//            app.strokeWeight(1.2f);
//            app.ellipse(base.xf(), base.yf(), 100, 100);
//            app.line(
//                    base.xf(),
//                    base.yf(),
//                    base.xf() + 50,
//                    base.yf()
//            );
//            app.fill(255, 0, 0);
//            app.textSize(5);
//            app.pushMatrix();
//            app.scale(1, -1);
//            app.translate(0, (float) (-2 * base.yf()));
//            app.text("50m",
//                    base.xf() + 0.5f * 25,
//                    base.yf() + 1
//            );
//            app.popMatrix();
//        }
//        app.popStyle();
//    }


    /* ------------- deprecated ------------- */

    public void setBufferCurve_receive(int floorNum, List<LineString> bufferCurve_receive) {
        if (floorNum == 1) {
            this.bufferCurve_receive.set(0, bufferCurve_receive);
        } else {
            this.bufferCurve_receive.set(1, bufferCurve_receive);
        }
    }

    public void setBufferCurve_receive(List<List<LineString>> bufferCurve_receive) {
        this.bufferCurve_receive = bufferCurve_receive;
    }

    public void setCellPolys_receive(int floorNum, List<WB_Polygon> cellPolys_receive) {
//        this.cellPolys_receive.set(floorNum - 1, cellPolys_receive);
    }

//    public void setShopCells_receive(int floorNum, List<Shop> shopCell_receive) {
//        this.floors[floorNum - 1].setAllShops(shopCell_receive);
//    }

    public void setCellPolys_receive(List<List<WB_Polygon>> cellPolys_receive) {
//        this.cellPolys_receive = cellPolys_receive;
    }

//    public MallFloor[] getFloors() {
//        return floors;
//    }

//    public List<WB_Segment> getGraphSegments(int floorNum) {
//        return floors[floorNum - 1].getGraph().toWB_Segments();
//    }

//    public List<List<WB_Coord>> getBufferControlPoints(int floorNum) {
//        return floors[floorNum - 1].getBufferControlPoints();
//    }

//    public String getFloorStats(int floorNum) {
//        MallFloor floor = floors[floorNum - 1];
//
//        WB_Polygon bufferOut = ZFactory.wbgf.createBufferedPolygons2D(boundary_receive, MallConst.EVACUATION_WIDTH).get(0);
//        double bufferArea = Math.abs(bufferOut.getSignedArea());
//
////        double totalArea = Math.abs(boundary_receive.getSignedArea());
////        double shopArea = 0;
////        for (Polygon p : floor.getShopBlocks()) {
////            shopArea += p.getArea();
////        }
//        double shopArea = newBound.getArea();
//        for (Polygon ep : evacPoly) {
//            shopArea -= ep.getArea();
//        }
//
//        int shopNum = getShopCells(floorNum).size();
//        double trafficLength = 0;
//        for (ZEdge e : floor.getGraph().getAllEdges()) {
//            trafficLength += e.getLength();
//        }
//        double shopRatio = shopArea / bufferArea;
//
//        return "本层建筑面积 : " + String.format("%.2f", bufferArea) + " ㎡"
//                + "\n" + "可租赁面积 : " + String.format("%.2f", shopArea) + " ㎡"
//                + "\n" + "商铺总数量 : " + shopNum
//                + "\n" + "分层得铺率 : " + String.format("%.2f", shopRatio * 100) + " %"
//                + "\n" + "小铺率 : "
//                + "\n" + "动线长度 : " + String.format("%.2f", trafficLength) + " m";
//    }

    /* ------------- deprecated ------------- */

//    /**
//     * update traffic graph and buffer of current floor
//     *
//     * @param floorNum    current floor number
//     * @param dist        buffer distance
//     * @param curvePtsNum curve subdivision number
//     * @return void
//     */
//    public void generateGraphAndBuffer(int floorNum, double dist, int curvePtsNum) {
//        if (floorNum == 1) {
//            this.floors[floorNum - 1].updateGraph(innerNode_receive, entryNode_receive);
//        } else {
//            this.floors[floorNum - 1].updateGraph(innerNode_receive, new ArrayList<WB_Point>());
//        }
//        this.floors[floorNum - 1].updateBuffer(rawAtrium_receive, dist, curvePtsNum);
//
//        this.floors[floorNum - 1].disposeSplit();
//        this.floors[floorNum - 1].disposeSubdivision();
//        this.floors[floorNum - 1].disposeEvacuation();
//        this.floors[floorNum - 1].setStatus(1);
//    }
//
//    /**
//     * update block split and subdivision
//     *
//     * @param floorNum current floor number
//     * @return void
//     */
//    public void generateSubdivision(int floorNum) {
//        if (floorNum == 1) {
//            this.floors[floorNum - 1].updateSubdivision(bufferCurve_receive.get(0), grids);
//        } else {
//            this.floors[floorNum - 1].updateSubdivision(bufferCurve_receive.get(1), grids);
//        }
//
//        this.floors[floorNum - 1].disposeEvacuation();
//        this.floors[floorNum - 1].setStatus(2);
//    }
//
//    /**
//     * generate positions of evacuate stairways
//     *
//     * @param floorNum current floor number
//     * @return void
//     */
//    public void generateEvacuation(int floorNum) {
////        this.floors[floorNum - 1].updateEvacuation(cellPolys_receive.get(floorNum - 1));
//
//        this.floors[floorNum - 1].setStatus(3);
//    }
//
//

//    public void displayGraphLocal(int floorNum, PApplet app, WB_Render render) {
//        app.pushStyle();
//        app.stroke(255);
//        app.strokeWeight(1);
//        for (WB_Segment seg : getGraphSegments(floorNum)) {
//            render.drawSegment(seg);
//        }
//        app.popStyle();
//    }

//    public void displayPartitionLocal(int floorNum, PApplet app, JtsRender jtsRender) {
//        app.pushStyle();
//        app.strokeWeight(3);
//        app.stroke(255);
//        if (floors[floorNum - 1].getAllShops() != null) {
//            List<Shop> cells = floors[floorNum - 1].getAllShops();
//            for (Shop s : cells) {
//                jtsRender.drawGeometry(s.getShape());
//            }
//        }
////        if (floors[floorNum - 1].getAllSubLines() != null) {
////            for (LineString l : floors[floorNum - 1].getAllSubLines()) {
////                jtsRender.drawGeometry(l);
////            }
////        }
//
//        if (floors[floorNum - 1].getAllShops() != null) {
//            app.noStroke();
//            for (Shop s : floors[floorNum - 1].getAllShops()) {
//                s.display(app, jtsRender);
//            }
//
//            app.fill(255);
//            app.textSize(2);
//
//            for (Shop s : floors[floorNum - 1].getAllShops()) {
//                app.pushMatrix();
//                app.scale(1, -1);
//                app.translate(0, (float) (-2 * s.getCenter().getY()));
//                s.displayText(app);
//                app.popMatrix();
//            }
//        }
//        app.popStyle();
//    }

//
//    /**
//     * converting status 1 geometries to JsonElement
//     *
//     * @param floorNum current floor
//     * @param elements list of JsonElement
//     * @param gson     Gson
//     * @return void
//     */
//    private void convertingStatus1(int floorNum, List<JsonElement> elements, Gson gson) {
//        // preparing data
//        List<WB_Segment> graphSegments = getGraphSegments(floorNum);
//        List<List<WB_Coord>> controlPoints = getBufferControlPoints(floorNum);
//
//        // converting to json
//        for (WB_Segment seg : graphSegments) {
//            Segments segments = WB_Converter.toSegments(seg);
//            JsonObject prop1 = new JsonObject();
//            prop1.addProperty("name", "treeEdges");
//            segments.setProperties(prop1);
//            elements.add(gson.toJsonTree(segments));
//        }
//        for (List<WB_Coord> splitPointsEach : controlPoints) {
//            Vertices bufferControlPointsEach = WB_Converter.toVertices(splitPointsEach, 3);
//            JsonObject prop2 = new JsonObject();
//            prop2.addProperty("name", "bufferControl");
//            bufferControlPointsEach.setProperties(prop2);
//            elements.add(gson.toJsonTree(bufferControlPointsEach));
//        }
//    }
//
//    /**
//     * converting status 2 geometries to JsonElement
//     *
//     * @param floorNum current floor
//     * @param elements list of JsonElement
//     * @param gson     Gson
//     * @return void
//     */
//    private void convertingStatus2(int floorNum, List<JsonElement> elements, Gson gson) {
//        // preparing data
//        List<Shop> allShops = floors[floorNum - 1].getAllCells();
//        List<WB_Polygon> allCells = new ArrayList<>();
//        for (Shop s : allShops) {
//            allCells.add(s.getShapeWB());
//        }
//
//        // converting to json
//        for (WB_Polygon p : allCells) {
//            Segments cell = WB_Converter.toSegments(p);
//            JsonObject prop = new JsonObject();
//            prop.addProperty("name", "shopCell");
//
//            double area = Math.abs(p.getSignedArea());
//            if (area > 2000) {
//                prop.addProperty("shopType", "anchor");
//            } else if (area > 400 && area <= 2000) {
//                prop.addProperty("shopType", "subAnchor");
//            } else if (area > 80 && area <= 400) {
//                prop.addProperty("shopType", "ordinary");
//            } else {
//                prop.addProperty("shopType", "invalid");
//            }
//
//            cell.setProperties(prop);
//            elements.add(gson.toJsonTree(cell));
//        }
//    }
//
//    /**
//     * converting status 3 geometries to JsonElement
//     *
//     * @param floorNum current floor
//     * @param elements list of JsonElement
//     * @param gson     Gson
//     * @return void
//     */
//    private void convertingStatus3(int floorNum, List<JsonElement> elements, Gson gson) {
//        // preparing data
//        List<WB_Point> evacuationPoints = floors[floorNum - 1].getEvacuationPoint();
//
//        // converting to json
//        Vertices evacuation = WB_Converter.toVertices(evacuationPoints, 3);
//        JsonObject prop = new JsonObject();
//        prop.addProperty("name", "evacuation");
//        evacuation.setProperties(prop);
//        elements.add(gson.toJsonTree(evacuation));
//    }
//
//    /* ------------- JSON sending ------------- */
//
//    /**
//     * convert backend geometries to ArchiJSON
//     * graph segments, buffer control points
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONGraphAndBuffer(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        convertingStatus1(floorNum, elements, gson);
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
//
//    /**
//     * convert backend geometries to ArchiJSON
//     * first-level subdivision cells
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONSubdivision(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        convertingStatus2(floorNum, elements, gson);
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
//
//    /**
//     * convert backend geometries to ArchiJSON
//     * evacuation points & segments
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONEvacuation(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        convertingStatus3(floorNum, elements, gson);
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
//
//    /**
//     * switch floor num: convert all geometries in one floor
//     *
//     * @param floorNum current floor number
//     * @param clientID
//     * @param gson
//     * @return main.ArchiJSON
//     */
//    public ArchiJSON toArchiJSONFloor(int floorNum, String clientID, Gson gson) {
//        // initializing
//        ArchiJSON json = new ArchiJSON();
//        json.setId(clientID);
//        List<JsonElement> elements = new ArrayList<>();
//
//        // preparing data
//        int currentStatus = floors[floorNum - 1].getStatus();
//        int count = 1;
//        if (count <= currentStatus) {
//            convertingStatus1(floorNum, elements, gson);
//            count++; // ==2
//            if (count <= currentStatus) {
//                convertingStatus2(floorNum, elements, gson);
//                count++; // ==3
//                if (count <= currentStatus) {
//                    convertingStatus3(floorNum, elements, gson);
//                }
//            }
//        } else {
//            System.out.println("this floor hasn't been initialized due to some error");
//        }
//
//        // setup json
//        json.setGeometryElements(elements);
//        return json;
//    }
}