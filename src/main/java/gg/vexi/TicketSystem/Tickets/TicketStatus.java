package gg.vexi.TicketSystem.Tickets;

import java.util.Map.Entry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import gg.vexi.TicketSystem.Enums.StatusEnum;

public class TicketStatus {
    private StatusEnum status;
    private JsonObject data;

    public TicketStatus(StatusEnum status) {
        this.status = status;
        this.data = new JsonObject();
    }

    public StatusEnum getStatus() { return status; }

    public void setStatus(StatusEnum status) { this.status = status; }

    public void setStatusData(String key, JsonElement value) { data.add(key, value); }

    public JsonElement getStatusData(String key) { return data.get(key); }

    public JsonObject getStatusDataMap() {
        JsonObject copy = new JsonObject();
        for (Entry<String, JsonElement> entry : data.entrySet()) {
            copy.add(entry.getKey(), entry.getValue());
        }
        return copy;
    }
}
