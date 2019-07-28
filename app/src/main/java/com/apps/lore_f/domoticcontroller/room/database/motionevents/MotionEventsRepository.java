package com.apps.lore_f.domoticcontroller.room.database.motionevents;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.sqlite.db.SupportSQLiteQuery;

public class MotionEventsRepository {

    private MotionEventsDAO motionEventsDao;

    public LiveData<Integer> countAll() {
        return motionEventsDao.countAll();
    }

    public LiveData<Integer> countAll(long v1, long v2) {
        return motionEventsDao.countAll(v1, v2);
    }

    public LiveData<Integer> countEvents(long v1) {
        return motionEventsDao.countEvents(v1);
    }

    LiveData<List<MotionEvent>> getEventsList(SupportSQLiteQuery query) {
        return motionEventsDao.getEventsList(query);
    }

    MotionEventsRepository(Application application) {
        MotionEventsDatabase db = MotionEventsDatabase.getDatabase(application);
        motionEventsDao = db.motionEventsDAO();

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