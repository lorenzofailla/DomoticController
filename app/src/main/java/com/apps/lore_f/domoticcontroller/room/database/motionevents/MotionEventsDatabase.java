package com.apps.lore_f.domoticcontroller.room.database.motionevents;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MotionEvent.class}, version = 1, exportSchema = false)
public abstract class MotionEventsDatabase extends RoomDatabase {

    public abstract MotionEventsDAO motionEventsDAO();

    private static volatile MotionEventsDatabase INSTANCE;

    public static MotionEventsDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MotionEventsDatabase.class) {
                if (INSTANCE == null) {
                    // Create database here
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            MotionEventsDatabase.class, "motion_events_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }


}

