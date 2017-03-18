package com.example.ivan.wifidirectclient2;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.widget.TextView;

import java.util.Arrays;

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

    //SYSTEM MANAGEMENT
    boolean             image_loaded = false;
    boolean             wifi_connected = false;
    private             MainActivity mActivity;

    public DataManagement(MainActivity activity){
        Log.d(TAG, "Data Manager Called");
        this.mActivity = activity;
    }

    public void loadImage(byte[] b){
        if (!image_loaded){
            image_holder = Arrays.copyOf(b, b.length);;
            image_loaded = true;
            Log.d(TAG, "Data Manager, Image loaded: " + load_count);
            load_count = load_count + 1;
        }else{
            Log.d(TAG,"Image not loaded, holder full");
        }
    }

    public byte[] getImage(){
        if(image_loaded){
            image_loaded = false;
            return  image_holder;
        }else{
            Log.d(TAG,"No Image Loaded");
            return null;
        }
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

    public void setConnectionStatus(boolean b){
        wifi_connected = b;
    }

    public void testDataManagerCall(){
        Log.d(TAG,"Data Manager Call: " + test_count);
        test_count = test_count + 1;
    }


}
