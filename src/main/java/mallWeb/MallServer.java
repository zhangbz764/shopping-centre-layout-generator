package mallWeb;

import archijson.ArchiJSON;
import archijson.ArchiServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.socket.client.Socket;
import mallParameters.MallConst;

/**
 * description
 *
 * @author ZHANG Bai-zhou zhangbz
 * @project archijson
 * @date 2021/3/10
 * @time 17:49
 */
public class MallServer implements ArchiServer {
    private MallJsonProcessor processor;

    /* ------------- constructor ------------- */

    public MallServer() {
        String URL = "https://web.archialgo.com";
        String TOKEN = "bc5726c6-96ff-40e5-8459-353453a05caf";
        String IDENTITY = "mall-java-backend";

        ArchiServer.super.setup(URL, TOKEN, IDENTITY);

        this.processor = new MallJsonProcessor();

        System.out.println(">>> server initialized");
    }

    @Override
    public void onConnect(Socket socket) {
        System.out.println("connected");
    }

    @Override
    public void onReceive(Socket socket, String id, JsonObject body) {
        ArchiServer.super.onReceive(socket, id, body);

        Gson gson = new Gson();

        // archijson received
        ArchiJSON jsonR = gson.fromJson(body, ArchiJSON.class);
        jsonR.parseGeometryElements(gson);
        int statusID = jsonR.getProperties().get("statusID").getAsInt();
        int functionID = jsonR.getProperties().get("functionID").getAsInt();
        System.out.println("statusID: " + statusID + "  " + "functionID: " + functionID);

        // archijson to send
        ArchiJSON jsonS = new ArchiJSON();
        JsonObject properties = new JsonObject();
        properties.addProperty("statusID", statusID);
        properties.addProperty("functionID", functionID);

        switch (statusID) {
            case MallConst.E_SITE_BOUNDARY:
                processor.processStatus0(functionID, jsonR, jsonS, properties);
                break;
            case MallConst.E_TRAFFIC_ATRIUM:
                processor.processStatus1(functionID, jsonR, jsonS, properties);
                break;
            case MallConst.E_STRUCTURE_GRID:
                processor.processStatus4(functionID, jsonR, jsonS, properties);
                break;
        }

        ArchiServer.super.send(socket, "client", id, gson.toJson(jsonS));
    }
}