package com.apps.lore_f.domoticcontroller.generic.dataobjects;

/**
 * Created by 105053228 on 30/mar/2017.
 */

public class TorrentInfo {

    private String IDString;
    public String getIDString() {
        return IDString;
    }
    public void setIDString(String ID) {
        this.IDString = ID;
    }

    private String statusString;
    public String getStatusString() {
        return statusString;
    }
    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TorrentStatus getStatus() {
        return status;
    }

    public void setStatus(TorrentStatus status) {
        this.status= status;
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

        IDString=rawServerResponseLine.substring(0,4);
        ID=Integer.parseInt(IDString.replace(" ",""));

        name=rawServerResponseLine.substring(70,rawServerResponseLine.length()-1);
        statusString=rawServerResponseLine.substring(57,70);
        eta=  rawServerResponseLine.substring(24,34);
        done=rawServerResponseLine.substring(7,11);
        have=rawServerResponseLine.substring(11,22);

        if (statusString.replace(" ", "").equals("Stopped")){

            status = TorrentStatus.STOPPED;

        } else {

            status = TorrentStatus.NOT_STOPPED;
        }

    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }


    private int ID;
    private String name;

    private TorrentStatus status;
    private String eta;
    private String done;
    private String have;

    public static enum TorrentStatus{

        STOPPED,NOT_STOPPED;

    }

}
