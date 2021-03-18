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

import java.net.URISyntaxException;
import java.util.ArrayList;

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
                String uri = "http://127.0.0.1:" + PORT;
                socket = IO.socket(uri);
                this.setupServer();
                System.out.println("Socket connected to " + uri);
            }

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /* ------------- member function ------------- */

    public void setupServer() {
        generator = new MallGenerator();
        generator.init();
        Gson gson = new Gson();

        socket.connect();

        socket.on("bts:receiveGeometry", args -> {
            // receive
            ArchiJSON archijson = gson.fromJson(args[0].toString(), ArchiJSON.class);
            archijson.parseGeometryElements(gson);
//            System.out.println(archijson);

            // converting
            System.out.println("geo num received -> " + archijson.getGeometries().size());
            generator.innerNode_receive = WB_Converter.toWB_Point((Vertices) archijson.getGeometries().get(0));
            generator.polyAtrium_receive = new ArrayList<>();
            for (int i = 1; i < archijson.getGeometries().size(); i++) {
                BaseGeometry g = archijson.getGeometries().get(i);
                if (g instanceof Segments) {
                    generator.polyAtrium_receive.add(WB_Converter.toWB_Polygon((Segments) g));
                } else if (g instanceof Plane) {
                    generator.polyAtrium_receive.add(WB_Converter.toWB_Polygon((Plane) g));
                }
            }

            // processing
            generator.generateGraph();
            generator.generateBuffer();

            // return
            ArchiJSON json = generator.toArchiJSON(archijson.getId(), gson);
            socket.emit("stb:sendGeometry", gson.toJson(json));
        });
    }
}