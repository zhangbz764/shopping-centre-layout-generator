package webMain;

import com.google.gson.Gson;
import converter.WB_Converter;
import geometry.BaseGeometry;
import geometry.Plane;
import geometry.Segments;
import geometry.Vertices;
import io.socket.client.IO;
import io.socket.client.Socket;
import main.ArchiJSON;
import wblut.geom.WB_Polygon;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project archijson
 * @date 2021/3/10
 * @time 17:49
 */
public class MallServer {
    private static final int PORT = 27781;
    private Socket socket;
    public MallGenerator generator;

    /* ------------- constructor ------------- */

    public MallServer(String... args) {
        try {
            if (args.length > 0) {
                socket = IO.socket(args[0]);
                this.setupServer();
                System.out.println("Socket connected to " + args[0]);
            } else {
                String url = "http://127.0.0.1:" + PORT;
                socket = IO.socket(url);
                this.setupServer();
                System.out.println("Socket connected to " + url);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /* ------------- member function ------------- */

    /**
    * receiving, converting, processing and returning data
    *
    * @return void
    */
    public void setupServer() {
        Gson gson = new Gson();

        generator = new MallGenerator();
        generator.init();

        socket.connect();

        socket.on("ftb:receiveBuffer", args ->{
            System.out.println("receiving");
            // receiving
            ArchiJSON archijson = gson.fromJson(args[0].toString(), ArchiJSON.class);
            archijson.parseGeometryElements(gson);


            // return
            ArchiJSON json = generator.toArchiJSON2(archijson.getId(), gson);
            socket.emit("btf:sendPartition", gson.toJson(json));
        });

        socket.on("bts:receiveGeometry", args -> {
            System.out.println("receiving");
            // receiving
            ArchiJSON archijson = gson.fromJson(args[0].toString(), ArchiJSON.class);
            archijson.parseGeometryElements(gson);

            // converting
            int atriumNum = archijson.getProperties().getAtriumNum(); // 中庭图元个数
            double bufferDist = archijson.getProperties().getBufferDist(); // 偏移距离

            System.out.println(atriumNum);
            generator.setInnerNode_receive(
                    WB_Converter.toWB_Point((Vertices) archijson.getGeometries().get(0))
            );
            List<WB_Polygon> polyAtrium_receive = new ArrayList<>();
            for (int i = 1; i < atriumNum + 1; i++) {
                BaseGeometry g = archijson.getGeometries().get(i);
                if (g instanceof Segments) {
                    polyAtrium_receive.add(WB_Converter.toWB_Polygon((Segments) g));
                } else if (g instanceof Plane) {
                    polyAtrium_receive.add(WB_Converter.toWB_Polygon((Plane) g));
                }
            }
            generator.setPolyAtrium_receive(polyAtrium_receive);

            // processing
            generator.generateGraph();
            generator.generateBuffer(bufferDist);

            // return
            ArchiJSON json = generator.toArchiJSON(archijson.getId(), gson);
            socket.emit("stb:sendGeometry", gson.toJson(json));
        });

    }
}