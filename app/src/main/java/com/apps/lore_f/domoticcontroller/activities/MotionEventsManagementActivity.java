package com.apps.lore_f.domoticcontroller.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.apps.lore_f.domoticcontroller.firebase.dataobjects.MotionEvent;
import com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEventsDAO;
import com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEventsDatabase;
import com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEventsViewModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static apps.android.loref.GeneralUtilitiesLibrary.getTimeElapsedToday;

import com.apps.lore_f.domoticcontroller.R;

public class MotionEventsManagementActivity extends AppCompatActivity {

    private enum timeSpanDef {
        HOUR,
        DAY,
        WEEK,
        MONTH
    }

    private boolean filterByNew = false;
    private boolean filterByCameraName = false;
    private boolean filterByLocked = false;
    private boolean filterByTimestamp = false;

    private String filterByNewVALUE;
    private String filterByCameraNameVALUE;
    private String filterByLockedVALUE;
    private String filterByTimestampVALUE;

    private MotionEventsDatabase localDatabase;
    private MotionEventsViewModel motionEventsViewModel;

    private final static String TAG = MotionEventsManagementActivity.class.getName();

    private final static int BG_COLOR_SELECTED = Color.argb(32, 0, 0, 127);
    private final static long MAX_THUMBNAIL_DOWNLOAD_SIZE = 4194304;
    private final static String VIDEO_SUBDIR = "MotionEventVideos";
    private final static String THUMB_SUBDIR = "Thumbnails";

    private RecyclerView eventsRecyclerView;
    private File downloadDirectoryRoot;
    private String groupName;

    private static Context context;

    private boolean showEventsResume = false;
    private boolean showFiltersPanel = false;

    private ChildEventListener firebaseMotionEventsManager = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            MotionEvent event = dataSnapshot.getValue(MotionEvent.class);
            Log.d(TAG, "Motion event found. Key=" + dataSnapshot.getKey());

            com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent localEvent = new com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent();
            localEvent.setCameraFullID(event.getCameraFullID());
            localEvent.setCameraName(event.getCameraName());
            localEvent.setId(dataSnapshot.getKey());
            localEvent.setPictureFileName(event.getThumbnailID());
            localEvent.setVideoFileName(event.getVideoID());
            localEvent.setTimeStamp(Long.parseLong(event.getTimestamp()));
            localEvent.setLocked(false);
            localEvent.setNewItem(true);

