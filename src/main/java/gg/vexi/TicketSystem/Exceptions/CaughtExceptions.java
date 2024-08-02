package gg.vexi.TicketSystem.Exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CaughtExceptions {

    private final ArrayList<Anomaly> Errors = new ArrayList<>();

    public void add(Anomaly e) {
        Errors.add(e);
    }

    public boolean any() {
        return !Errors.isEmpty();
    }

    public ArrayList<Anomaly> getAll() {
        ArrayList<Anomaly> allErrors = new ArrayList<>();
        for (Anomaly error : Errors) {
            allErrors.add(error);
        }
        return allErrors;
    }

    public JsonObject toJson() {

        ConcurrentHashMap<String, List<Anomaly>> groupedErrors = new ConcurrentHashMap<>();
        for (Anomaly error : Errors) {
            groupedErrors.computeIfAbsent(error.getType(), k -> new ArrayList<>()).add(error);
        }

        JsonObject result = new JsonObject();
        for (ConcurrentHashMap.Entry<String, List<Anomaly>> entry : groupedErrors.entrySet()) {
            String type = entry.getKey();
            List<Anomaly> errorList = entry.getValue();

            JsonArray jsonArray = new JsonArray();
            for (Anomaly error : errorList) {
                JsonObject errorJson = new JsonObject();
                errorJson.add("id", new JsonPrimitive(error.getId()));
                errorJson.add("message", new JsonPrimitive(error.getMessage()));
                jsonArray.add(errorJson);
            }

            result.add(type, jsonArray);
        }

        return result;
    }
}
