package com.apps.lore_f.domoticcontroller;

/**
 * Created by 105053228 on 30/mar/2017.
 */

public class TorrentInfo {

    public String getID() {
        return IDString;
    }

    public void setID(String ID) {
        this.IDString = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return statusString;
    }

    public void setStatus(String status) {
        this.statusString = status;
    }

    public String getEta() {
        return eta;
    }

    public void setEta(String eta) {
        this.eta = eta;
    }

    public String getDone() {
        return done;
    }

    public void setDone(String done) {
        this.done = done;
    }

    public String getHave() {
        return have;
    }

    public void setHave(String have) {
        this.have = have;
    }

    public TorrentInfo(String rawServerResponseLine){

        IDString=rawServerResponseLine.substring(0,6);
        ID=Integer.parseInt(IDString);

        name=rawServerResponseLine.substring(70,rawServerResponseLine.length()-1);
        statusString=rawServerResponseLine.substring(57,70);
        eta=  rawServerResponseLine.substring(24,34);
        done=rawServerResponseLine.substring(7,11);
        have=rawServerResponseLine.substring(11,22);

    }

    private String IDString;
    private int ID;
    private String name;
    private String statusString;
    private TorrentStatus status;
    private String eta;
    private String done;
    private String have;

    public static enum TorrentStatus{

        STOPPED,STARTED;

    }

}
