package main;

import basicGeometry.ZPoint;
import com.google.gson.Gson;
import wblut.geom.WB_Coord;
import wblut.geom.WB_Point;
import wblut.geom.WB_Polygon;

import java.io.*;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/6/9
 * @time 11:46
 */
public class TemporarySave {

    /* ------------- constructor ------------- */

    public TemporarySave() {
        Gson gson = new Gson();

        ZPoint zp = new ZPoint(253, 577);

        WB_Coord[] pts = new WB_Coord[6];
        pts[0] = new WB_Point(100, 100);
        pts[1] = new WB_Point(700, 100);
        pts[2] = new WB_Point(800, 500);
        pts[3] = new WB_Point(500, 800);
        pts[4] = new WB_Point(100, 700);
        pts[5] = new WB_Point(100, 100);
        WB_Polygon polygon = new WB_Polygon(pts);

        String s1 = gson.toJson(zp);
        String s2 = gson.toJson(polygon);


    }

    /* ------------- member function ------------- */

    private void saveDataToFile(String fileName, String data) {
        BufferedWriter writer = null;
        File file = new File("d:\\" + fileName + ".json");
        //如果文件不存在，则新建一个
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
            writer.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("文件写入成功！");
    }


    /* ------------- setter & getter ------------- */



    /* ------------- draw ------------- */
}
