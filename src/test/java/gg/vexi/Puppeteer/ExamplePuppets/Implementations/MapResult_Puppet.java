package gg.vexi.Puppeteer.ExamplePuppets.Implementations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gg.vexi.Puppeteer.ResultStatus;
import gg.vexi.Puppeteer.Core.Puppet;
import gg.vexi.Puppeteer.Core.Ticket;

public class MapResult_Puppet extends Puppet {

    private Map<String, Integer> data;

    public MapResult_Puppet(Ticket ticket) {
        super(ticket);
    }

    @Override
    public void main() {

        try {
            Thread.sleep(200);
            data = new ConcurrentHashMap<>();
                data.put("exampleProperty", 1987);
            super.complete(ResultStatus.SUCCESS, data);
        } catch (InterruptedException e) {
            super.complete(ResultStatus.ERROR_FAILED, null); 
        }
    }
}
