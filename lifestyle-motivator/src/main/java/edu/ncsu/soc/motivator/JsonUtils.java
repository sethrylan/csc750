package edu.ncsu.soc.motivator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class JsonUtils {

    /**
     * Generic method to create any DomainObject from a JSON string using GSON
     * @param domainClass is the Class type for the object to be created
     * @param rawData is the serialized JSON string
     * @return a newly created and populated DomainObject
     * @throws DomainObjectFactoryException
     */
    public static <T> T createFromJson(Class<T> domainClass, String rawJson) {
        // Return null if no string is specified
        if (rawJson == null || rawJson.trim().length() == 0) {
            return null;
        }

        // Convert the JSON to a domain object
        try {
            Gson gson = new Gson();
            return gson.fromJson(rawJson, domainClass);
        } catch (JsonSyntaxException jse) {
            jse.printStackTrace();
            return null;
        }
    }
    
    /**
     * Pretty-ify json
     * @param json   raw JSON
     * @return      Pretty JSON
     */
    public static String prettyPrint(String json) {
        return new GsonBuilder().setPrettyPrinting().create().toJson(new JsonParser().parse(json));
    }
}
