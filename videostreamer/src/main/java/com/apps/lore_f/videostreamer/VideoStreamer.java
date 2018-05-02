package com.apps.lore_f.videostreamer;

import android.os.AsyncTask;
import android.util.Log;

import net.sf.jipcam.axis.MjpegFrame;
import net.sf.jipcam.axis.MjpegInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lore_f on 24/04/2018.
 */

public class VideoStreamer {

    private final static String TAG = "VideoStreamer";
    private final static int DEFAULT_MAX_BUFFER_TIME = 5;
    private final static int DEFAULT_MIN_BUFFER_TIME = 1;
    private final static double DEFAULT_FPS = 25.0;

    private PipedOutputStream out = new PipedOutputStream();
    private BufferedOutputStream outBuffer = new BufferedOutputStream(out);

    private PipedInputStream in = new PipedInputStream();
    private BufferedInputStream inBuffer = new BufferedInputStream(in);

    private boolean running = true;
    private boolean streaming = false;

    /* interface */
    public interface VideoStreamerListener {
        void onBufferOk();

        void onBufferKo();

        void onFrame(byte[] data);

    }

    private VideoStreamerListener localListener;

    public void setVideoStreamerListener(VideoStreamerListener l) {
        localListener = l;
    }

    public void removeVideoStreamerListener(VideoStreamerListener l) {
        localListener = null;
    }

    private int maxBufferTime = DEFAULT_MAX_BUFFER_TIME;

    public void setMaxBufferTime(int value) {
        maxBufferTime = value;
        reCalcFrameBufferSize();
    }

    private int minBufferTime = DEFAULT_MIN_BUFFER_TIME;

    public void setMinBufferTime(int value) {
        minBufferTime = value;
        reCalcFrameBufferSize();
    }

    private double streamFPS = DEFAULT_FPS;

    public void setStreamFPS(double value) {
        streamFPS = value;
        reCalcFrameBufferSize();
    }

    private int minFramesBuffer;
    private int maxFramesBuffer;

    private List<byte[]> frames = new ArrayList<>();

    private class MainAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            long lastFrameTime = System.currentTimeMillis();
            long timeBetweenFrames = (int) (1000 / streamFPS);

            MjpegInputStream input = new MjpegInputStream(inBuffer);

            while (running) {
                
                /*
                    Attempts to read a frame from the MJpeg stream
                     */
                MjpegFrame frame = null;
                try {
                    frame = input.readMjpegFrame();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
                if (frame != null) {

                        /*
                        A new frame has been read. Adds it to the frame buffer list
                        */

                    frames.add(frame.getJpegBytes());
                    Log.d(TAG, String.format("new frame added. total frames:%d, buffer to go:%d", frames.size(), maxFramesBuffer));

                        /*
                        if streaming is not active, checks if there are the conditions to resume the streaming
                        */

                    if (!streaming && frames.size() > maxFramesBuffer) {

                        if (localListener != null)
                            localListener.onBufferOk();

                        streaming = true;

                    }

                }


                if (streaming && (System.currentTimeMillis() - lastFrameTime > timeBetweenFrames)) {

                        /*
                        serve the next frame
                         */

                    if (localListener != null) {
                        localListener.onFrame(frames.get(0));
                        Log.d(TAG, String.format("new frame served. total frames:%d, buffer to go:%d", frames.size(), minFramesBuffer));


                    }

                    frames.remove(0);
                    lastFrameTime = System.currentTimeMillis();

                }

                if (frames.size() < minFramesBuffer) {

                    streaming = false;
                    if (localListener != null)
                        localListener.onBufferKo();
                }

            }

            return null;
        }
        
    }

    /* constructor */
    public VideoStreamer() {

        try {
            in.connect(out);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void feed(byte[] data) {

        /* attempts to write the data byte array into the main data buffer */

        try {

            outBuffer.write(data);

        } catch (IOException e) {

            Log.e(TAG, e.getMessage());
        }

    }

    private void reCalcFrameBufferSize() {

        minFramesBuffer = (int) (minBufferTime * streamFPS);
        maxFramesBuffer = (int) (maxBufferTime * streamFPS);

    }

    public void start() {

        new MainAsyncTask().execute();

    }

    public void stop() {

        running=false;

    }

}
