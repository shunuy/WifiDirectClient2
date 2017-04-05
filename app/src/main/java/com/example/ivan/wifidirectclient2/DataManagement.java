package com.example.ivan.wifidirectclient2;

import android.util.Log;

import java.util.List;

/**
 * Created by ivan on 17/3/17.
 */
public class DataManagement {

    private static final String TAG = "NEUTRAL";

    //DEBUGGING
    int                 test_count = 0;
    int                 load_count = 0;

    //SYSTEM DATA
    byte[]              image_holder;
    byte[]              audio_holder;
    int                 image_height;
    int                 image_width;
    int                 audioBufSize;
    List<String>        resSize;
    List<String>        fpsList;

    //SYSTEM MANAGEMENT
    boolean             image_loaded = false;
    boolean             audio_loaded = false;
    boolean             wifi_connected = false;
    private             MainActivity mActivity;
    boolean             resUpdated = false;
    boolean             fpsUpdated = false;

    public DataManagement(MainActivity activity){
        Log.d(TAG, "Data Manager Called");
        this.mActivity = activity;
    }

    public void loadImage(byte[] b){
        if (!image_loaded){
            image_holder = b;
            image_loaded = true;
            Log.d(TAG, "Data Manager, Image loaded: " + load_count);
            load_count = load_count + 1;
        }else{
            Log.d(TAG,"Image not loaded, holder full");
        }
    }

    public boolean loadAudio (byte[] b){
        if(!audio_loaded){
            audio_holder = b;
            audio_loaded = true;
            Log.d(TAG,"Data Manager, Audio Loaded: " + load_count);
            return true;
        }else {
            Log.d(TAG,"Audio not loaded, holder full");
            return false;
        }
    }

    public byte[] getImage(){
        if(image_loaded){
            return image_holder;
        }else{
            Log.d(TAG,"No Image Loaded");
            return null;
        }
    }

    public byte[] getAudio(){
        if(audio_loaded){
            return audio_holder;
        }else{
            Log.d(TAG,"No Audio Loaded");
            return null;
        }
    }

    public void unloadImage(){
        image_loaded = false;
    }

    public void unloadAudio(){
        audio_loaded = false;
    }

    public boolean getImageLoadStatus(){
        return image_loaded;
    }

    public boolean getConnectionStatus(){
        return wifi_connected;
    }

    public boolean getLoadStatus(){
        if (image_loaded){
            return true;
        }else{
            return false;
        }
    }

    public int getImageHeight(){return image_height;}

    public int getImageWidth(){return image_width;}

    public List<String> getAvailableFPS(){
        //Log.d(TAG,"Getting available FPS Range");
        if (fpsUpdated){
            Log.d(TAG,"Returning FPS Range");
            return fpsList;
        }else{
            Log.d(TAG,"Returning Null");
            return null;
        }
    }

    public List<String> getAvailableRes(){
        //Log.d(TAG,"Getting available Res");
        if (resUpdated){
            Log.d(TAG,"Returning Res Range");
            return resSize;
        }else{
            Log.d(TAG,"Returning Null");
            return null;
        }
    }

    public void setConnectionStatus(boolean b){
        wifi_connected = b;
    }

    public void setImageSize(int w, int h){
        image_height = h;
        image_width = w;
    }

    public void setAudioBufSize(int i){ audioBufSize = i; }

    public void setAvailableResSize(List<String> r){
        resSize = r;
        resUpdated = true;
        mActivity.updateUserSelection();
    }

    public void setAvailableFpsRange(List<String> f){
        fpsList = f;
        fpsUpdated = true;
    }

    public int getAudioBufSize(){return audioBufSize; }

    public void testDataManagerCall(){
        Log.d(TAG,"Data Manager Call: " + test_count);
        test_count = test_count + 1;
    }


}
