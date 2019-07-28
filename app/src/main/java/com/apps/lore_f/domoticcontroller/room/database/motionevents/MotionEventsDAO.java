package com.apps.lore_f.domoticcontroller.room.database.motionevents;

import com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface MotionEventsDAO {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(MotionEvent event);

    @Query("DELETE FROM motion_events")
    void deleteAll();

    @Query("SELECT * FROM motion_events WHERE :whereClause ORDER BY timestamp DESC")
    LiveData<List<MotionEvent>> getEventsList(String whereClause);

    @Query("UPDATE motion_events SET new=:value WHERE id=:id")
    void setField_NEW(String id, boolean value);

    @Query("SELECT COUNT(id) FROM motion_events")
    LiveData<Integer> countAll();

    @Query("SELECT COUNT(id) FROM motion_events WHERE timestamp > :value1")
    LiveData<Integer> countEvents(long value1);

    @Query("SELECT COUNT(id) FROM motion_events WHERE timestamp BETWEEN :value1 AND :value2")
    LiveData<Integer> countAll(long value1, long value2);



}
