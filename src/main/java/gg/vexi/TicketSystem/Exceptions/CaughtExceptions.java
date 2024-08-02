package gg.vexi.TicketSystem.Exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import gg.vexi.TicketSystem.Exceptions.Error;

public class CaughtExceptions {

    private final ArrayList<Error> Errors = new ArrayList<>();

    public void add() {}

    public boolean any() { return !Errors.isEmpty(); }

    public ArrayList<Error> getAll() {
        ArrayList<Error> allErrors = new ArrayList<>();
        for (Error error : Errors) { allErrors.add(error); }
        return allErrors;
    }

    public JsonObject toJson() {

        ConcurrentHashMap<String, List<Error>> groupedErrors = new ConcurrentHashMap<>();
        for (Error error : Errors) {
            groupedErrors.computeIfAbsent(error.getType(), k -> new ArrayList<>()).add(error);
        }

        JsonObject result = new JsonObject();
        for (ConcurrentHashMap.Entry<String, List<Error>> entry : groupedErrors.entrySet()) {
            String type = entry.getKey();
            List<Error> errorList = entry.getValue();

            JsonArray jsonArray = new JsonArray();
            for (Error error : errorList) {
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