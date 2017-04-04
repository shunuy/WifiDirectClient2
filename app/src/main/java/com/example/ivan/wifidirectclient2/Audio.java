package com.example.ivan.wifidirectclient2;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by ivan on 1/4/17.
 */
public class Audio implements Runnable{

    private static final    String TAG = "NEUTRAL";

    AudioRecord             recorder;
    private int             channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int             audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int             sampleRate = 8000;
    private int             minBufSize = 2048;
    byte[]                  buffer = new byte[minBufSize];
    boolean                 audioStatus = true;

    DataManagement          dm;

    public Audio(DataManagement d){
        Log.d(TAG,"Audio Class Called");
        dm = d;
        //minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);

        Log.d(TAG,"Min Buffer Size is:" + minBufSize);
        Log.d(TAG,"Audio File Format is: " + audioFormat);

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,sampleRate,channelConfig,audioFormat,minBufSize);
    }

    public void run(){

        Log.d(TAG,"Starting Audio Class");

        recorder.startRecording();
        Log.d(TAG, "Recorder initialized");

        //Audio Information Retrieval Loop
        while(audioStatus){

            recorder.read(buffer, 0, buffer.length);
            dm.loadAudio(buffer);
            try{
                Thread.sleep(100);
            }catch (InterruptedException e) {
                Log.d(TAG,"Error sleeping thread: " + e.toString());
            }

            if (Thread.interrupted()) {
                Log.d(TAG,"Audio Thread Interrupted");
                audioStatus = false;
                recorder.stop();
            }

        }
    }

}
