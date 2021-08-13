package demoTest;

import basicGeometry.ZLine;
import basicGeometry.ZPoint;
import com.google.gson.Gson;
import org.locationtech.jts.geom.LineString;

import java.io.*;
import java.util.Arrays;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project shopping_mall
 * @date 2021/6/17
 * @time 11:03
 */
public class TestJson {

    /* ------------- constructor ------------- */

    public TestJson() {

    }

    /* ------------- member function ------------- */

    public void runSave() {
        ZPoint p0 = new ZPoint(0, 100, 0);
        ZPoint p1 = new ZPoint(0, 200, 0);
        ZLine l = new ZLine(100, 100, 200, 200);
        LineString ls = l.toJtsLineString();

        Gson gson = new Gson();
        ZPoint[] ps = new ZPoint[]{p0, p1};
//        String s = gson.toJson(ps);
//        String s0 = gson.toJson(p0);
//        String s1 = gson.toJson(p0);
        String s2 = gson.toJson(l);
//        String s3 = gson.toJson(ls);

        saveDataToFile("./src/test/resources/test.json", s2);
    }

    public void runLoad() {
        String read = readJsonFile("./src/test/resources/test.json");
        System.out.println(read);

        Gson gson = new Gson();
        ZPoint[] p0 = gson.fromJson(read, ZPoint[].class);
        System.out.println(Arrays.toString(p0));
    }

    /**
     * description
     *
     * @param filePath
     * @param data
     * @return void
     */
    private void saveDataToFile(String filePath, String data) {
        BufferedWriter writer = null;
        File file = new File(filePath);
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

    private String getDatafromFile(String filePath) {
        BufferedReader reader = null;
        String laststr = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                laststr += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return laststr;
    }

    public String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* ------------- setter & getter ------------- */



    /* ------------- draw ------------- */
}
