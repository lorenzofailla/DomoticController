package com.apps.lore_f.domoticcontroller;

/**
 * Created by lore_f on 04/06/2017.
 */

public class FileInfo {

    private String fileName;
    private String fileRootDir;
    private long fileSize;
    private FileInfoType fileInfoType;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileRootDir() {
        return fileRootDir;
    }

    public void setFileRootDir(String fileRoorDir) {
        this.fileRootDir = fileRoorDir;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileSizeString() {
        return String.format("%n", fileSize);
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public FileInfoType getFileInfoType() {
        return fileInfoType;
    }

    public void setFileInfoType(FileInfoType fileInfoType) {
        this.fileInfoType = fileInfoType;
    }

    public enum FileInfoType{

        TYPE_FILE,
        TYPE_DIRECTORY
    }

    public FileInfo(String rootDirectory, String rawFileData){

                fileRootDir = rootDirectory;

        fileSize = 0L;

        String[] supportString = rawFileData.split(" +");

        fileName=supportString[8];

        /* determina il tipo di file */
        if (supportString[0].charAt(0) == 'd'){
            fileInfoType = FileInfoType.TYPE_DIRECTORY;
        } else {
            fileInfoType = FileInfoType.TYPE_FILE;
        }

    }

}
