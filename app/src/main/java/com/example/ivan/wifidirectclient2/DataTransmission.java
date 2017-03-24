package com.example.ivan.wifidirectclient2;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * Created by ivan on 17/3/17.
 */
public class DataTransmission implements Runnable{
    private static final String TAG = "NEUTRAL";

    private Boolean transmissionReady=false;
    private int port;
    private byte[] nv21_buffer;
    private byte[] pictureData, audioData;
    InetAddress targetIP;
    WifiP2pInfo wifiP2pInfo;

    int transfer_count = 0;
    private MainActivity mActivity;
    DataManagement dm;
    OutputStream os = null;
    Socket clientSocket = null;


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

        try {
            clientSocket = new Socket(targetIP,port);
            clientSocket.setPerformancePreferences(0 , 1, 1);
            clientSocket.setTcpNoDelay(true);
            clientSocket.setSendBufferSize(1024*1024);
            clientSocket.setReceiveBufferSize(1024*1024);
            Log.d(TAG,"=========Client Socket Details=========");
            Log.d(TAG,"Send Buffer Size: " + clientSocket.getSendBufferSize());
            Log.d(TAG,"Receive Buffer Size: " + clientSocket.getReceiveBufferSize());

            os = clientSocket.getOutputStream();
            //os = new BufferedOutputStream(clientSocket.getOutputStream());

        }catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }

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
                    //Log.d(TAG,"Image not ready");
                }
            }
            //Delay before retrieving next frame
            synchronized (this) {
                try {
                    wait(10);
                } catch (Exception e) {}
            }

            if (Thread.interrupted()) {
                Log.d(TAG,"Thread Interrupted");

                try{
                    clientSocket.close();
                }catch (IOException e) {
                    Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
                }

                return;
            }
        }
    }

    public void transfer(){
        //pictureData = dm.getImage();
        Log.d(TAG,"Client: Processing Image");

        int write;
        int marker=0;
        int picture_length;
        nv21_buffer = dm.getImage();

        YuvImage yuv = new YuvImage(nv21_buffer, 17, 640, 480, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(50, 50, 590, 430), 50, out);
        pictureData = out.toByteArray();
        byte[] transfer_length  = ByteBuffer.allocate(4).putInt(pictureData.length).array();
        picture_length = pictureData.length;

        /*
        byte[] transfer = dm.getImage();
        byte[] transfer_length  = ByteBuffer.allocate(4).putInt(transfer.length).array();
        */

        //int test_length = byteArrayToInt(transfer_length);
        //Log.d(TAG, "Transfer length test: " + test_length);

        Log.d(TAG, "Client: Preparing to send");
        Log.d(TAG, "Sending First Packet");
        try {
            os.write(transfer_length, 0, transfer_length.length);
        } catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }
        Log.d(TAG, "Sending Second Packet");
        Log.d(TAG,"Length of data: " + picture_length);
        while(marker < picture_length){

            if(picture_length - marker >=1024){
                write = 1024;
            }else{
                write = picture_length - marker;
            }
            try{
                //Log.d(TAG,"Output stream, marker position: " + marker + " write: " + write);
                os.write(pictureData, marker, write);
            } catch (IOException e) {
                Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
            }
            marker = marker + write;
        }
        Log.d(TAG,"Send Complete");
        dm.unloadImage();
        /*
        try {
            os.write(transfer_length, 0, transfer_length.length);
           // Log.d("NEUTRAL","Size of transfer length: " + transfer_length.length);
            //os.write(transfer, 0, transfer.length);
            //Log.d("NEUTRAL","Size of data transfer: " + transfer.length);
            os.write(pictureData, 0, pictureData.length);
            Log.d("NEUTRAL","Size of data transfer: " + pictureData.length);

            //os.flush();
           // os.close();
            //Log.d(TAG, "Send Complete");
            dm.unloadImage();
        } catch (IOException e) {
            Log.d(TAG, "Client Service Error, IO Exception: " + e.getMessage());
        }
        */
    }

    public static int byteArrayToInt(byte[] b)
    {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }
}
