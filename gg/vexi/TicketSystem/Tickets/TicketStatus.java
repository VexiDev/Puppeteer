package gg.vexi.TicketSystem.Tickets;

import java.util.*;

import gg.vexi.TicketSystem.Enums.StatusEnum;

public class TicketStatus {
    private StatusEnum status;
    private Map<String, Object> data;

    public TicketStatus(StatusEnum status) {
        this.status = status;
        this.data = new HashMap<>();
    }

    public StatusEnum getStatus() { return status; }
    public void setStatus(StatusEnum status) { this.status = status; }
    public void setStatusData(String key, Object value) { data.put(key, value); }
    public Object getStatusData(String key) { return data.get(key); }
    public Map<String, Object> getStatusDataMap() { return new HashMap<>(data); }
}