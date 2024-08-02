package gg.vexi.TicketSystem.Exceptions;

import java.util.ArrayList;

import com.google.gson.JsonObject;

public class CaughtExceptions {

    ArrayList<Error> Errors = new ArrayList<>();

    public void add() {}

    public boolean any() { return !Errors.isEmpty(); }

    public ArrayList<Error> getAll() {}

    public JsonObject toJson() {}
}