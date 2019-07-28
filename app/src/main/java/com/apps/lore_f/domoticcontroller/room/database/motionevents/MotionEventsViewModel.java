package com.apps.lore_f.domoticcontroller.room.database.motionevents;

import android.app.Application;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class MotionEventsViewModel extends AndroidViewModel {

    private MotionEventsRepository repository;

    public MotionEventsViewModel (Application application) {
        super(application);
        repository = new MotionEventsRepository(application);

    }

    public LiveData<List<MotionEvent>> getEventsList(String whereClause) { return repository.getEventsList(whereClause); }

    public void insert(MotionEvent event) { repository.insert(event); }

    public LiveData<Integer> countAll() {
        return repository.countAll();
    }

    public LiveData<Integer> countAll(long v1, long v2) {
        return repository.countAll(v1, v2);
    }

    public LiveData<Integer> countEvents(long v1) {
        return repository.countEvents(v1);
    }

}