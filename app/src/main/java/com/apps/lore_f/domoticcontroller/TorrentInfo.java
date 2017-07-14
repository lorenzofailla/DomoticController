package com.apps.lore_f.domoticcontroller;

/**
 * Created by 105053228 on 30/mar/2017.
 */

public class TorrentInfo {

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

        ID=rawServerResponseLine.substring(0,6);
        name=rawServerResponseLine.substring(70,rawServerResponseLine.length()-1);
        status=rawServerResponseLine.substring(57,69);
        eta=  rawServerResponseLine.substring(24,33);
        done=rawServerResponseLine.substring(7,10);
        have=rawServerResponseLine.substring(11,21);
    }

    private String ID;
    private String name;
    private String status;
    private String eta;
    private String done;
    private String have;


}
