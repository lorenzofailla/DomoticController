package com.apps.lore_f.domoticcontroller.room.database.motionevents;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "motion_events")
public class MotionEvent {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "timestamp")
    private long timeStamp;
    @ColumnInfo(name = "picture_filename")
    private String pictureFileName;
    @ColumnInfo(name = "video_filename")
    private String videoFileName;
    @ColumnInfo(name = "camera_name")
    private String cameraName;
    @ColumnInfo(name = "camera_full_id")
    private String cameraFullID;
    @ColumnInfo(name = "locked")
    private boolean locked;
    @ColumnInfo(name = "new")
    private boolean newItem;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getPictureFileName() {
        return pictureFileName;
    }

    public void setPictureFileName(String pictureFileName) {
        this.pictureFileName = pictureFileName;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public void setVideoFileName(String videoFileName) {
        this.videoFileName = videoFileName;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraFullID() {
        return cameraFullID;
    }

    public void setCameraFullID(String cameraFullID) {
        this.cameraFullID = cameraFullID;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean canBeDeleted) {
        this.locked = canBeDeleted;
    }

    public boolean isNewItem() {
        return newItem;
    }

    public void setNewItem(boolean newItem) {
        this.newItem = newItem;
    }
}
