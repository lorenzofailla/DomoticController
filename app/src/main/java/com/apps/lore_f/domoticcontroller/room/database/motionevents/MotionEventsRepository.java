package com.apps.lore_f.domoticcontroller.room.database.motionevents;

import android.app.Application;
import android.os.AsyncTask;

import com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent;
import com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEventsDAO;
import com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEventsDatabase;

import java.util.List;

import androidx.lifecycle.LiveData;

public class MotionEventsRepository {

    private MotionEventsDAO motionEventsDao;
    private LiveData<List<MotionEvent>> allMotionEvents;

    public LiveData<Integer> countAll() {
        return motionEventsDao.countAll();
    }

    public LiveData<Integer> countAll(long v1, long v2) {
        return motionEventsDao.countAll(v1, v2);
    }

    public LiveData<Integer> countEvents(long v1) {
        return motionEventsDao.countEvents(v1);
    }


    MotionEventsRepository(Application application) {
        MotionEventsDatabase db = MotionEventsDatabase.getDatabase(application);
        motionEventsDao = db.motionEventsDAO();
        allMotionEvents = motionEventsDao.getAllEvents();

    }

    LiveData<List<MotionEvent>> getAllMotionEvents() {
        return allMotionEvents;
    }

    public void insert(MotionEvent event) {
        new insertAsyncTask(motionEventsDao).execute(event);
    }

    private static class insertAsyncTask extends AsyncTask<MotionEvent, Void, Void> {

        private MotionEventsDAO asyncTaskDAO;

        insertAsyncTask(MotionEventsDAO dao) {
            asyncTaskDAO = dao;
        }

        @Override
        protected Void doInBackground(final MotionEvent... params) {
            asyncTaskDAO.insert(params[0]);
            return null;
        }

    }

}