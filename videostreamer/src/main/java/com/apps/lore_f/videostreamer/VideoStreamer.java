package com.apps.lore_f.videostreamer;

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
    private final static int DEFAULT_MAX_BUFFER_TIME=5;
    private final static int DEFAULT_MIN_BUFFER_TIME=1;
    private final static double DEFAULT_FPS=25.0;

    private PipedOutputStream out = new PipedOutputStream();
    private BufferedOutputStream outBuffer = new BufferedOutputStream(out);

    private PipedInputStream in = new PipedInputStream();
    private BufferedInputStream inBuffer = new BufferedInputStream(in);

    private boolean running = true;
    private boolean streaming=false;

    /* interface */
    interface VideoStreamerListener{
        void onBufferOk();
        void onBufferKo();
        void onFrame(byte[] data);

    }

    private VideoStreamerListener localListener;

    public void setVideoStreamerListener(VideoStreamerListener l){
        localListener=l;
    }

    public void removeVideoStreamerListener(VideoStreamerListener l){
        localListener=null;
    }


    private int maxBufferTime=DEFAULT_MAX_BUFFER_TIME;
    public void setMaxBufferTime(int value){
        maxBufferTime=value;
        reCalcFrameBufferSize();
    }

    private int minBufferTime=DEFAULT_MIN_BUFFER_TIME;
    public void setMinBufferTime(int value){
        minBufferTime=value;
        reCalcFrameBufferSize();
    }

    private double streamFPS=DEFAULT_FPS;
    public void setStreamFPS(double value){
        streamFPS=value;
        reCalcFrameBufferSize();
    }

    private int minFramesBuffer;
    private int maxFramesBuffer;

    private List<byte[]> frames = new ArrayList<>();

    private Thread frameFeeder = new Thread(){

        @Override
        public void run(){

            try {

                MjpegInputStream input = new MjpegInputStream(inBuffer);

                while(running){

                    /*
                    This is the main thread cycle
                     */

                    /*
                    Attempts to read a frame from the MJpeg stream
                     */
                    MjpegFrame frame = input.readMjpegFrame();
                    if(frame!=null){

                        /*
                        A new frame has been read. Adds it to the frame buffer list
                        */

                        frames.add(frame.getJpegBytes());

                        /*
                        if streaming is not active, checks if there are the conditions to resume the streaming
                        */

                        if(!streaming && frames.size()>maxFramesBuffer){

                            if(localListener!=null)
                                localListener.onBufferOk();

                            streaming=true;

                        }

                    }

                }

            } catch (IOException e) {

                running = false;
                Log.e(TAG, e.getMessage());

            }

        }

    };

    private Thread frameStreamer = new Thread(){

        @Override
        public void run(){

            long lastFrameTime = System.currentTimeMillis();
            long timeBetweenFrames = (int) (1000/streamFPS);

            while(running){

                while(streaming){


                    if (lastFrameTime-System.currentTimeMillis()<timeBetweenFrames){

                        /*
                        waits
                         */

                    } else {

                        if(localListener!=null){
                            localListener.onFrame(frames.get(0));
                        }

                        frames.remove(0);
                        lastFrameTime = System.currentTimeMillis();

                    }

                    if(frames.size()<minFramesBuffer)
                        streaming = false;

                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage());
                }

            }

        }

    };

    /* constructor */
    public VideoStreamer() {

        try {
            in.connect(out);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void feed(byte[] data){

        /* attempts to write the data byte array into the main data buffer */

        try {

            outBuffer.write(data);

        } catch (IOException e) {

           Log.e(TAG, e.getMessage());
        }

    }

    private void reCalcFrameBufferSize(){

        minFramesBuffer=(int) (minBufferTime*streamFPS);
        maxFramesBuffer=(int) (maxBufferTime*streamFPS);

    }

    public void start(){

        frameFeeder.start();
        frameStreamer.start();

    }

}
