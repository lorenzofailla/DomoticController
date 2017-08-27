package com.apps.lore_f.domoticcontroller;

/**
 * Created by lore_f on 26/08/2017.
 */

public class FileInCloudStorage {

    private String fileName;
    private String requestor;
    private String mediaLink;
    private long size;
    private int nOfDownloads;

    // empty constructor
    public FileInCloudStorage() {
    }

    public FileInCloudStorage(String fileName, String requestor, String mediaLink, long size, int nOfDownloads) {
        this.fileName = fileName;
        this.requestor = requestor;
        this.mediaLink = mediaLink;
        this.size = size;
        this.nOfDownloads = nOfDownloads;
    }

    public String getFileName() {
        return fileName;
    }

    public String getRequestor() {
        return requestor;
    }

    public String getMediaLink() {
        return mediaLink;
    }

    public long getSize() {
        return size;
    }

    public int getnOfDownloads() {
        return nOfDownloads;
    }

}


