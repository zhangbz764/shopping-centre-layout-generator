package main;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * description
 *
 * @author zhangbz ZHANG Baizhou
 * @project shopping_mall
 * @date 2021/8/5
 * @time 15:41
 */
public class InterfaceAdapter implements JsonSerializer, JsonDeserializer {

    private static final String CLASSNAME = "CLASSNAME";
    private static final String DATA = "DATA";

    /**
     * Helper method to get the className of the object to be deserialized
     *
     * @param className
     * @return java.lang.Class
     */
    public Class getObjectClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            throw new JsonParseException(e.getMessage());
        }
    }

    @Override
    public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        String className = prim.getAsString();
        Class klass = getObjectClass(className);
        return jsonDeserializationContext.deserialize(jsonObject.get(DATA), klass);
    }

    @Override
    public JsonElement serialize(Object o, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(CLASSNAME, o.getClass().getName());
        jsonObject.add(DATA, jsonSerializationContext.serialize(o));
        return jsonObject;
    }
}
