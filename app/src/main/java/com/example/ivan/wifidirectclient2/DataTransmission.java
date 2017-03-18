package com.example.ivan.wifidirectclient2;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by ivan on 17/3/17.
 */
public class DataTransmission implements Runnable{
    private static final String TAG = "NEUTRAL";

    private Boolean transmissionReady=false;
    private int port;
    private byte[] pictureData, audioData;
    InetAddress targetIP;
    WifiP2pInfo wifiP2pInfo;

    int transfer_count = 0;
    private MainActivity mActivity;
    DataManagement dm;

    public DataTransmission(MainActivity activity, int p, WifiP2pInfo info, DataManagement d){
        Log.d(TAG,"Data Transmission Class Called");
        this.mActivity = activity;
        this.port = p;
        this.wifiP2pInfo = info;
        this.dm = d;
    }

    public void updateInitialisationData(WifiP2pInfo info){
        this.wifiP2pInfo = info;
    }

    public void run(){
        Log.d(TAG,"Initialising Data Transmission Class");
        targetIP = wifiP2pInfo.groupOwnerAddress;
        transmissionReady = true;
        Log.d(TAG,"Data Transmission Initialisation Completed");

        Log.d(TAG,"Begin Transmission Wait Loop");
        while (true){
            //Check if Wifi P2P is connected
            if (dm.getConnectionStatus()){
                //Check if image and Audio are available
                if (dm.getLoadStatus()){
                    Log.d(TAG,"Initating transfer: " + transfer_count);
                    transfer_count = transfer_count + 1;
                    transfer();
                }
                else{
                    Log.d(TAG,"Image not ready");
                }
            }
            //Delay before retrieving next frame
            synchronized (this) {
                try {
                    wait(10);
                } catch (Exception e) {}
            }
        }
    }

    public void transfer(){
        pictureData = dm.getImage();

        byte[] transfer = null;
        transfer = new byte[pictureData.length];
        System.arraycopy(pictureData, 0, transfer, 0, pictureData.length);
        Log.d(TAG, "Client: Preparing to send");

        try {
            Socket clientSocket = null;
            OutputStream os = null;
            clientSocket = new Socket(targetIP,port);
            os = clientSocket.getOutputStream();
            os.write(transfer, 0, transfer.length);
            os.flush();
            os.close();
            clientSocket.close();
            Log.d(TAG, "Send Complete");
        } catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }
    }
}