            new PopulateDbAsync(localDatabase).execute(localEvent);

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }

    };

    private static class PopulateDbAsync extends AsyncTask<com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent, Void, Void> {

        private final MotionEventsDAO motionEventsDAO;

        PopulateDbAsync(MotionEventsDatabase database) {

            motionEventsDAO = database.motionEventsDAO();

        }

        @Override
        protected Void doInBackground(final com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent... params) {

            motionEventsDAO.insert(params[0]);
            return null;

        }

    }

    private static class MarkEventAsRead extends AsyncTask<String, Void, Void> {

        private final MotionEventsDAO motionEventsDAO;

        MarkEventAsRead(MotionEventsDatabase database) {

            motionEventsDAO = database.motionEventsDAO();

        }

        @Override
        protected Void doInBackground(final String... params) {

            motionEventsDAO.setField_NEW(params[0], false);
            return null;

        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // recupera il nome del gruppo [R.string.data_group_name] dalle shared preferences
        context = getApplicationContext();
        SharedPreferences sharedPref = context.getSharedPreferences(
                getString(R.string.data_file_key), Context.MODE_PRIVATE);

        groupName = sharedPref.getString(getString(R.string.data_group_name), null);

        if (groupName == null) { // should never happen

            // nome del gruppo non impostato, lancia l'Activity GroupSelection per selezionare il gruppo a cui connettersi
            startActivity(new Intent(this, GroupSelection.class));

            // termina l'Activity corrente
            finish();
            return;

        }

        // visualizza il layout
        setContentView(R.layout.activity_motioneventsmanagement);

        // set up the toolbar of this activity
        Toolbar toolbar = findViewById(R.id.TBR___MOTIONEVENTS_MANAGEMENT___TOOLBAR);
        toolbar.inflateMenu(R.menu.motioneventsmanagement_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.MENU_MOTIONEVENTMANAGEMENTS_Filter:

                        showFiltersPanel = !showFiltersPanel;

                        if (showFiltersPanel)
                            showEventsResume = false;

                        configureView();
                        return true;

                    case R.id.MENU_MOTIONEVENTMANAGEMENTS_ShowData:

                        showEventsResume = !showEventsResume;

                        if (showEventsResume)
                            showFiltersPanel = false;

                        configureView();

                        return true;

                    default:
                        return false;

                }
            }

        });

    }

    private void configureView() {

        int eventsDataResumeVisibility = GONE;
        if (showEventsResume)
            eventsDataResumeVisibility = VISIBLE;

        int filterPanelVisibility = GONE;
        if (showFiltersPanel)
            filterPanelVisibility = VISIBLE;

        RelativeLayout eventsDataResumeRLA = findViewById(R.id.RLA___MOTIONEVENTS_MANAGEMENT___DATA);
        eventsDataResumeRLA.setVisibility(eventsDataResumeVisibility);

        RelativeLayout filterPanelRLA = findViewById(R.id.RLA___MOTIONEVENTS_MANAGEMENT___FILTERS);
        filterPanelRLA.setVisibility(filterPanelVisibility);

    }

    @Override
    protected void onResume() {

        // calls the onResume() method on the superclass
        super.onResume();

        // configura la visualizzazione
        configureView();

        // inizializza il riferimento alla directory dove i file dei video saranno scaricati
        downloadDirectoryRoot = new File(Environment.getExternalStorageDirectory(), "Domotic");

        // crea la directory principale se non esiste
        if (!downloadDirectoryRoot.exists()) downloadDirectoryRoot.mkdir();

        // crea le sottodirectory se non esistono
        File videoDir = new File(downloadDirectoryRoot, VIDEO_SUBDIR);
        if (!videoDir.exists()) videoDir.mkdir();

        File thumbnailDir = new File(downloadDirectoryRoot, THUMB_SUBDIR);
        if (!thumbnailDir.exists()) thumbnailDir.mkdir();

        motionEventsViewModel = ViewModelProviders.of(this).get(MotionEventsViewModel.class);

        updateEvents();

        motionEventsViewModel.countEvents(getTimeDefinition(timeSpanDef.HOUR)).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

                TextView eventsCount = findViewById(R.id.TXV___MOTIONEVENTS_MANAGEMENT___EVENTSHOUR_VALUE);
                eventsCount.setText(String.format("%d", integer));

            }

        });

        motionEventsViewModel.countEvents(getTimeDefinition(timeSpanDef.DAY)).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

                TextView eventsCount = findViewById(R.id.TXV___MOTIONEVENTS_MANAGEMENT___EVENTSTODAY_VALUE);
                eventsCount.setText(String.format("%d", integer));

            }

        });

        motionEventsViewModel.countEvents(getTimeDefinition(timeSpanDef.WEEK)).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

                TextView eventsCount = findViewById(R.id.TXV___MOTIONEVENTS_MANAGEMENT___EVENTSWEEK_VALUE);
                eventsCount.setText(String.format("%d", integer));

            }

        });

        motionEventsViewModel.countEvents(getTimeDefinition(timeSpanDef.MONTH)).observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

                TextView eventsCount = findViewById(R.id.TXV___MOTIONEVENTS_MANAGEMENT___EVENTSMONTH_VALUE);
                eventsCount.setText(String.format("%d", integer));

            }

        });

        motionEventsViewModel.countAll().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

                TextView eventsCount = findViewById(R.id.TXV___MOTIONEVENTS_MANAGEMENT___EVENTSTOTAL_VALUE);
                eventsCount.setText(String.format("%d", integer));

            }

        });

        // ottiene un riferimento all'istanza del database locale
        localDatabase = MotionEventsDatabase.getDatabase(this);

        // ottiene un riferimento al nodo principale degli eventi, e assegna un ChildEventListener per aggiungere gli eventi al database locale
        FirebaseDatabase.getInstance().getReference(String.format("MotionEvents/%s", groupName)).addChildEventListener(firebaseMotionEventsManager);

    }

    @Override
    public void onPause() {

        super.onPause();

        // ottiene un riferimento al nodo principale degli eventi, e rimuove il ChildEventListener per aggiungere gli eventi al database locale
        FirebaseDatabase.getInstance().getReference(String.format("MotionEvents/%s", groupName)).removeEventListener(firebaseMotionEventsManager);

    }

    private String getSQLWhereClause() {

        String result = "1=1";

        if (filterByNew) {
            result += String.format(" AND new=%s", filterByNewVALUE);
        }

        if (filterByTimestamp) {
            result += String.format(" AND timestamp>%s", filterByTimestampVALUE);
        }

        Log.d(TAG, result);

        return result;

    }

    private long getTimeDefinition(timeSpanDef def) {

        if (def == timeSpanDef.HOUR) {

            return System.currentTimeMillis() - 3600000L;

        } else {

            Calendar calendar;

            calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            switch (def) {
                case DAY:
                    return calendar.getTimeInMillis();
                case WEEK:
                    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    return calendar.getTimeInMillis();
                case MONTH:
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    return calendar.getTimeInMillis();
                default:
                    return System.currentTimeMillis();
            }

        }

    }

    private void updateEvents() {

        eventsRecyclerView = findViewById(R.id.RWV___MOTIONEVENTS_MANAGEMENT___EVENTS);
        final MotionEventsListAdapter adapter = new MotionEventsListAdapter(this);
        eventsRecyclerView.setAdapter(adapter);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        motionEventsViewModel.getEventsList(getSQLWhereClause()).observe(this, new Observer<List<com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent>>() {
            @Override
            public void onChanged(@Nullable final List<com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent> events) {
                // Update the cached copy of the words in the adapter.
                adapter.setWords(events);
            }
        });

    }

    public void manageMotionEventsFilterRadioButtonClick(View v) {

        // Is the button now checked?
        boolean checked = ((RadioButton) v).isChecked();

        if (checked) {

            // Check which radio button was clicked
            switch (v.getId()) {
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_STATUS_ANY:
                    filterByNew = false;
                    filterByNewVALUE = "";
                    break;
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_STATUS_NEW:
                    filterByNew = true;
                    filterByNewVALUE = "true";
                    break;
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_STATUS_NOTNEW:
                    filterByNew = true;
                    filterByNewVALUE = "false";
                    break;
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_TIME_ANY:
                    filterByTimestamp = false;
                    filterByTimestampVALUE = "";
                    break;
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_TIME_LASTHOUR:
                    filterByTimestamp = true;
                    filterByTimestampVALUE = "" + getTimeDefinition(timeSpanDef.HOUR);
                    break;
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_TIME_TODAY:
                    filterByTimestamp = true;
                    filterByTimestampVALUE = "" + getTimeDefinition(timeSpanDef.DAY);
                    break;
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_TIME_THISWEEK:
                    filterByTimestamp = true;
                    filterByTimestampVALUE = "" + getTimeDefinition(timeSpanDef.WEEK);
                    break;
                case R.id.RBT___MOTIONEVENTSMANAGEMENT_FILTERENTRY_TIME_THISMONTH:
                    filterByTimestamp = true;
                    filterByTimestampVALUE = "" + getTimeDefinition(timeSpanDef.MONTH);
                    break;

                default:
                    filterByTimestamp = false;
                    filterByNew = false;

            }

            updateEvents();

        }

    }

    public class MotionEventsListAdapter extends RecyclerView.Adapter<MotionEventsListAdapter.MotionEventHolder> {

        public class MotionEventHolder extends RecyclerView.ViewHolder {

            private TextView eventDateTextView;
            private TextView eventMonitorNameTextView;

            private ProgressBar progressBar;
            private TextView eventCameraNameTextView;

            private ImageView eventPreviewImage;

            private ImageView newItemImage;
            private ImageView lockedItemImage;
            private ImageView eventLocationImage;

            private ConstraintLayout eventLabels;
            private ConstraintLayout eventOptions;

            private ConstraintLayout eventContainer;

            private boolean optionsVisible;

            public void setOptionsVisible(boolean value) {

                this.optionsVisible = value;

                if (value) {
                    eventOptions.setVisibility(VISIBLE);
                } else {
                    eventOptions.setVisibility(GONE);
                }

            }

            private int position;

            public void setPosition(int value) {
                this.position = value;
            }

            public MotionEventHolder(View v) {
                super(v);

                eventDateTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDATETIME);
                eventMonitorNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTDEVICENAME);
                eventCameraNameTextView = (TextView) v.findViewById(R.id.TXV___VSEVENTROW___EVENTCAMERANAME);

                progressBar = (ProgressBar) v.findViewById(R.id.PBR___VSEVENTROW___DOWNLOADPROGRESS);
                eventLabels = (ConstraintLayout) v.findViewById(R.id.CLA___VSEVENTROW___LABELS);
                eventOptions = (ConstraintLayout) v.findViewById(R.id.CLA___VSEVENTROW___EVENTOPTIONS);

                eventPreviewImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___EVENTPREVIEW);
                newItemImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___NEWITEM);
                lockedItemImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___LOCKEDITEM);
                eventLocationImage = (ImageView) v.findViewById(R.id.IVW___VSEVENTROW___EVENTLOCATION);

                eventContainer = (ConstraintLayout) v.findViewById(R.id.CLA___VSEVENTROW___EVENT);

                setOptionsVisible(false);

                eventLabels.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        setOptionsVisible(!optionsVisible);

                    }

                });

            }

        }

        private final LayoutInflater layoutInflater;
        private List<com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent> motionEvents;

        MotionEventsListAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public MotionEventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.row_holder_motionevent_element, parent, false);
            return new MotionEventHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MotionEventHolder holder, final int position) {

            if (motionEvents != null) {

                final com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent event = motionEvents.get(position);

                holder.eventMonitorNameTextView.setText(event.getCameraFullID());
                holder.eventCameraNameTextView.setText(event.getCameraName());

                holder.eventDateTextView.setText(getTimeElapsedToday(event.getTimeStamp(), getApplicationContext()));

                // crea il File relativo alla posizione di download locale sul dispositivo
                final File localVideoFile = new File(downloadDirectoryRoot, VIDEO_SUBDIR + "/" + event.getVideoFileName());

                // crea il File relativo alla posizione di download locale sul dispositivo
                final File localThumbnailFile = new File(downloadDirectoryRoot, THUMB_SUBDIR + "/" + event.getPictureFileName());

                final String videoFileRemoteLocation = String.format("MotionEvents/%s/%s", groupName, event.getVideoFileName());
                final String thumbnailFileRemoteLocation = String.format("MotionEvents/%s/%s", groupName, event.getPictureFileName());

                // controlla se il File creato esiste
                if (localVideoFile.exists()) { // esiste

                    // imposta l'immagine da visualizzare
                    holder.eventLocationImage.setImageResource(R.drawable.sdcard);

                    // imposta l'OnClickListener per lanciare il video tramite un Intent
                    holder.eventPreviewImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // lancia il video
                            playVideo(localVideoFile.getAbsolutePath());

                            // segna l'evento come già letto
                            new MarkEventAsRead(localDatabase).execute(event.getId());

                        }

                    });

                } else { // non esiste

                    // imposta l'immagine da visualizzare
                    holder.eventLocationImage.setImageResource(R.drawable.cloud);

                    // imposta l'OnClickListener per scaricare il video in una cartella locale
                    holder.eventPreviewImage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            // mostra la progressbar
                            holder.progressBar.setVisibility(VISIBLE);
                            holder.progressBar.setIndeterminate(true);

                            FirebaseStorage.getInstance().getReference(videoFileRemoteLocation).getFile(localVideoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                    // nasconde la progressbar
                                    holder.progressBar.setVisibility(GONE);

                                    // esegue il video
                                    playVideo(localVideoFile.getAbsolutePath());

                                    // segna l'evento come già visto
                                    new MarkEventAsRead(localDatabase).execute(event.getId());

                                }

                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    // nasconde la progressbar
                                    holder.progressBar.setVisibility(GONE);

                                }

                            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                    if (holder.progressBar.isIndeterminate())
                                        holder.progressBar.setIndeterminate(false);

                                    // calcola la percentuale dello scaricamento
                                    int progress = (int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());

                                    // aggiorna la progressBar di conseguenza
                                    holder.progressBar.setProgress(progress);

                                }

                            });

                        }

                    });

                }

                // recupera i dati dell'immagine della preview

                // controlla che sia già stata scaricata
                if (localThumbnailFile.exists()) { // è già stata scaricata

                    updateEventPreviewImage(holder.eventPreviewImage, localThumbnailFile.getAbsolutePath());

                } else { // non è già stata scaricata

                    holder.eventPreviewImage.setImageResource(R.drawable.dummy_movie);

                    FirebaseStorage.getInstance().getReference(thumbnailFileRemoteLocation).getFile(localThumbnailFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            updateEventPreviewImage(holder.eventPreviewImage, localThumbnailFile.getAbsolutePath());

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // adatta le dimensioni dell'immagine a quelle disponibili su schermo
                            holder.eventPreviewImage.setImageResource(R.drawable.broken);
                        }
                    });

                }

                // gestisce il tag NEW
                if (event.isNewItem()) {
                    holder.newItemImage.setVisibility(VISIBLE);
                } else {
                    holder.newItemImage.setVisibility(GONE);
                }


            } else {
                // Covers the case of data not being ready yet.
                //TODO: popolare l'holder
            }
        }

        void setWords(List<com.apps.lore_f.domoticcontroller.room.database.motionevents.MotionEvent> events) {
            motionEvents = events;
            notifyDataSetChanged();
        }

        // getItemCount() is called many times, and when it is first called,
        // mWords has not been updated (means initially, it's null, and we can't return null).
        @Override
        public int getItemCount() {
            if (motionEvents != null)
                return motionEvents.size();
            else return 0;
        }


    }

    private void updateEventPreviewImage(ImageView image, String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        image.setImageBitmap(bitmap);
    }

    private void playVideo(String videoFullPath) {

        // lancia il video
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(videoFullPath), "video/*");
        startActivity(intent);

    }

    private void shareVideo(String videoFullPath) {

        Uri uriPath = Uri.parse(videoFullPath);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Text");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uriPath);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "send"));

    }

}
