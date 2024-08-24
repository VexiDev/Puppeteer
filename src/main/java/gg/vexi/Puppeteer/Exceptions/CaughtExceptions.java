package gg.vexi.Puppeteer.Exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class CaughtExceptions {

    private final ArrayList<ExceptionRecord> Exceptions = new ArrayList<>();



    public void add(ExceptionRecord error) {
        Exceptions.add(error);
    }

    public boolean any() {
        return !Exceptions.isEmpty();
    }

    public ArrayList<ExceptionRecord> getAll() {
        return Exceptions;
    }

    public JsonObject getAsJson() {

        ConcurrentHashMap<String, List<ExceptionRecord>> groupedErrors = new ConcurrentHashMap<>();
        for (ExceptionRecord error : Exceptions) {
            groupedErrors.computeIfAbsent(error.getType(), k -> new ArrayList<>()).add(error);
        }

        JsonObject result = new JsonObject();
        for (ConcurrentHashMap.Entry<String, List<ExceptionRecord>> entry : groupedErrors.entrySet()) {
            String type = entry.getKey();
            List<ExceptionRecord> errorList = entry.getValue();

            JsonArray jsonArray = new JsonArray();
            for (ExceptionRecord error : errorList) {
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
