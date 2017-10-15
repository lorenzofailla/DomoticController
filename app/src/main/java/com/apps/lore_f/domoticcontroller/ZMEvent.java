package com.apps.lore_f.domoticcontroller;

/**
 * Created by lore_f on 08/10/2017.
 */

public class ZMEvent {

    private String Id;
    private String Name;
    private String Cause;
    private String MonitorId;
    private String StartTime;
    private String EndTime;

    // empty constructor
    public ZMEvent() {
    }

    public String getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public String getCause() {
        return Cause;
    }

    public String getMonitorId() {
        return MonitorId;
    }

    public String getStartTime() {
        return StartTime;
    }

    public String getEndTime() {
        return EndTime;
    }

}
