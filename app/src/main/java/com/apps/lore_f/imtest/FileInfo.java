package com.apps.lore_f.imtest;

/**
 * Created by lore_f on 04/06/2017.
 */

public class FileInfo {

    private String fileName;
    private String fileRoorDir;
    private long fileSize;
    private FileInfoType fileInfoType;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileRoorDir() {
        return fileRoorDir;
    }

    public void setFileRoorDir(String fileRoorDir) {
        this.fileRoorDir = fileRoorDir;
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

                fileRoorDir = rootDirectory;

        String[] supportString = rawFileData.split(" ");

        fileName=supportString[8];
        fileSize = 0L;

        /* determina il tipo di file */
        if (supportString[0].charAt(0) == 'd'){
            fileInfoType = FileInfoType.TYPE_DIRECTORY;
        } else {
            fileInfoType = FileInfoType.TYPE_FILE;
        }

    }

}
